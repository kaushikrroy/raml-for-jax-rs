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
package org.raml.jaxrs.generator;

import com.google.common.io.Files;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import org.raml.jaxrs.generator.builders.AbstractTypeGenerator;
import org.raml.jaxrs.generator.builders.BuildPhase;
import org.raml.jaxrs.generator.builders.CodeContainer;
import org.raml.jaxrs.generator.builders.CodeModelTypeGenerator;
import org.raml.jaxrs.generator.ramltypes.GType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Jean-Philippe Belanger on 11/20/16. Just potential zeroes and ones
 */
public class JsonSchemaTypeGenerator extends AbstractTypeGenerator<JCodeModel> implements CodeModelTypeGenerator {

  private final CurrentBuild build;
  private final String pack;
  private final GType type;
  private final ClassName name;
  private final String schema;

  private JType model;

  public JsonSchemaTypeGenerator(CurrentBuild build, String pack, GType type, ClassName name, String schema) {
    this.build = build;
    this.pack = pack;
    this.type = type;
    this.name = name;
    this.schema = schema;
  }

  @Override
  public void output(CodeContainer<JCodeModel> container, BuildPhase buildPhase) throws IOException {

    final JCodeModel codeModel = new JCodeModel();
    JType model = createModel(codeModel);
    if (!model.fullName().startsWith("java.util.List")) {
      container.into(codeModel);
    }
  }

  public GType getType() {
    return type;
  }

  private JType createModel(JCodeModel codeModel) throws IOException {

    GenerationConfig config = build.getJsonMapperConfig();
    final SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(), new SchemaStore()),
                                                 new SchemaGenerator());

    File schemaFile = File.createTempFile("schema", "json", build.getSchemaRepository());
    Files.write(schema, schemaFile, Charset.defaultCharset());
    try {

      return mapper.generate(codeModel, name.simpleName(), pack, schemaFile.toURL());

    } catch (IOException e) {
      throw new GenerationException(e);
    }
  }

  @Override
  public TypeName getGeneratedJavaType() {

    try {
      final JCodeModel codeModel = new JCodeModel();
      JType model = createModel(codeModel);
      // duplicated logic with json2pojo. Should look in model.
      if (model.fullName().startsWith("java.util.List")) {

        return ParameterizedTypeName
            .get(ClassName.get(List.class), ClassName.get(name.packageName(), model.elementType().name()));
      } else {

        return ClassName.get(name.packageName(), build.getJsonMapperConfig().getClassNamePrefix() + name.simpleName()
            + build.getJsonMapperConfig().getClassNameSuffix());
      }
    } catch (IOException e) {
      throw new GenerationException(e);
    }
  }
}
