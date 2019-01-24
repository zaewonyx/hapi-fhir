package ca.uhn.fhir.jpa.subscription.module.matcher;

import ca.uhn.fhir.jpa.subscription.module.BaseSubscriptionDstu3Test;
import ca.uhn.fhir.jpa.subscription.module.cache.SubscriptionMatchingStrategy;
import org.hl7.fhir.dstu3.model.Subscription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class SubscriptionCriteriaStrategyEvaluatorTest extends BaseSubscriptionDstu3Test {
	@Autowired
	SubscriptionStrategyEvaluator mySubscriptionStrategyEvaluator;

	@Test
	public void testInMemory() {
		Subscription subscription = new Subscription();
		subscription.setCriteria("Observation?");

		// FIXME KHS
//		assertEquals(SubscriptionMatchingStrategy.IN_MEMORY, mySubscriptionStrategyEvaluator.determineStrategy(resourceDef, subscription.getCriteria()));
	}

}
