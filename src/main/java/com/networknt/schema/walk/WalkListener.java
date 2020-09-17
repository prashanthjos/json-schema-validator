package com.networknt.schema.walk;

import java.util.Set;

import com.networknt.schema.ValidationMessage;

/**
 * 
 * Listener class that captures walkStart and walkEnd events.
 *
 */
public interface WalkListener {

	public boolean onWalkStart(WalkEvent walkEvent);

	public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages);
}