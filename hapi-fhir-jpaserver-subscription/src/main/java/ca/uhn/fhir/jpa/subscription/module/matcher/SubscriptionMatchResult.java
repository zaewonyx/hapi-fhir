package ca.uhn.fhir.jpa.subscription.module.matcher;

/*-
 * #%L
 * HAPI FHIR Subscription Server
 * %%
 * Copyright (C) 2014 - 2019 University Health Network
 * %%
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
 * #L%
 */

public class SubscriptionMatchResult {
	private final boolean myMatch;
	private final boolean mySupported;
	private final String myUnsupportedParameter;
	private final String myUnsupportedReason;

	private SubscriptionMatchResult(boolean theMatch) {
		this.myMatch = theMatch;
		this.mySupported = true;
		this.myUnsupportedParameter = null;
		this.myUnsupportedReason = null;
	}

	private SubscriptionMatchResult(String theUnsupportedParameter, String theUnsupportedReason) {
		this.myMatch = false;
		this.mySupported = false;
		this.myUnsupportedParameter = theUnsupportedParameter;
		this.myUnsupportedReason = theUnsupportedReason;
	}

	private SubscriptionMatchResult(SubscriptionMatchingEvaluationResult theSubscriptionMatchingEvaluationResult) {
		this(theSubscriptionMatchingEvaluationResult.getUnsupportedParameter(), theSubscriptionMatchingEvaluationResult.getUnsupportedReason());
	}

	public static SubscriptionMatchResult unsupported(SubscriptionMatchingEvaluationResult theSubscriptionMatchingEvaluationResult) {
		return new SubscriptionMatchResult(theSubscriptionMatchingEvaluationResult);
	}

	public static SubscriptionMatchResult successfulMatch() {
		return new SubscriptionMatchResult(true);
	}

	public static SubscriptionMatchResult fromBoolean(boolean theMatched) {
		return new SubscriptionMatchResult(theMatched);
	}

	public static SubscriptionMatchResult unsupportedFromReason(String theUnsupportedReason) {
		return new SubscriptionMatchResult(null, theUnsupportedReason);
	}

	public static SubscriptionMatchResult unsupportedFromParameterAndReason(String theUnsupportedParameter, String theUnsupportedReason) {
		return new SubscriptionMatchResult(theUnsupportedParameter, theUnsupportedReason);
	}

	public boolean supported() {
		return mySupported;
	}

	public boolean matched() {
		return myMatch;
	}

	public String getUnsupportedReason() {
		if (myUnsupportedParameter != null) {
			return "Parameter: <" + myUnsupportedParameter + "> Reason: " + myUnsupportedReason;
		}
		return myUnsupportedReason;
	}
}
