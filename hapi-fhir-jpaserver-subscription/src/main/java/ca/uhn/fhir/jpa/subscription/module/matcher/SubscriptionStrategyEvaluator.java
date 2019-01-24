package ca.uhn.fhir.jpa.subscription.module.matcher;

import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionStrategyEvaluator {
	@Autowired
	ParamEvaluator myParamEvaluator;


	public static final String NO_RESOURCE_DEF = "unable to find resource definition";
	public static final String PARSE_FAIL = "failed to translate parse query string";
	public static final String STANDARD_PARAMETER = "standard parameters not supported";

	public SubscriptionMatchingEvaluationResult determineStrategy(RuntimeResourceDefinition theResourceDef, SearchParameterMap theSearchParameterMap) {
		theSearchParameterMap.clean();
		if (theSearchParameterMap.getLastUpdated() != null) {
			return SubscriptionMatchingEvaluationResult.databaseResultFromParameterAndReason(Constants.PARAM_LASTUPDATED, "Standard Parameters not supported");
		}

		return myParamEvaluator.evaluateParams(theResourceDef, theSearchParameterMap);
	}

}
