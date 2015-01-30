package it.unito.jas2.demo07.algorithms;

//import it.zero11.microsim.statistics.regression.ILinearRegression;

import java.util.Map;

public interface IBinaryChoiceRegression extends ILinearRegression {
	
	boolean event(Object individual);

	boolean event(Map<String, Double> values);

}
