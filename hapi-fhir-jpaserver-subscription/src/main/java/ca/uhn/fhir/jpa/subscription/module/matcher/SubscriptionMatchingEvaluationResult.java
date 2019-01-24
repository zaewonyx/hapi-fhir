package ca.uhn.fhir.jpa.subscription.module.matcher;

import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.subscription.module.cache.SubscriptionMatchingStrategy;

public class SubscriptionMatchingEvaluationResult {
	private final SubscriptionMatchingStrategy myMatchingStrategy;
	private final String myUnsupportedParameter;

	private final String myUnsupportedReason;

	private SubscriptionMatchingEvaluationResult(String theUnsupportedParameter, String theUnsupportedReason) {
		myMatchingStrategy = SubscriptionMatchingStrategy.DATABASE;
		myUnsupportedParameter = theUnsupportedParameter;
		myUnsupportedReason = theUnsupportedReason;
	}

	private SubscriptionMatchingEvaluationResult() {
		myMatchingStrategy = SubscriptionMatchingStrategy.IN_MEMORY;
		myUnsupportedParameter = null;
		myUnsupportedReason = null;
	}

	public static SubscriptionMatchingEvaluationResult databaseResultFromParameter(String theUnsupportedParameter) {
		return databaseResultFromParameterAndReason(theUnsupportedParameter, "Parameter not supported");
	}

	public static SubscriptionMatchingEvaluationResult inMemoryResult() {
		return new SubscriptionMatchingEvaluationResult();
	}

	public static SubscriptionMatchingEvaluationResult databaseResultFromReason(String theUnsupportedReason) {
		return databaseResultFromParameterAndReason("", theUnsupportedReason);
	}

	public static SubscriptionMatchingEvaluationResult databaseResultFromParameterAndReason(String theUnsupportedParameter, String theUnsupportedReason) {
		return new SubscriptionMatchingEvaluationResult(theUnsupportedParameter, theUnsupportedReason);
	}

	public SubscriptionMatchingStrategy getMatchingStrategy() {
		return myMatchingStrategy;
	}

	public String getUnsupportedParameter() {
		return myUnsupportedParameter;
	}

	// FIXME KHS call this in log
	public String getUnsupportedReason() {
		return myUnsupportedReason;
	}
}
