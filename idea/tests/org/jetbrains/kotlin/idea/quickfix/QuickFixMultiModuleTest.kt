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

package org.jetbrains.kotlin.idea.quickfix

import org.jetbrains.kotlin.config.TargetPlatformKind
import org.junit.Test

class QuickFixMultiModuleTest : AbstractQuickFixMultiModuleTest() {

    private fun doCommonPlusTest(headerName: String = "header",
                                 implName: String = "jvm",
                                 implKind: TargetPlatformKind<*> = TargetPlatformKind.Jvm.JVM_1_6) {
        val header = module(headerName)
        header.setPlatformKind(TargetPlatformKind.Common)

        val jvm = module(implName)
        jvm.setPlatformKind(implKind)
        jvm.enableMultiPlatform()
        jvm.addDependency(header)

        doQuickFixTest()
    }

    @Test
    fun testAbstract() {
        doCommonPlusTest(implName = "js", implKind = TargetPlatformKind.JavaScript)
    }

    @Test
    fun testClass() {
        doCommonPlusTest()
    }

    @Test
    fun testEnum() {
        doCommonPlusTest(implName = "js", implKind = TargetPlatformKind.JavaScript)
    }

    @Test
    fun testFunction() {
        doCommonPlusTest()
    }

    @Test
    fun testInterface() {
        doCommonPlusTest()
    }

    @Test
    fun testObject() {
        doCommonPlusTest()
    }

    @Test
    fun testNested() {
        doCommonPlusTest()
    }

    @Test
    fun testProperty() {
        doCommonPlusTest()
    }

    @Test
    fun testSealed() {
        doCommonPlusTest(implName = "js", implKind = TargetPlatformKind.JavaScript)
    }
}