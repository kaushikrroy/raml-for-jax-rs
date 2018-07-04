/*
 * Copyright 2013-2017 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.jaxrs.generator.extension.plugin;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.immutables.value.Value;
import org.raml.ramltopojo.EventType;
import org.raml.ramltopojo.extensions.AllTypesPluginHelper;
import org.raml.ramltopojo.extensions.ObjectPluginContext;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;

public class ImmutablesExtension extends AllTypesPluginHelper {

  @Override
  public TypeSpec.Builder classCreated(final ObjectPluginContext objectPluginContext,
                                       final ObjectTypeDeclaration ramlType,
                                       final TypeSpec.Builder incoming,
                                       final EventType eventType) {
    if (eventType == EventType.INTERFACE) {
      incoming.addAnnotation(AnnotationSpec.builder(
                                                    ClassName.get("org.immutables.value", "Value.Immutable")).build());
      incoming.addAnnotation(AnnotationSpec.builder(JsonDeserializer.class)
          .addMember("as", "Immutable" + ramlType.name() + ".class").build());
    }

    return incoming;
  }
}
