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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.jpa.searchparam.MatchUrlService;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.searchparam.extractor.ResourceIndexedSearchParams;
import ca.uhn.fhir.jpa.searchparam.registry.ISearchParamRegistry;
import ca.uhn.fhir.jpa.subscription.module.cache.SubscriptionMatchingStrategy;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.param.BaseParamWithPrefix;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class CriteriaResourceMatcher {
	// FIXME KHS one place for this (currently two)
	private static final String CRITERIA = "CRITERIA";

	@Autowired
	ISearchParamRegistry mySearchParamRegistry;
	@Autowired
	SubscriptionStrategyEvaluator mySubscriptionStrategyEvaluator;
	@Autowired
	FhirContext myFhirContext;

	public SubscriptionMatchResult match(String theCriteria, IBaseResource theResource, ResourceIndexedSearchParams theSearchParams) {

		SubscriptionMatchingEvaluationResult subscriptionMatchingEvaluationResult = mySubscriptionStrategyEvaluator.determineStrategy(theCriteria);
		if (subscriptionMatchingEvaluationResult.getMatchingStrategy() == SubscriptionMatchingStrategy.DATABASE) {
			return new SubscriptionMatchResult(subscriptionMatchingEvaluationResult);
		}

		return getSubscriptionMatchResult(theResource, theSearchParams, subscriptionMatchingEvaluationResult.getSearchParameterMap());
	}

	private SubscriptionMatchResult getSubscriptionMatchResult(IBaseResource theResource, ResourceIndexedSearchParams theSearchParams, SearchParameterMap theSearchParameterMap) {
		for (Map.Entry<String, List<List<? extends IQueryParameterType>>> entry : theSearchParameterMap.entrySet()) {
			String theParamName = entry.getKey();
			List<List<? extends IQueryParameterType>> andOrParams = entry.getValue();
			SubscriptionMatchResult result = matchIdsWithAndOr(theParamName, andOrParams, theResource, theSearchParams);
			if (!result.matched()){
				return result;
			}
		}
		return new SubscriptionMatchResult(true, CRITERIA);
	}

	// This method is modelled from SearchBuilder.searchForIdsWithAndOr()
	private SubscriptionMatchResult matchIdsWithAndOr(String theParamName, List<List<? extends IQueryParameterType>> theAndOrParams, IBaseResource theResource, ResourceIndexedSearchParams theSearchParams) {
		switch (theParamName) {
			case IAnyResource.SP_RES_ID:

				return new SubscriptionMatchResult(matchIdsAndOr(theAndOrParams, theResource), CRITERIA);

			case IAnyResource.SP_RES_LANGUAGE:

				return new SubscriptionMatchResult(theParamName, CRITERIA);

			case Constants.PARAM_HAS:

				return new SubscriptionMatchResult(theParamName, CRITERIA);

			case Constants.PARAM_TAG:
			case Constants.PARAM_PROFILE:
			case Constants.PARAM_SECURITY:

				return new SubscriptionMatchResult(theParamName, CRITERIA);

			default:

				String resourceName = myFhirContext.getResourceDefinition(theResource).getName();
				RuntimeSearchParam paramDef = mySearchParamRegistry.getActiveSearchParam(resourceName, theParamName);
				return matchResourceParam(theParamName, theAndOrParams, theSearchParams, resourceName, paramDef);
		}
	}

	private boolean matchIdsAndOr(List<List<? extends IQueryParameterType>> theAndOrParams, IBaseResource theResource) {
		return theAndOrParams.stream().allMatch(nextAnd -> matchIdsOr(nextAnd, theResource));
	}
	private boolean matchIdsOr(List<? extends IQueryParameterType> theOrParams, IBaseResource theResource) {
		return theOrParams.stream().anyMatch(param -> param instanceof StringParam && matchId(((StringParam)param).getValue(), theResource.getIdElement()));
	}

	private boolean matchId(String theValue, IIdType theId) {
		return theValue.equals(theId.getValue()) || theValue.equals(theId.getIdPart());
	}

	private SubscriptionMatchResult matchResourceParam(String theParamName, List<List<? extends IQueryParameterType>> theAndOrParams, ResourceIndexedSearchParams theSearchParams, String theResourceName, RuntimeSearchParam theParamDef) {
		if (theParamDef != null) {
			switch (theParamDef.getParamType()) {
				case QUANTITY:
				case TOKEN:
				case STRING:
				case NUMBER:
				case URI:
				case DATE:
				case REFERENCE:
					return new SubscriptionMatchResult(theAndOrParams.stream().anyMatch(nextAnd -> matchParams(theResourceName, theParamName, theParamDef, nextAnd, theSearchParams)), CRITERIA);
				case COMPOSITE:
				case HAS:
				case SPECIAL:
				default:
					return new SubscriptionMatchResult(theParamName, CRITERIA);
			}
		} else {
			if (Constants.PARAM_CONTENT.equals(theParamName) || Constants.PARAM_TEXT.equals(theParamName)) {
				return new SubscriptionMatchResult(theParamName, CRITERIA);
			} else {
				throw new InvalidRequestException("Unknown search parameter " + theParamName + " for resource type " + theResourceName);
			}
		}
	}

	private boolean matchParams(String theResourceName, String theParamName, RuntimeSearchParam paramDef, List<? extends IQueryParameterType> theNextAnd, ResourceIndexedSearchParams theSearchParams) {
		return theNextAnd.stream().anyMatch(token -> theSearchParams.matchParam(theResourceName, theParamName, paramDef, token));
	}

}
