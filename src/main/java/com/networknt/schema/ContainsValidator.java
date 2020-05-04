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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ContainsValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ContainsValidator.class);

    private JsonSchema schema;

    public ContainsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.CONTAINS, validationContext);
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            schema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), schemaNode, parentSchema)
                .initialize();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public JsonNode validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);


        if (!node.isArray()) {
            // ignores non-arrays
            return null;
        }

        if (node.isEmpty()) {
            // Array was empty
            return buildErrorMessageSet(at);
        } else if (node.isArray()) {
            int i = 0;
            for (JsonNode n : node) {
                if (schema.validate(n, rootNode, at + "[" + i + "]").isEmpty()) {
                    //Short circuit on first success
                    return null;
                }
                i++;
            }
            // None of the elements in the array satisfies the schema
            return buildErrorMessageSet(at);
        }

        return null;
    }

	private JsonNode buildErrorMessageSet(String at) {
		return constructErrorsNode(buildValidationMessage(at, schema.getSchemaNode().toString()));
	}

}
