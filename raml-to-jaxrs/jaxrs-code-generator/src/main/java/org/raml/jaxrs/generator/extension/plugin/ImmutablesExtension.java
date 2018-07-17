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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.immutables.value.Value.Immutable;
import org.raml.ramltopojo.EventType;
import org.raml.ramltopojo.extensions.AllTypesPluginHelper;
import org.raml.ramltopojo.extensions.ObjectPluginContext;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Extension to generate immutables for us.
 *
 * @author kroy@cvent.com
 */
public class ImmutablesExtension extends AllTypesPluginHelper {

  private static final Logger LOG = LoggerFactory.getLogger(ImmutablesExtension.class);

  @Override
  public TypeSpec.Builder classCreated(final ObjectPluginContext objectPluginContext,
                                       final ObjectTypeDeclaration ramlType,
                                       final TypeSpec.Builder incoming,
                                       final EventType eventType) {
    if (eventType == EventType.INTERFACE) {
      removeDuplicates(incoming);
      incoming.addAnnotation(AnnotationSpec.builder(Immutable.class).build());
      incoming
          .addAnnotation(AnnotationSpec.builder(JsonInclude.class)
              .addMember("value", "JsonInclude.Include.NON_NULL").build());
      incoming.addAnnotation(AnnotationSpec.builder(JsonDeserialize.class)
          .addMember("as", "Immutable" + ramlType.name() + ".class").build());
    }

    return incoming;
  }

  private void removeDuplicates(TypeSpec.Builder incoming) {
    try {
      Field annotations = incoming.getClass().getDeclaredField("annotations");
      annotations.setAccessible(true);

      List<AnnotationSpec> listOfAnnotations = (List<AnnotationSpec>) annotations.get(incoming);
      List<AnnotationSpec> toBeRemoved = new LinkedList<>();

      for (final AnnotationSpec each : listOfAnnotations) {
        if (each.type.equals(ClassName.get(com.fasterxml.jackson.databind.annotation.JsonDeserialize.class))) {
          toBeRemoved.add(each);
        } else if (each.type.equals(ClassName.get(com.fasterxml.jackson.databind.JsonDeserializer.class))) {
          toBeRemoved.add(each);
        }
      }

      listOfAnnotations.removeAll(toBeRemoved);
    } catch (NoSuchFieldException e) {
      LOG.warn("Could not remove duplicate annotations.", e);
    } catch (IllegalAccessException e) {
      LOG.warn("Could not remove duplicate annotations.", e);
    } finally {

    }
  }
}
