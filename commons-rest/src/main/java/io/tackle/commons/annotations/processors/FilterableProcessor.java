/*
 * Copyright © 2021 the Konveyor Contributors (https://konveyor.io/)
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

import io.tackle.commons.annotations.Filterable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.persistence.OneToMany;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes(value = {"io.tackle.commons.annotations.Filterable"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class FilterableProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        roundEnvironment
                .getElementsAnnotatedWith(OneToMany.class)
                .stream()
                .filter(element -> element.getAnnotation(Filterable.class) != null)
                .filter(element ->
                        // in case of 'OneToMany' relations, there must be a 'filterName' value to know
                        // the field in the referred entity to be used to filter 
                        element.getAnnotation(Filterable.class).filterName().isEmpty() ||
                        // and the 'filterName' value must contain a dot '.' to be sure the field
                        // in the referred entity is referenced in the right way (aka dot notation)
                        !element.getAnnotation(Filterable.class).filterName().contains("."))
                .forEach(element -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "@Filterable must have a 'filterName' value when used with a 'OneToMany' annotated field. The format must follow the 'dot' notation (i.e. 'entities.field')", element));
        return true;
    }
}
