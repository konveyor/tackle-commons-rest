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
