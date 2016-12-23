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

package org.jetbrains.kotlin.idea.quickfix.createImpl.createCallable

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PlatformKind
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.core.toDescriptor
import org.jetbrains.kotlin.idea.quickfix.createImpl.CreateHeaderImplementationFix
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

class CreateHeaderFunctionImplementationFix(
        function: KtFunction,
        implPlatformKind: PlatformKind
) : CreateHeaderImplementationFix(function, implPlatformKind) {
    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val headerFunction = element as? KtFunction ?: return
        val name = headerFunction.name ?: return
        val factory = KtPsiFactory(project)
        val implFile = getOrCreateImplementationFile(project, name) ?: return

        val descriptor = headerFunction.toDescriptor() as? FunctionDescriptor ?: return
        val implFunction = implFile.add(factory.generateFunction(project, headerFunction, descriptor, implNeeded = true)) as KtFunction
        ShortenReferences.DEFAULT.process(implFunction)
    }

    override fun getText() =  "Create header function implementation for platform ${implPlatformKind.name}"
}