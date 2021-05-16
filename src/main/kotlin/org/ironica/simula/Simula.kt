package org.ironica.simula

import org.apache.logging.log4j.kotlin.Logging
import javax.script.ScriptEngineManager
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

data class ProvidedProperty(val name: String, val type: KClass<*>, val value: Any?) {
    constructor(name: String, type: Class<*>, value: Any?) : this(name, type.kotlin, value)
}

object SimulaRunner : Logging {

    fun evalScript(scriptFile: SourceCode, props: List<ProvidedProperty>): ResultWithDiagnostics<EvaluationResult> {
        val compileConfig = ScriptCompilationConfiguration {
            jvm {
                dependenciesFromCurrentContext(wholeClasspath = true)
                defaultImports(
                    "org.ironica.playground.*",
                )
            }
            providedProperties(*(props.map { it.name to KotlinType(it.type) }.toTypedArray()))
        }
        val evaluationConfig = ScriptEvaluationConfiguration {
            providedProperties(*(props.map { it.name to it.value }.toTypedArray()))
        }

        return BasicJvmScriptingHost().eval(scriptFile, compileConfig, evaluationConfig)
    }
}