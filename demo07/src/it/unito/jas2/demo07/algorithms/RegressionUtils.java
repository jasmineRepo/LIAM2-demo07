package it.unito.jas2.demo07.algorithms;

import java.util.Random;

import it.zero11.microsim.engine.SimulationEngine;

public class RegressionUtils {

	public static <T> T event(Class<T> eventClass, double[] prob) {
		return event(eventClass.getEnumConstants(), prob, SimulationEngine.getRnd());		
	}
	
	/**
	 * You must provide a vector of events (any type of object) and relative
	 * weights which sum must be equal to 1.0.
	 * 
	 * The function toss a random double number and search in witch probability
	 * range the sampled number is within to select the corresponding event.
	 * 
	 * @param events
	 * @param prob
	 * @return
	 */
	public static <T> T event(T[] events, double[] prob) {
		return event(events, prob, SimulationEngine.getRnd());		
	}
	
	public static <T> T event(T[] events, double[] prob, Random rnd) {
		double x = 0.0;
		for (int i = 0; i < prob.length; i++) {
			x += prob[i];
		}
		
		if (x != 1.0) 
			throw new IllegalArgumentException("Choice's weights must sum 1.0. Current vector" + prob + " sums " + x);
		
		double toss = rnd.nextDouble();
		
		x = 0.0;
		int i = 0;
		while (toss >= x)
		{
			x += prob[i];
			i++;					
		}
		
		return events[i-1];			
	}
	
	public static boolean event(double prob) {
		return SimulationEngine.getRnd().nextDouble() < prob;		
	}
	
	public static boolean event(double prob, Random rnd) {
		return rnd.nextDouble() < prob;
	}
}
