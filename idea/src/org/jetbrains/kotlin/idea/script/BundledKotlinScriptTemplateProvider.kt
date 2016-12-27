/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.script

import org.jetbrains.kotlin.script.*
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.util.concurrent.Future

class BundledKotlinScriptTemplateProvider : ScriptTemplatesProvider {
    override val id: String = "BundledKotlinScriptTemplateProvider"
    override val version: Int = 1
    override val isValid: Boolean = true

    override val templateClassNames: Iterable<String> get() = listOf("org.jetbrains.kotlin.idea.script.BundledScriptWithNoParam")
    override val dependenciesClasspath: Iterable<String> get() = emptyList()
    
    override val environment: Map<String, Any?>? = null
    override val resolver: ScriptDependenciesResolver = BundledKotlinScriptDependenciesResolver()
}

private class BundledKotlinScriptDependenciesResolver : ScriptDependenciesResolver {
    private val dependencies = KotlinBundledScriptDependencies()

    override fun resolve(
            script: ScriptContents,
            environment: Map<String, Any?>?,
            report: (ScriptDependenciesResolver.ReportSeverity, String, ScriptContents.Position?) -> Unit,
            previousDependencies: KotlinScriptExternalDependencies?): Future<KotlinScriptExternalDependencies?> {
        return PseudoFuture(dependencies)
    }
}

private class KotlinBundledScriptDependencies : KotlinScriptExternalDependencies {
    override val javaHome: String? get() = super.javaHome
    override val classpath: Iterable<File> get() {
        return with(PathUtil.getKotlinPathsForIdeaPlugin()) {
            listOf(
                    reflectPath,
                    runtimePath,
                    scriptRuntimePath
            )
        }
    }

    override val imports: Iterable<String> get() = super.imports
    override val sources: Iterable<File> get() = super.sources
    override val scripts: Iterable<File> get() = super.scripts
}

@Suppress("unused")
@ScriptTemplateDefinition
private class BundledScriptWithNoParam