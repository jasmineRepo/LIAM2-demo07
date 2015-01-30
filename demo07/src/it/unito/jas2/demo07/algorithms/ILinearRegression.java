package it.unito.jas2.demo07.algorithms;

import java.util.Map;

public interface ILinearRegression {
	
	double getScore(Object individual);
		
	double getScore(Map<String, Double> values);
	
}
