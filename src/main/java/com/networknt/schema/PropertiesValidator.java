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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PropertiesValidator extends BaseJsonValidator implements JsonValidator {
    public static final String PROPERTY = "properties";
    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidator.class);
    private Map<String, JsonSchema> schemas;

    public PropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTIES, validationContext);
        schemas = new HashMap<String, JsonSchema>();
        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            schemas.put(pname, new JsonSchema(validationContext, schemaPath + "/" + pname, parentSchema.getCurrentUri(), schemaNode.get(pname), parentSchema)
                .initialize());
        }
    }

    public JsonNode validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        ArrayNode errors = objectMapper.createArrayNode();

        // get the Validator state object storing validation data
        ValidatorState state = validatorState.get();
        if (state == null) {
            // if one has not been created, instantiate one
            state = new ValidatorState();
            validatorState.set(state);
        }

        for (Map.Entry<String, JsonSchema> entry : schemas.entrySet()) {
            JsonSchema propertySchema = entry.getValue();
            JsonNode propertyNode = node.get(entry.getKey());

            if (propertyNode != null) {
                // check whether this is a complex validator. save the state
                boolean isComplex = state.isComplexValidator();
                // if this is a complex validator, the node has matched, and all it's child elements, if available, are to be validated
                if (state.isComplexValidator()) {
                    state.setMatchedNode(true);
                }
                // reset the complex validator for child element validation, and reset it after the return from the recursive call
                state.setComplexValidator(false);

                //validate the child element(s)
                errors.add(propertySchema.validate(propertyNode, rootNode, at + "." + entry.getKey()));

                // reset the complex flag to the original value before the recursive call
                state.setComplexValidator(isComplex);
                // if this was a complex validator, the node has matched and has been validated
                if (state.isComplexValidator()) {
                    state.setMatchedNode(true);
                }
            } else {
                // check whether the node which has not matched was mandatory or not
                if (getParentSchema().hasRequiredValidator()) {
                    JsonNode requiredErrors = getParentSchema().getRequiredValidator().validate(node, rootNode, at);

                    if (!requiredErrors.isEmpty()) {
                        // the node was mandatory, decide which behavior to employ when validator has not matched
                        if (state.isComplexValidator()) {
                            // this was a complex validator (ex oneOf) and the node has not been matched
                            state.setMatchedNode(false);
                            return null;
                        } else {
                            errors.add(requiredErrors);
                        }
                    }
                }
            }
        }

        return errors;
    }

}
