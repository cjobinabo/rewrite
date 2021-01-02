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
package org.openrewrite.java.style;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.openrewrite.java.JavaStyle;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class BlankLineStyle implements JavaStyle {
    KeepMaximum keepMaximum = new KeepMaximum();
    Minimum minimum = new Minimum();

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class KeepMaximum {
        int inDeclarations;
        int inCode;
        int beforeEndOfBlock;
        int betweenHeaderAndPackage;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class Minimum {
        int beforePackage;
        int afterPackage;
        int beforeImports;
        int afterImports;
        int aroundClass;
        int afterClassHeader;
        int beforeClassEnd;
        int afterAnonymousClassHeader;
        int aroundFieldInInterface;
        int aroundField;
        int aroundMethodInInterface;
        int aroundMethod;
        int beforeMethodBody;
        int aroundInitializer;
    }
}
