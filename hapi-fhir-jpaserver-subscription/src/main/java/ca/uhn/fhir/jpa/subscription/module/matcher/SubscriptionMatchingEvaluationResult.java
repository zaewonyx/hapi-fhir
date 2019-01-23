package ca.uhn.fhir.jpa.subscription.module.matcher;

import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.subscription.module.cache.SubscriptionMatchingStrategy;

public class SubscriptionMatchingEvaluationResult {
	private final SubscriptionMatchingStrategy myMatchingStrategy;
	private final String myUnsupportedParameter;
	private final String myMatcherShortName;

	private final SearchParameterMap mySearchParameterMap;

	public SubscriptionMatchingEvaluationResult(String theMatcherShortName) {
		this(null, theMatcherShortName);
	}

	public SubscriptionMatchingEvaluationResult(String theUnsupportedParameter, String theMatcherShortName) {
		myMatchingStrategy = SubscriptionMatchingStrategy.DATABASE;
		myUnsupportedParameter = theUnsupportedParameter;
		myMatcherShortName = theMatcherShortName;
		mySearchParameterMap = null;
	}

	public SubscriptionMatchingEvaluationResult(SearchParameterMap theSearchParameterMap) {
		myMatchingStrategy = SubscriptionMatchingStrategy.IN_MEMORY;
		myUnsupportedParameter = null;
		myMatcherShortName = null;
		mySearchParameterMap = theSearchParameterMap;
	}

	public SubscriptionMatchingStrategy getMatchingStrategy() {
		return myMatchingStrategy;
	}

	public String getUnsupportedParameter() {
		return myUnsupportedParameter;
	}

	public String getMatcherShortName() {
		return myMatcherShortName;
	}

	public SearchParameterMap getSearchParameterMap() {
		return mySearchParameterMap;
	}
}
