package it.unito.jas2.demo07.algorithms;

import it.unito.jas2.demo07.model.enums.Gender;			//XXX: This cannot be a candidate for the JAS libraries if it relies on a model specific definition of Gender!
import microsim.data.MultiKeyCoefficientMap;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;

public class MapAgeSearch {

	public static Double getValue(MultiKeyCoefficientMap map, int age, Gender gender, Integer index) {		//For use with Integer indices for the names of the value columns, such as an enum.ORDINAL or just a regular Integer variable
		for (MapIterator iterator = map.mapIterator(); iterator.hasNext();) {
			iterator.next();
			MultiKey mk = (MultiKey) iterator.getKey();
			int ageFrom = (Integer) mk.getKey(0);
			int ageTo = (Integer) mk.getKey(1);
			String g = (String) mk.getKey(2);
			
			if (age >= ageFrom && age <= ageTo && g.equalsIgnoreCase(gender.toString()))
				return  ((Number) map.getValue(ageFrom, ageTo, g, index)).doubleValue();
		}
				
		throw new IllegalArgumentException("Age " + age + " cannot be mapped for gender " + gender);
	}

	public static Double getValue(MultiKeyCoefficientMap map, int age, Gender gender, String stringIndex) {	//For use with String indices for the names of the value columns, such as an an enum.STRING or just a regular String variable 
		for (MapIterator iterator = map.mapIterator(); iterator.hasNext();) {
			iterator.next();
			MultiKey mk = (MultiKey) iterator.getKey();
			int ageFrom = (Integer) mk.getKey(0);
			int ageTo = (Integer) mk.getKey(1);
			String g = (String) mk.getKey(2);
			
			if (age >= ageFrom && age <= ageTo && g.equalsIgnoreCase(gender.toString()))
				return  ((Number) map.getValue(ageFrom, ageTo, g, stringIndex)).doubleValue();
		}
				
		throw new IllegalArgumentException("Age " + age + " cannot be mapped for gender " + gender);
	}
}