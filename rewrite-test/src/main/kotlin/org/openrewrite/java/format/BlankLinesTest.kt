/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.format

import org.junit.jupiter.api.Test
import org.openrewrite.EvalVisitor
import org.openrewrite.EvalVisitorTest
import org.openrewrite.java.JavaParser
import org.openrewrite.java.style.IntelliJ

interface BlankLinesTest : EvalVisitorTest {
    override val visitors: Iterable<EvalVisitor<*>>
        get() = listOf(BlankLines())

    @Test
    fun keepMaximumInDeclarations(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            keepMaximum.inDeclarations = 0
        })),
        before = """
            public class Foo {
            
            
                private int field1;
                private int field2;
            
                {
                    field1 = 2;
                }
            
                public void foo1() {
                    new Runnable() {
                        public void run() {
                        }
                    };
                }
            
                public class InnerClass {
                }
            }
        """,
        after = """
            public class Foo {
                private int field1;
                private int field2;
                {
                    field1 = 2;
                }
                public void foo1() {
                    new Runnable() {
                        public void run() {
                        }
                    };
                }
                public class InnerClass {
                }
            }
        """
    )

    @Test
    fun keepMaximumInCode(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            keepMaximum.inCode = 0
        })),
        before = """
            public class Foo {
                private int field1;
                {
                
                
                    field1 = 2;
                }
            }
        """,
        after = """
            public class Foo {
                private int field1;
                {
                    field1 = 2;
                }
            }
        """
    )

    @Test
    fun keepMaximumBeforeEndOfBlock(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            keepMaximum.beforeEndOfBlock = 0
        })),
        before = """
            public class Foo {
                private int field1;
                {
                    field1 = 2;
                    
                    
                }
            }
        """,
        after = """
            public class Foo {
                private int field1;
                {
                    field1 = 2;
                }
            }
        """
    )

    @Test
    fun keepMaximumBetweenHeaderAndPackage(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            keepMaximum.betweenHeaderAndPackage = 0
        })),
        before = """
            /*
             * This is a sample file.
             */
            
            package com.intellij.samples;
            
            public class Foo {
            }
        """,
        after = """
            /*
             * This is a sample file.
             */
            package com.intellij.samples;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumPackageWithComment(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            keepMaximum.betweenHeaderAndPackage = 0
            minimum.beforePackage = 1 // this takes precedence over the "keep max"
        })),
        before = """
            /*
             * This is a sample file.
             */
            package com.intellij.samples;
            
            public class Foo {
            }
        """,
        after = """
            /*
             * This is a sample file.
             */
            
            package com.intellij.samples;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumBeforePackage(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.beforePackage = 1 // no blank lines if nothing preceding package
        })),
        before = """
            
            package com.intellij.samples;
            
            public class Foo {
            }
        """,
        after = """
            package com.intellij.samples;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumBeforePackageWithComment(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            keepMaximum.betweenHeaderAndPackage = 0
            minimum.beforePackage = 1 // this takes precedence over the "keep max"
        })),
        before = """
            /** Comment */
            package com.intellij.samples;
            
            public class Foo {
            }
        """,
        after = """
            /** Comment */
            
            package com.intellij.samples;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumBeforeImportsWithPackage(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.beforeImports = 1
        })),
        before = """
            package com.intellij.samples;
            import java.util.Vector;
            
            public class Foo {
            }
        """,
        after = """
            package com.intellij.samples;
            
            import java.util.Vector;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumBeforeImports(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.beforeImports = 1 // no blank lines if nothing preceding imports
        })),
        before = """
            
            import java.util.Vector;
            
            public class Foo {
            }
        """,
        after = """
            import java.util.Vector;
            
            public class Foo {
            }
        """.trimIndent()
    )

    @Test
    fun minimumBeforeImportsWithComment(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.beforeImports = 1
        })),
        before = """
            /*
             * This is a sample file.
             */
            import java.util.Vector;
            
            public class Foo {
            }
        """,
        after = """
            /*
             * This is a sample file.
             */
            
            import java.util.Vector;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumAfterPackageWithImport(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.beforeImports = 0
            minimum.afterPackage = 1
        })),
        before = """
            package com.intellij.samples;
            import java.util.Vector;
            
            public class Foo {
            }
        """,
        after = """
            package com.intellij.samples;
            
            import java.util.Vector;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumAfterPackage(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.afterPackage = 1
        })),
        before = """
            package com.intellij.samples;
            public class Foo {
            }
        """,
        after = """
            package com.intellij.samples;
            
            public class Foo {
            }
        """
    )

    @Test
    fun minimumAfterImports(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.afterImports = 1
        })),
        before = """
            import java.util.Vector;
            public class Foo {
            }
        """,
        after = """
            import java.util.Vector;
            
            public class Foo {
            }
        """.trimIndent()
    )

    @Test
    fun minimumAroundClass(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.aroundClass = 2
        })),
        before = """
            import java.util.Vector;
            
            public class Foo {
            }
            
            class Bar {
            }
        """,
        after = """
            import java.util.Vector;
            
            public class Foo {
            }
            
            
            class Bar {
            }
        """.trimIndent()
    )

    @Test
    fun minimumAfterClassHeader(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.afterClassHeader = 1
        })),
        before = """
            public class Foo {
                private int field1;
            }
        """,
        after = """
            public class Foo {
            
                private int field1;
            }
        """
    )

    @Test
    fun minimumBeforeClassEnd(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.beforeClassEnd = 1
        })),
        before = """
            public class Foo {
            }
        """,
        after = """
            public class Foo {
            
            }
        """
    )

    @Test
    fun minimumAfterAnonymousClassHeader(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.afterAnonymousClassHeader = 1
        })),
        before = """
            public class Foo {
                public void foo1() {
                    new Runnable() {
                        public void run() {
                        }
                    };
                }
            }
        """,
        after = """
            public class Foo {
                public void foo1() {
                    new Runnable() {
            
                        public void run() {
                        }
                    };
                }
            }
        """
    )

    @Test
    fun minimumAroundFieldInInterface(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.aroundFieldInInterface = 1
        })),
        before = """
            interface TestInterface {
                int MAX = 10;
                int MIN = 1;
            }
        """,
        after = """
            interface TestInterface {
                int MAX = 10;
            
                int MIN = 1;
            }
        """
    )

    @Test
    fun minimumAroundField(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.aroundField = 1
        })),
        before = """
            class Test {
                int max = 10;
                int min = 1;
            }
        """,
        after = """
            class Test {
                int max = 10;
            
                int min = 1;
            }
        """
    )

    @Test
    fun minimumAroundMethodInInterface(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.aroundMethodInInterface = 1
        })),
        before = """
            interface TestInterface {
                void method1();
                void method2();
            }
        """,
        after = """
            interface TestInterface {
                void method1();
            
                void method2();
            }
        """
    )

    @Test
    fun minimumAroundMethod(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.aroundMethod = 1
        })),
        before = """
            class Test {
                void method1() {}
                void method2() {}
            }
        """,
        after = """
            class Test {
                void method1() {}
            
                void method2() {}
            }
        """
    )

    @Test
    fun beforeMethodBody(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.beforeMethodBody = 1
        })),
        before = """
            class Test {
                void method1() {}
            
                void method2() {
                    int n = 0;
                }
            }
        """,
        after = """
            class Test {
                void method1() {
            
                }
            
                void method2() {
            
                    int n = 0;
                }
            }
        """
    )

    @Test
    fun aroundInitializer(jp: JavaParser) = assertRefactored(
        jp.withStyles(listOf(IntelliJ.defaultBlankLine().apply {
            minimum.aroundInitializer = 1
        })),
        before = """
            public class Foo {
                private int field1;
                {
                    field1 = 2;
                }
                private int field2;
            }
        """,
        after = """
            public class Foo {
                private int field1;
            
                {
                    field1 = 2;
                }
            
                private int field2;
            }
        """
    )
}
