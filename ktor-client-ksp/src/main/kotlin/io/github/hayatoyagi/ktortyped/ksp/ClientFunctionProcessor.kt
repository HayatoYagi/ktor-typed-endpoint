package io.github.hayatoyagi.ktortyped.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

private val ENDPOINT_CONTRACT_TYPES = mapOf(
    "io.github.hayatoyagi.ktortyped.GetEndpointContract" to ContractKind.GET,
    "io.github.hayatoyagi.ktortyped.PostEndpointContract" to ContractKind.POST,
    "io.github.hayatoyagi.ktortyped.PutEndpointContract" to ContractKind.PUT,
    "io.github.hayatoyagi.ktortyped.PatchEndpointContract" to ContractKind.PATCH,
    "io.github.hayatoyagi.ktortyped.DeleteEndpointContract" to ContractKind.DELETE,
)

private enum class ContractKind(val hasBody: Boolean) {
    GET(false), POST(true), PUT(true), PATCH(true), DELETE(false),
}

internal class ClientFunctionProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private var invocationCount = 0

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // KSP 2.x calls process() in multiple rounds; only run on the first invocation
        if (invocationCount++ > 0) return emptyList()
        data class ContractInfo(
            val obj: KSClassDeclaration,
            val superType: KSType,
            val kind: ContractKind,
        )

        val contracts = resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.OBJECT }
            .mapNotNull { obj ->
                val matchingSuperType = obj.superTypes
                    .map { it.resolve() }
                    .firstOrNull { st ->
                        st.declaration.qualifiedName?.asString() in ENDPOINT_CONTRACT_TYPES
                    } ?: return@mapNotNull null
                val kind = ENDPOINT_CONTRACT_TYPES[matchingSuperType.declaration.qualifiedName?.asString()]!!
                ContractInfo(obj, matchingSuperType, kind)
            }
            .toList()

        if (contracts.isEmpty()) return emptyList()

        contracts.groupBy { it.obj.packageName.asString() }.forEach { (pkg, group) ->
            generateFile(pkg, group.map { Triple(it.obj, it.superType, it.kind) })
        }

        return emptyList()
    }

    private fun generateFile(
        packageName: String,
        contracts: List<Triple<KSClassDeclaration, KSType, ContractKind>>,
    ) {
        val sourceFiles = contracts.mapNotNull { (obj, _, _) -> obj.containingFile }
        val file = codeGenerator.createNewFile(
            Dependencies(false, *sourceFiles.toTypedArray()),
            packageName,
            "GeneratedClientFunctions",
        )

        val sb = StringBuilder()
        sb.appendLine("@file:Suppress(\"unused\")")
        sb.appendLine()
        sb.appendLine("package $packageName")
        sb.appendLine()
        sb.appendLine("import io.ktor.client.HttpClient")
        sb.appendLine("import io.github.hayatoyagi.ktortyped.client.request")
        sb.appendLine()

        contracts.forEach { (obj, superType, kind) ->
            generateFunction(obj, superType, kind, sb)
        }

        file.use { it.write(sb.toString().toByteArray()) }
    }

    private fun generateFunction(
        obj: KSClassDeclaration,
        superType: KSType,
        kind: ContractKind,
        sb: StringBuilder,
    ) {
        val contractName = obj.simpleName.asString()
        val functionName = contractName.replaceFirstChar { it.lowercaseChar() }
        val typeArgs = superType.arguments

        val resourceType = typeArgs[0].type?.resolve() ?: run {
            logger.error("Cannot resolve Resource type for $contractName", obj)
            return
        }
        val resourceClass = resourceType.declaration as? KSClassDeclaration ?: run {
            logger.error("Resource type is not a class for $contractName", obj)
            return
        }
        val responseType = typeArgs.last().type?.resolve() ?: run {
            logger.error("Cannot resolve Response type for $contractName", obj)
            return
        }

        val dataParams = collectDataParams(resourceClass)
        val paramList = mutableListOf<String>()
        dataParams.forEach { param ->
            paramList.add("${param.name}: ${param.typeFqn}")
        }

        if (kind.hasBody) {
            val requestType = typeArgs[1].type?.resolve() ?: run {
                logger.error("Cannot resolve Request type for $contractName", obj)
                return
            }
            paramList.add("body: ${requestType.toFqnString()}")
        }

        val paramsStr = if (paramList.isEmpty()) "" else "\n    ${paramList.joinToString(",\n    ")},\n"
        val responseFqn = responseType.toFqnString()
        val contractFqn = obj.qualifiedName?.asString()
        val resourceExpr = buildResourceExpr(resourceClass, dataParams)

        sb.appendLine("suspend fun HttpClient.$functionName($paramsStr): $responseFqn =")
        if (kind.hasBody) {
            sb.appendLine("    request($contractFqn, $resourceExpr, body)")
        } else {
            sb.appendLine("    request($contractFqn, $resourceExpr)")
        }
        sb.appendLine()
    }

    private data class DataParam(val name: String, val typeFqn: String)

    /**
     * Recursively collects non-parent constructor parameters from [resourceClass] and its parent chain.
     * Parameters named "parent" with a default value are skipped (Ktor resource hierarchy boilerplate).
     * Parameters named "parent" without a default value are resolved recursively.
     */
    private fun collectDataParams(resourceClass: KSClassDeclaration): List<DataParam> {
        val constructor = resourceClass.primaryConstructor ?: return emptyList()
        val result = mutableListOf<DataParam>()
        for (param in constructor.parameters) {
            if (param.name?.asString() == "parent") {
                if (!param.hasDefault) {
                    val parentClass = param.type.resolve().declaration as? KSClassDeclaration
                    if (parentClass != null) {
                        result.addAll(0, collectDataParams(parentClass))
                    }
                }
                // parent with default → skip
            } else {
                result.add(DataParam(param.name!!.asString(), param.type.resolve().toFqnString()))
            }
        }
        return result
    }

    /**
     * Builds the constructor call expression for [resourceClass], filling in data params by name
     * and reconstructing parent params recursively when they lack default values.
     */
    private fun buildResourceExpr(
        resourceClass: KSClassDeclaration,
        dataParams: List<DataParam>,
    ): String {
        val fqn = resourceClass.qualifiedName?.asString()
        val constructor = resourceClass.primaryConstructor ?: return "$fqn()"
        val dataParamNames = dataParams.map { it.name }.toSet()

        val args = mutableListOf<String>()
        for (param in constructor.parameters) {
            val name = param.name?.asString() ?: continue
            when {
                name == "parent" && param.hasDefault -> { /* omit — use default */ }
                name == "parent" && !param.hasDefault -> {
                    val parentClass = param.type.resolve().declaration as? KSClassDeclaration
                    if (parentClass != null) {
                        // collect only the params that belong to the parent subtree
                        val parentDataParams = collectDataParams(parentClass)
                            .filter { it.name in dataParamNames }
                        args.add("parent = ${buildResourceExpr(parentClass, parentDataParams)}")
                    }
                }
                else -> args.add("$name = $name")
            }
        }

        return if (args.isEmpty()) "$fqn()" else "$fqn(${args.joinToString(", ")})"
    }

    private fun KSType.toFqnString(): String {
        val base = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
        val typeArgStr = if (arguments.isNotEmpty()) {
            "<${arguments.joinToString(", ") { arg ->
                arg.type?.resolve()?.toFqnString() ?: "*"
            }}>"
        } else {
            ""
        }
        val nullMark = if (isMarkedNullable) "?" else ""
        return "$base$typeArgStr$nullMark"
    }

    private fun KSValueParameter.toParamString(): String {
        val name = name?.asString() ?: "_"
        return "$name: ${type.resolve().toFqnString()}"
    }
}
