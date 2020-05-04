/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ConstValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConstValidator.class);
    JsonNode schemaNode;

    public ConstValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.CONST, validationContext);
        this.schemaNode = schemaNode;
    }

    public JsonNode validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        
        ArrayNode errors = objectMapper.createArrayNode();
        if (schemaNode.isNumber() && node.isNumber()) {
            if (schemaNode.decimalValue().compareTo(node.decimalValue()) != 0) {
                errors.add(constructErrorsNode(buildValidationMessage(at, schemaNode.asText())));
            }
        } else if (!schemaNode.equals(node)) {
            errors.add(constructErrorsNode(buildValidationMessage(at, schemaNode.asText())));
        }
        return errors;
    }
}
