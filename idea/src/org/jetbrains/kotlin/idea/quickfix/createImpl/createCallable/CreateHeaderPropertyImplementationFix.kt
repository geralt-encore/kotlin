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
import org.jetbrains.kotlin.descriptors.PlatformKind
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.core.toDescriptor
import org.jetbrains.kotlin.idea.quickfix.createImpl.CreateHeaderImplementationFix
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

class CreateHeaderPropertyImplementationFix(
        property: KtProperty,
        implPlatformKind: PlatformKind
) : CreateHeaderImplementationFix(property, implPlatformKind) {
    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val headerProperty = element as? KtProperty ?: return
        val name = headerProperty.name ?: return
        val factory = KtPsiFactory(project)
        val implFile = getOrCreateImplementationFile(project, name) ?: return

        val descriptor = headerProperty.toDescriptor() as? PropertyDescriptor ?: return
        val implProperty = implFile.add(factory.generateProperty(project, headerProperty, descriptor, implNeeded = true)) as KtProperty
        ShortenReferences.DEFAULT.process(implProperty)
    }

    override fun getText() =  "Create header property implementation for platform ${implPlatformKind.name}"
}