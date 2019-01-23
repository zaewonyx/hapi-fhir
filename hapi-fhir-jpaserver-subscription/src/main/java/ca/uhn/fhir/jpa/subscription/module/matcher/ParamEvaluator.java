package ca.uhn.fhir.jpa.subscription.module.matcher;

import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.subscription.module.cache.SubscriptionMatchingStrategy;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.BaseParamWithPrefix;
import ca.uhn.fhir.rest.param.ReferenceParam;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ParamEvaluator {
	private final SearchParameterMap mySearchParameterMap;

	public ParamEvaluator(SearchParameterMap theSearchParameterMap) {
		mySearchParameterMap = theSearchParameterMap;
	}

	public SubscriptionMatchingEvaluationResult evaluateParams() {
		for (Map.Entry<String, List<List<? extends IQueryParameterType>>> entry : mySearchParameterMap.entrySet()) {
			String theParamName = entry.getKey();
			List<List<? extends IQueryParameterType>> andOrParams = entry.getValue();
			SubscriptionMatchingEvaluationResult subscriptionMatchingEvaluationResult = evaluateParam(theParamName, andOrParams);
			if (subscriptionMatchingEvaluationResult.getMatchingStrategy() == SubscriptionMatchingStrategy.DATABASE) {
				return subscriptionMatchingEvaluationResult;
			}
		}
		return new SubscriptionMatchingEvaluationResult(mySearchParameterMap);

	}

	public SubscriptionMatchingEvaluationResult evaluateParam(String theParamName, List<List<? extends IQueryParameterType>> theAndOrParams) {
		if (theAndOrParams.isEmpty()) {
			return new SubscriptionMatchingEvaluationResult(mySearchParameterMap);
		}

		if (hasQualifiers(theAndOrParams)) {

			return new SubscriptionMatchingEvaluationResult(theParamName, "Standard Parameters not supported.");

		}
		if (hasPrefixes(theAndOrParams)) {

			return new SubscriptionMatchingEvaluationResult(theParamName, "Prefixes not supported.");

		}
		if (hasChain(theAndOrParams)) {
			return new SubscriptionMatchingEvaluationResult(theParamName, "Chained references are not supported");
		}
		return new SubscriptionMatchingEvaluationResult(mySearchParameterMap);
	}

	private static boolean hasChain(List<List<? extends IQueryParameterType>> theAndOrParams) {
		return theAndOrParams.stream().flatMap(List::stream).anyMatch(param -> param instanceof ReferenceParam && ((ReferenceParam) param).getChain() != null);
	}

	private static boolean hasQualifiers(List<List<? extends IQueryParameterType>> theAndOrParams) {
		return theAndOrParams.stream().flatMap(List::stream).anyMatch(param -> param.getQueryParameterQualifier() != null);
	}

	private static boolean hasPrefixes(List<List<? extends IQueryParameterType>> theAndOrParams) {
		Predicate<IQueryParameterType> hasPrefixPredicate = param -> param instanceof BaseParamWithPrefix &&
			((BaseParamWithPrefix) param).getPrefix() != null;
		return theAndOrParams.stream().flatMap(List::stream).anyMatch(hasPrefixPredicate);
	}

}
