package org.ironica.simula

import org.apache.logging.log4j.kotlin.Logging
import org.ironica.playground.Status
import org.jetbrains.kotlinx.ki.shell.KotlinShell
import org.jetbrains.kotlinx.ki.shell.OnEval
import org.jetbrains.kotlinx.ki.shell.Shell
import org.jetbrains.kotlinx.ki.shell.bound
import org.jetbrains.kotlinx.ki.shell.wrappers.ResultWrapper
import javax.script.ScriptEngineManager
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.util.LinkedSnippet

data class ProvidedProperty(val name: String, val type: KClass<*>, val value: Any?) {
    constructor(name: String, type: Class<*>, value: Any?) : this(name, type.kotlin, value)
}

/**
 * Implementation of Eval Runner with help of @author dylech30th
 */
object SimulaRunner : Logging {

    private val defaultImportKotlinPackages = listOf(
        "kotlin.collections.*",
        "kotlin.comparisons.*",
        "kotlin.concurrent.*",
        "kotlin.coroutines.*",
        "kotlinx.coroutines.*",
        "kotlin.coroutines.intrinsics.*",
        "kotlin.io.*",
        "kotlin.io.path.*",
        "kotlin.jvm.*",
        "kotlin.math.*",
        "kotlin.random.*",
        "kotlin.reflect.*",
        "kotlin.reflect.full.*",
        "kotlin.reflect.jvm.*",
        "kotlin.sequences.*",
        "kotlin.streams.*",
        "kotlin.text.*",
        "kotlin.time.*",
    )

    private val defaultImportJavaPackages = listOf(
        "java.io.*",
        "java.lang.*",
        "java.math.*",
        "java.net.*",
        "java.util.*",
        "java.lang.ref.*",
        "java.lang.reflect.*",
        "java.lang.invoke.*",
        "java.nio.*",
        "java.nio.channels.*",
        "java.nio.file.*",
        "java.util.concurrent.*",
        "java.util.function.*",
        "java.util.regex.*",
        "java.util.stream.*",
        "sun.misc.*",
    )

    private val defaultImportPlaygroundPackage = listOf(
        "org.ironica.playground.*",
    )

    private val shell = Shell(
        KotlinShell.configuration(),
        defaultJvmScriptingHostConfiguration,
        ScriptCompilationConfiguration {
            jvm {
                dependenciesFromCurrentContext(wholeClasspath = true)
                defaultImports(
                    defaultImportKotlinPackages + defaultImportJavaPackages + defaultImportPlaygroundPackage
                )
            }
        },
        ScriptEvaluationConfiguration {
            jvm {
                baseClassLoader(Thread.currentThread().contextClassLoader)
            }
        }
    )

    init {
        shell.initEngine()
    }

    fun evalSnippet(source: String): Pair<String?, Status> {
        val time = System.nanoTime()
        val result = shell.eval(source)
        shell.evaluationTimeMillis = (System.nanoTime() - time) / 1_000_000
        return when (result.getStatus()) {
            ResultWrapper.Status.SUCCESS -> {
                shell.incompleteLines.clear()
                handleSuccess(result.result as ResultWithDiagnostics.Success<*>) to Status.OK
            }
            ResultWrapper.Status.ERROR -> {
                shell.incompleteLines.clear()
                handleError(result.result, result.isCompiled) to Status.ERROR
            }
            ResultWrapper.Status.INCOMPLETE -> {
                shell.incompleteLines.add(source)
                null to Status.INCOMPLETE
            }
        }
    }

    private fun handleSuccess(result: ResultWithDiagnostics.Success<*>): String? {
        val snippets = result.value as LinkedSnippet<KJvmEvaluatedSnippet>
        shell.eventManager.emitEvent(OnEval(snippets))
        return when (val evalResultValue = snippets.get().result) {
            is ResultValue.Value -> "${evalResultValue.name}${shell.renderResultType(evalResultValue)} = ${evalResultValue.value}".bound(shell.settings.maxResultLength)
            is ResultValue.Error -> renderError(evalResultValue)
            is ResultValue.Unit -> "Kotlin.Unit"
            ResultValue.NotEvaluated -> null
        }
    }

    private fun renderError(value: ResultValue.Error): String {
        val fullTrace = value.error.stackTrace
        return if (value.wrappingException == null
            || fullTrace.size < value.wrappingException!!.stackTrace.size) {
            value.error.stackTraceToString()
        } else {
            buildString {
                appendLine(value.error)
                val scriptTraceSize = fullTrace.size - value.wrappingException!!.stackTrace.size
                for (i in 0 until scriptTraceSize) {
                    appendLine("\tat " + fullTrace[i])
                }
            }
        }
    }

    private fun handleError(result: ResultWithDiagnostics<*>, isCompiled: Boolean): String {
        return buildString {
            result.reports.forEach {
                appendLine(it.render(withStackTrace = isCompiled))
            }
        }
    }

//    fun evalScript(scriptFile: SourceCode, props: List<ProvidedProperty>): ResultWithDiagnostics<EvaluationResult> {
//        val compileConfig = ScriptCompilationConfiguration {
//            jvm {
//                dependenciesFromCurrentContext(wholeClasspath = true)
//                defaultImports(
//                    "org.ironica.playground.*",
//                )
//            }
//            providedProperties(*(props.map { it.name to KotlinType(it.type) }.toTypedArray()))
//        }
//        val evaluationConfig = ScriptEvaluationConfiguration {
//            providedProperties(*(props.map { it.name to it.value }.toTypedArray()))
//        }
//
//        return BasicJvmScriptingHost().eval(scriptFile, compileConfig, evaluationConfig)
//    }
}