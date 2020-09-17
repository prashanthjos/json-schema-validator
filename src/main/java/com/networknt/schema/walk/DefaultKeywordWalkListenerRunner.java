package com.networknt.schema.walk;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

public class DefaultKeywordWalkListenerRunner extends AbstractWalkListenerRunner {

	private Map<String, List<WalkListener>> keywordWalkListenersMap;

	public DefaultKeywordWalkListenerRunner(Map<String, List<WalkListener>> keywordWalkListenersMap) {
		this.keywordWalkListenersMap = keywordWalkListenersMap;
	}

	@Override
	public boolean runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema) {
		String keyword = getKeywordName(keyWordPath);
		boolean continueRunningListenersAndWalk = true;
		WalkEvent keywordWalkEvent = constructWalkEvent(keyword, node, rootNode, at, schemaPath, schemaNode,
				parentSchema);
		// Run Listeners that are setup only for this keyword.
		List<WalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
		continueRunningListenersAndWalk = runPreWalkListeners(currentKeywordListeners, keywordWalkEvent);
		if (continueRunningListenersAndWalk) {
			// Run Listeners that are setup for all keywords.
			List<WalkListener> allKeywordListeners = keywordWalkListenersMap
					.get(JsonSchemaFactory.ALL_KEYWORD_WALK_LISTENER_KEY);
			runPreWalkListeners(allKeywordListeners, keywordWalkEvent);
		}
		return continueRunningListenersAndWalk;
	}

	@Override
	public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema, Set<ValidationMessage> validationMessages) {
		String keyword = getKeywordName(keyWordPath);
		WalkEvent keywordWalkEvent = constructWalkEvent(keyword, node, rootNode, at, schemaPath, schemaNode,
				parentSchema);
		// Run Listeners that are setup only for this keyword.
		List<WalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
		runPostWalkListeners(currentKeywordListeners, keywordWalkEvent, validationMessages);
		// Run Listeners that are setup for all keywords.
		List<WalkListener> allKeywordListeners = keywordWalkListenersMap
				.get(JsonSchemaFactory.ALL_KEYWORD_WALK_LISTENER_KEY);
		runPostWalkListeners(allKeywordListeners, keywordWalkEvent, validationMessages);
	}

}