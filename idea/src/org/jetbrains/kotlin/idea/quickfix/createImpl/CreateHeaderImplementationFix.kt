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

package org.jetbrains.kotlin.idea.quickfix.createImpl

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.ide.util.PackageUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.JavaDirectoryService
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.idea.core.TemplateKind
import org.jetbrains.kotlin.idea.core.getFunctionBodyTextFromTemplate
import org.jetbrains.kotlin.idea.core.toDescriptor
import org.jetbrains.kotlin.idea.project.TargetPlatformDetector
import org.jetbrains.kotlin.idea.quickfix.KotlinQuickFixAction
import org.jetbrains.kotlin.idea.quickfix.KotlinSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.quickfix.createImpl.createCallable.CreateHeaderFunctionImplementationFix
import org.jetbrains.kotlin.idea.quickfix.createImpl.createCallable.CreateHeaderPropertyImplementationFix
import org.jetbrains.kotlin.idea.quickfix.createImpl.createClass.CreateHeaderClassImplementationFix
import org.jetbrains.kotlin.idea.refactoring.getOrCreateKotlinFile
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

abstract class CreateHeaderImplementationFix(
        declaration: KtNamedDeclaration,
        val implPlatformKind: PlatformKind
) : KotlinQuickFixAction<KtNamedDeclaration>(declaration) {

    override fun getFamilyName() = text

    private fun Project.implementationModuleOf(headerModule: Module) =
            allModules().firstOrNull {
                TargetPlatformDetector.getPlatform(it).kind == implPlatformKind &&
                headerModule in ModuleRootManager.getInstance(it).dependencies
            }

    protected fun getOrCreateImplementationFile(
            project: Project,
            name: String
    ): KtFile? {
        val declaration = element as? KtNamedDeclaration ?: return null

        val headerDir = declaration.containingFile.containingDirectory
        val headerPackage = JavaDirectoryService.getInstance().getPackage(headerDir)

        val headerModule = ModuleUtilCore.findModuleForPsiElement(declaration) ?: return null
        val implModule = project.implementationModuleOf(headerModule) ?: return null
        val implDirectory = PackageUtil.findPossiblePackageDirectoryInModule(implModule, headerPackage?.name) ?: return null
        return getOrCreateKotlinFile("$name.kt", implDirectory)
    }

    private fun KtModifierListOwner.replaceHeaderModifier(implNeeded: Boolean) {
        if (implNeeded) {
            addModifier(KtTokens.IMPL_KEYWORD)
        }
        else {
            removeModifier(KtTokens.HEADER_KEYWORD)
        }
    }

    protected fun KtPsiFactory.generateClassOrObject(
            project: Project,
            headerClass: KtClassOrObject,
            implNeeded: Boolean
    ): KtClassOrObject {
        val header = headerClass.text
        val klass = if (headerClass is KtObjectDeclaration) createObject(header) else createClass(header)
        if (headerClass !is KtClass || !headerClass.isInterface()) {
            klass.declarations.forEach {
                if (it !is KtEnumEntry &&
                    it !is KtClassOrObject &&
                    !it.hasModifier(KtTokens.ABSTRACT_KEYWORD)) {
                    it.delete()
                }
            }

            declLoop@ for (headerDeclaration in headerClass.declarations) {
                if (headerDeclaration.hasModifier(KtTokens.ABSTRACT_KEYWORD)) continue
                val descriptor = headerDeclaration.toDescriptor() ?: continue
                val implDeclaration: KtDeclaration = when (headerDeclaration) {
                    is KtFunction -> generateFunction(project, headerDeclaration, descriptor as FunctionDescriptor, implNeeded = false)
                    is KtProperty -> generateProperty(project, headerDeclaration, descriptor as PropertyDescriptor, implNeeded = false)
                    else -> continue@declLoop
                }
                klass.addDeclaration(implDeclaration)
            }
        }

        return klass.apply {
            replaceHeaderModifier(implNeeded)
        }
    }

    protected fun KtPsiFactory.generateFunction(
            project: Project,
            headerFunction: KtFunction,
            descriptor: FunctionDescriptor,
            implNeeded: Boolean
    ): KtFunction {
        val body = run {
            val delegation = getFunctionBodyTextFromTemplate(
                    project,
                    TemplateKind.FUNCTION,
                    descriptor.name.asString(),
                    descriptor.returnType?.let {
                        IdeDescriptorRenderers.SOURCE_CODE.renderType(it)
                    } ?: "Unit"
            )

            "{$delegation\n}"
        }

        return if (headerFunction is KtSecondaryConstructor) {
            createSecondaryConstructor(headerFunction.text + " " +  body)
        }
        else {
            createFunction(headerFunction.text + " " +  body).apply {
                replaceHeaderModifier(implNeeded)
            }
        }
    }

    protected fun KtPsiFactory.generateProperty(
            project: Project,
            headerProperty: KtProperty,
            descriptor: PropertyDescriptor,
            implNeeded: Boolean
    ): KtProperty {
        val body = buildString {
            append("\nget()")
            append(" = ")
            append(getFunctionBodyTextFromTemplate(
                    project,
                    TemplateKind.FUNCTION,
                    descriptor.name.asString(),
                    descriptor.returnType?.let {
                        IdeDescriptorRenderers.SOURCE_CODE.renderType(it)
                    } ?: "Unit"
            )
            )
            if (descriptor.isVar) {
                append("\nset(value) {}")
            }
        }
        return createProperty(headerProperty.text + " " + body).apply {
            replaceHeaderModifier(implNeeded)
        }
    }

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val d = DiagnosticFactory.cast(diagnostic, Errors.HEADER_WITHOUT_IMPLEMENTATION)
            val declaration = d.psiElement as? KtNamedDeclaration ?: return null
            val compatibility = d.c
            if (compatibility.isNotEmpty()) return null
            val implPlatformKind = d.b.platformKind
            return when (declaration) {
                is KtClassOrObject -> CreateHeaderClassImplementationFix(declaration, implPlatformKind)
                is KtFunction -> CreateHeaderFunctionImplementationFix(declaration, implPlatformKind)
                is KtProperty -> CreateHeaderPropertyImplementationFix(declaration, implPlatformKind)
                else -> null
            }
        }
    }
}