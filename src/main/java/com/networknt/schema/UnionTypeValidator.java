/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class UnionTypeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnionTypeValidator.class);

    private List<JsonValidator> schemas;
    private String error;

    public UnionTypeValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNION_TYPE, validationContext);
        schemas = new ArrayList<JsonValidator>();

        StringBuilder errorBuilder = new StringBuilder();

        String sep = "";
        errorBuilder.append('[');

        if (!schemaNode.isArray())
            throw new JsonSchemaException("Expected array for type property on Union Type Definition.");

        int i = 0;
        for (JsonNode n : schemaNode) {
            JsonType t = TypeFactory.getSchemaNodeType(n);
            errorBuilder.append(sep).append(t);
            sep = ", ";

            if (n.isObject())
                schemas.add(new JsonSchema(validationContext, ValidatorTypeCode.TYPE.getValue(), parentSchema.getCurrentUri(), n, parentSchema)
                    .initialize());
            else
                schemas.add(new TypeValidator(schemaPath + "/" + i, n, parentSchema, validationContext));

            i++;
        }

        errorBuilder.append(']');

        error = errorBuilder.toString();
    }

    public JsonNode validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);

        boolean valid = false;

        for (JsonValidator schema : schemas) {
            JsonNode errors = schema.validate(node, rootNode, at);
            if (errors == null || errors.isEmpty()) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            return constructErrorsNode(buildValidationMessage(at, nodeType.toString(), error));
        }

        return null;
    }

}
