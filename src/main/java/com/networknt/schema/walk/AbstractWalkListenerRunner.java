package com.networknt.schema.walk;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

public abstract class AbstractWalkListenerRunner implements WalkListenerRunner {

	protected String getKeywordName(String keyWordPath) {
		return keyWordPath.substring(keyWordPath.lastIndexOf('/') + 1);
	}

	protected WalkEvent constructWalkEvent(String keyWordName, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema) {
		return WalkEvent.builder().at(at).keyWordName(keyWordName).node(node).parentSchema(parentSchema)
				.rootNode(rootNode).schemaNode(schemaNode).schemaPath(schemaPath).build();
	}

	protected boolean runPreWalkListeners(List<WalkListener> walkListeners, WalkEvent walkEvent) {
		boolean continueRunningListenersAndWalk = true;
		if (walkListeners != null) {
			for (WalkListener walkListener : walkListeners) {
				if (!walkListener.onWalkStart(walkEvent)) {
					continueRunningListenersAndWalk = false;
					break;
				}
			}
		}
		return continueRunningListenersAndWalk;
	}

	protected void runPostWalkListeners(List<WalkListener> walkListeners, WalkEvent walkEvent,
			Set<ValidationMessage> validationMessages) {
		if (walkListeners != null) {
			for (WalkListener walkListener : walkListeners) {
				walkListener.onWalkEnd(walkEvent, validationMessages);
			}
		}
	}
}