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

package org.jetbrains.kotlin.idea.quickfix.createImpl.createClass

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.PlatformKind
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.quickfix.createImpl.CreateHeaderImplementationFix
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

class CreateHeaderClassImplementationFix(
        klass: KtClassOrObject,
        implPlatformKind: PlatformKind
) : CreateHeaderImplementationFix(klass, implPlatformKind) {

    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val headerClass = element as? KtClassOrObject ?: return
        val name = headerClass.name ?: return
        val factory = KtPsiFactory(project)
        val implFile = getOrCreateImplementationFile(project, name) ?: return

        val implClass = implFile.add(factory.generateClassOrObject(project, headerClass, implNeeded = true)) as KtClassOrObject
        ShortenReferences.DEFAULT.process(implClass)
    }

    private val elementType: String
        get() = if ((element as? KtClass)?.isInterface() ?: false) "interface" else "class"


    override fun getText() = "Create header $elementType implementation for platform ${implPlatformKind.name}"
}