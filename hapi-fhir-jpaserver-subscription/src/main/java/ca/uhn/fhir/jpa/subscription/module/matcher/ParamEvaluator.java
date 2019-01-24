package ca.uhn.fhir.jpa.subscription.module.matcher;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.searchparam.extractor.ResourceIndexedSearchParams;
import ca.uhn.fhir.jpa.searchparam.registry.ISearchParamRegistry;
import ca.uhn.fhir.jpa.subscription.module.cache.SubscriptionMatchingStrategy;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.param.BaseParamWithPrefix;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class ParamEvaluator {
	@Autowired
	ISearchParamRegistry mySearchParamRegistry;


	public SubscriptionMatchingEvaluationResult evaluateParams(RuntimeResourceDefinition theResourceDef, SearchParameterMap theSearchParameterMap) {
		for (Map.Entry<String, List<List<? extends IQueryParameterType>>> entry : theSearchParameterMap.entrySet()) {
			String theParamName = entry.getKey();
			List<List<? extends IQueryParameterType>> andOrParams = entry.getValue();
			SubscriptionMatchingEvaluationResult subscriptionMatchingEvaluationResult = evaluateParam(theParamName, andOrParams, theResourceDef, theSearchParameterMap);
			if (subscriptionMatchingEvaluationResult.getMatchingStrategy() == SubscriptionMatchingStrategy.DATABASE) {
				return subscriptionMatchingEvaluationResult;
			}
		}
		return SubscriptionMatchingEvaluationResult.inMemoryResult();

	}

	private SubscriptionMatchingEvaluationResult evaluateParam(String theParamName, List<List<? extends IQueryParameterType>> theAndOrParams, RuntimeResourceDefinition theResourceDef, SearchParameterMap theSearchParameterMap) {
		if (theAndOrParams.isEmpty()) {
			return SubscriptionMatchingEvaluationResult.inMemoryResult();
		}

		if (hasQualifiers(theAndOrParams)) {

			return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, "Standard Parameters not supported.");

		}
		if (hasPrefixes(theAndOrParams)) {

			return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, "Prefixes not supported.");

		}
		if (hasChain(theAndOrParams)) {
			return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, "Chained references are not supported");
		}

		return evaluateAndOr(theResourceDef, theSearchParameterMap);
	}

	private SubscriptionMatchingEvaluationResult evaluateAndOr(RuntimeResourceDefinition theResourceDef, SearchParameterMap theSearchParameterMap) {
		for (Map.Entry<String, List<List<? extends IQueryParameterType>>> entry : theSearchParameterMap.entrySet()) {
			String theParamName = entry.getKey();
			List<List<? extends IQueryParameterType>> andOrParams = entry.getValue();
			SubscriptionMatchingEvaluationResult subscriptionMatchingEvaluationResult = evaluateParamWithAndOr(theParamName, andOrParams, theResourceDef, theSearchParameterMap);
			if (subscriptionMatchingEvaluationResult.getMatchingStrategy() == SubscriptionMatchingStrategy.DATABASE) {
				return subscriptionMatchingEvaluationResult;
			}
		}

		return SubscriptionMatchingEvaluationResult.inMemoryResult();
	}

	private SubscriptionMatchingEvaluationResult evaluateParamWithAndOr(String theParamName, List<List<? extends IQueryParameterType>> theAndOrParams, RuntimeResourceDefinition theResourceDef, SearchParameterMap theSearchParameterMap) {
		switch (theParamName) {
			case IAnyResource.SP_RES_ID:

				return SubscriptionMatchingEvaluationResult.inMemoryResult();

			case IAnyResource.SP_RES_LANGUAGE:

				return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, SubscriptionStrategyEvaluator.STANDARD_PARAMETER);

			case Constants.PARAM_HAS:

				return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, SubscriptionStrategyEvaluator.STANDARD_PARAMETER);

			case Constants.PARAM_TAG:
			case Constants.PARAM_PROFILE:
			case Constants.PARAM_SECURITY:

				return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, SubscriptionStrategyEvaluator.STANDARD_PARAMETER);

			default:
				String resourceName = theResourceDef.getName();
				RuntimeSearchParam paramDef = mySearchParamRegistry.getActiveSearchParam(resourceName, theParamName);
				return evaluateResourceParam(theParamName, theAndOrParams, theResourceDef, theSearchParameterMap, paramDef);
		}
	}

	private SubscriptionMatchingEvaluationResult evaluateResourceParam(String theParamName, List<List<? extends IQueryParameterType>> theAndOrParams, RuntimeResourceDefinition theResourceDef, SearchParameterMap theSearchParameterMap, RuntimeSearchParam theParamDef) {
		if (theParamDef != null) {
			switch (theParamDef.getParamType()) {
				case QUANTITY:
				case TOKEN:
				case STRING:
				case NUMBER:
				case URI:
				case DATE:
				case REFERENCE:
					return SubscriptionMatchingEvaluationResult.inMemoryResult();
				case COMPOSITE:
				case HAS:
				case SPECIAL:
				default:
					return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, SubscriptionStrategyEvaluator.STANDARD_PARAMETER);
			}
		} else {
			if (Constants.PARAM_CONTENT.equals(theParamName) || Constants.PARAM_TEXT.equals(theParamName)) {
				return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(theParamName, SubscriptionStrategyEvaluator.STANDARD_PARAMETER);
			} else {
				throw new InvalidRequestException("Unknown search parameter " + theParamName + " for resource type " + theResourceDef.getName());
			}
		}
	}

	private boolean hasChain(List<List<? extends IQueryParameterType>> theAndOrParams) {
		return theAndOrParams.stream().flatMap(List::stream).anyMatch(param -> param instanceof ReferenceParam && ((ReferenceParam) param).getChain() != null);
	}

	private boolean hasQualifiers(List<List<? extends IQueryParameterType>> theAndOrParams) {
		return theAndOrParams.stream().flatMap(List::stream).anyMatch(param -> param.getQueryParameterQualifier() != null);
	}

	private boolean hasPrefixes(List<List<? extends IQueryParameterType>> theAndOrParams) {
		Predicate<IQueryParameterType> hasPrefixPredicate = param -> param instanceof BaseParamWithPrefix &&
			((BaseParamWithPrefix) param).getPrefix() != null;
		return theAndOrParams.stream().flatMap(List::stream).anyMatch(hasPrefixPredicate);
	}
}
