package ca.uhn.fhir.jpa.subscription.module.matcher;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.jpa.model.search.QueryParser;
import ca.uhn.fhir.jpa.searchparam.MatchUrlService;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.searchparam.registry.ISearchParamRegistry;
import ca.uhn.fhir.jpa.subscription.module.cache.SubscriptionMatchingStrategy;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.api.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SubscriptionStrategyEvaluator {
	private static final String CRITERIA = "CRITERIA";

	@Autowired
	private MatchUrlService myMatchUrlService;
	@Autowired
	ISearchParamRegistry mySearchParamRegistry;
	@Autowired
	FhirContext myFhirContext;

	public SubscriptionMatchingEvaluationResult determineStrategy(String theCriteria) {
		RuntimeResourceDefinition resourceDef = QueryParser.parseUrlResourceType(myFhirContext, theCriteria);

		if (resourceDef == null) {
			return new SubscriptionMatchingEvaluationResult("CRITERIA");
		}

		SearchParameterMap searchParameterMap;
		try {
			searchParameterMap = myMatchUrlService.translateMatchUrl(theCriteria, resourceDef);
		} catch (UnsupportedOperationException e) {
			return new SubscriptionMatchingEvaluationResult(CRITERIA);
		}
		searchParameterMap.clean();
		if (searchParameterMap.getLastUpdated() != null) {
			return new SubscriptionMatchingEvaluationResult(Constants.PARAM_LASTUPDATED, "Standard Parameters not supported");
		}

		ParamEvaluator paramEvaluator = new ParamEvaluator(searchParameterMap);
		return paramEvaluator.evaluateParams();
	}

}
