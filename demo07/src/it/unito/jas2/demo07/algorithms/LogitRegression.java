package it.unito.jas2.demo07.algorithms;

import java.util.Map;
import java.util.Random;

//import it.zero11.microsim.data.MultiKeyCoefficientMap;
//import it.unito.jas2.demo07.algorithms.MultiKeyCoefficientMap;
import it.zero11.microsim.engine.SimulationEngine;
import it.zero11.microsim.statistics.IDoubleSource;
//import it.zero11.microsim.statistics.regression.IBinaryChoiceRegression;

public class LogitRegression extends LinearRegression implements IBinaryChoiceRegression {

	private Random random;
	
	public LogitRegression(MultiKeyCoefficientMap map) {
		super(map);
		random = SimulationEngine.getRnd();
	}

	public LogitRegression(MultiKeyCoefficientMap map, Random random) {
		super(map);		
		this.random = random;
	}
	

	public double getProbability(Map<String, Double> values) {
		final double score = super.getScore(values);		
		return (double) 1 / (1 + Math.exp(- score));
	}
	
	public double getProbability(Object individual) {
		final double score = super.getScore(individual);		
		return (double) 1 / (1 + Math.exp(- score));			
	}
	
//	@Override
	public boolean event(Object individual) {
		final double probability = getProbability(individual);
		return (random.nextDouble() < probability);		
	}

//	@Override
	public boolean event(Map<String, Double> values) {
		final double probability = getProbability(values);
		return (random.nextDouble() < probability);		
	}
	
	////////////////////////////////
	//New methods
	////////////////////////////////
	
	public <T extends Enum<T>> double getProbability(IDoubleSource iDblSrc, Class<T> enumType) {
		final double score = super.getScore(iDblSrc, enumType);		
		return (double) 1 / (1 + Math.exp(- score));			
	}
	
//	@Override
	public <T extends Enum<T>> boolean event(IDoubleSource iDblSrc, Class<T> enumType) {
		final double probability = getProbability(iDblSrc, enumType);
		return (random.nextDouble() < probability);		
	}

	public <T extends Enum<T>, U extends Enum<U>> double getProbability(IDoubleSource iDblSrc, Class<T> enumTypeDbl, IObjectSource iObjSrc, Class<U> enumTypeObj) {
		final double score = super.getScore(iDblSrc, enumTypeDbl, iObjSrc, enumTypeObj);		
		return (double) 1 / (1 + Math.exp(- score));			
	}
	
//	@Override
	public <T extends Enum<T>, U extends Enum<U>> boolean event(IDoubleSource iDblSrc, Class<T> enumTypeDbl, IObjectSource iObjSrc, Class<U> enumTypeObj) {
		final double probability = getProbability(iDblSrc, enumTypeDbl, iObjSrc, enumTypeObj);
		return (random.nextDouble() < probability);		
	}

	
}
