/*
 * Copyright © 2021 Konveyor (https://konveyor.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.tackle.commons.annotations.processors;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class FilterableProcessorTest {
    
    @Test
    public void test() {
        JavaFileObject sampleEntity = JavaFileObjects.forResource("FilterNameMissingEntity.java");
        Compilation compilation =
                javac()
                        .withProcessors(new FilterableProcessor())
                        .compile(sampleEntity);
        assertThat(compilation).failed();
        assertThat(compilation)
                .hadErrorContaining("@Filterable must have a 'filterName' value when used with a 'OneToMany' annotated field. The format must follow the 'dot' notation (i.e. 'entities.field')")
                .inFile(sampleEntity)
                .onLine(9)
                .atColumn(25);
    }
}
