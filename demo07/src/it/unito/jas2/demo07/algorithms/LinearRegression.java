package it.unito.jas2.demo07.algorithms;

//import it.zero11.microsim.data.MultiKeyCoefficientMap;
//import it.unito.jas2.demo07.algorithms.MultiKeyCoefficientMap;
import it.zero11.microsim.statistics.IDoubleSource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.keyvalue.MultiKey;

public class LinearRegression implements ILinearRegression {
	
	private MultiKeyCoefficientMap map = null;
	
	/**
	 * Requires the first column entry of the MultiKeyCoefficientMap (i.e. the first key of the multiKey) to be the name of the regressor variables.
	 * @param map
	 */
	public LinearRegression(MultiKeyCoefficientMap map) {
		this.map = map;
	}

	private static Map<String, Double> extractMapNumbersAndBooleans(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, Double> resultMap = new HashMap<String, Double>();
		
		Map<?, ?> describedData = PropertyUtils.describe(object);
		
		for (Iterator<?> iterator = describedData.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Object value = describedData.get(key);
			if(value != null)
			{
				if (value.getClass().equals(Double.class)) {
					final Double r = (Double) value;
					resultMap.put(key, (r != null ? r : 0.0));				
				} else if (value.getClass().equals(Float.class)) {
					final Float r = (Float) value;
					resultMap.put(key, ((Float)(r != null ? r : 0.0f)).doubleValue());				
				} else if (value.getClass().equals(Long.class)) {
					final Long r = (Long) value;
					resultMap.put(key, ((Long)(r != null ? r : 0L)).doubleValue());				
				} else if (value.getClass().equals(Integer.class)) {
					Integer r = (Integer) value;
					resultMap.put(key, ((Integer)(r != null ? r : 0)).doubleValue());				
				} else if (value.getClass().equals(Boolean.class)) {
					Boolean r = (Boolean) value;
					boolean b = (Boolean)(r != null ? r : false);
					resultMap.put(key, (b ? 1.0 : 0.0));				
				}
			} 
		}
		
		return resultMap;
	}
	
//	@Override
	public double getScore(Object indivdual) {
		return computeScore(map, indivdual);
	}
	
//	public double getScore(Map<String, Double> values) {
//		return computeScore(map, values);
//	}


//Deprecated - does not work properly with MultiKeyCoefficientMaps.  Has been replaced with method (which has same signature) below.
//	public static double computeScore(MultiKeyCoefficientMap amap, Object indivdual) {		
//		try {
//			final Map<String, Double> valueMap = extractMapNumbersAndBooleans(indivdual);
//			return computeScore(amap, valueMap);			
//		} catch (IllegalArgumentException e) {
//			System.err.println(e.getMessage());
//			return 0;
//		} catch (IllegalAccessException e) {
//			System.err.println(e.getMessage());
//			return 0;
//		} catch (InvocationTargetException e) {
//			System.err.println(e.getMessage());
//			return 0;
//		} catch (NoSuchMethodException e) {
//			System.err.println(e.getMessage());
//			return 0;
//		}
//	}
	
	
	
	
//	/**
//	 *
//	 * Warning - this method only looks at the first key of the MultiKeyCoefficientMap amap, so any other keys that are used to distinguish a unique multiKey (i.e. if the first key occurs more than once) will be ignored!
//	 * This should not accept a MultiKeyCoefficientMap as an argument if it doesn't use these properties! Should use the other method computeScore(Map<String, Double>, Map<String, Double>) instead!!!
//	 * @param amap
//	 * @param values
//	 * @return
//	 */
//	public static double computeScore(MultiKeyCoefficientMap amap, Map<String, Double> values) {
//		double sum = 0.0;
//		try {
//			for (Object multiKey : amap.keySet()) {
//				final String key = (String) ((MultiKey) multiKey).getKey(0);
//				
//				if (key.contains("@"))
//					sum += (Double) (amap.getValue(key) == null ? 0.0 : amap.getValue(key));
//				else
//					sum += (Double) (amap.getValue(key) == null ? 0.0 : amap.getValue(key)) * (Double) (values.get(key) == null ? 0.0 : values.get(key));
//			}
//			return sum;
//		} catch (IllegalArgumentException e) {
//			System.err.println(e.getMessage());
//			return 0;
//		} 
//	}

	/**
	 *
	 * Warning - only use when LinearRegression object contains a MultiKeyCoefficientMap with only one key.  This method only looks at the first key of the MultiKeyCoefficientMap field of LinearRegression, so any other keys that are used to distinguish a unique multiKey (i.e. if the first key occurs more than once) will be ignored! If the first key of the multiKey appears more than once, the method would return an incorrect value, so will throw an exception.   
	 * @param values
	 * @return
	 */
//	@Override
	public double getScore(Map<String, Double> values) {
		double sum = 0.0;
		HashSet<String> regressors = new HashSet<String>();
		
		try {
			for (Object multiKey : map.keySet()) {
				final String key = (String) ((MultiKey) multiKey).getKey(0);
				if(!regressors.add(key)) {
					throw new IllegalArgumentException("Regressor key " + key + " is not unique!  The LinearRegression will not return the correct value when LinearRegression.getScore(Map<String,Double>) is used with a LinearRegression instance containing a MultiKeyCoefficientMap with more than one key!  Consider using LineraRegression.getScore(Object) instead.");
				}
			}
			
			for (String key : regressors) {
				if (key.contains("@"))
					sum += (Double) (map.getValue(key) == null ? 0.0 : map.getValue(key));
				else
					sum += (Double) (map.getValue(key) == null ? 0.0 : map.getValue(key)) * (Double) (values.get(key) == null ? 0.0 : values.get(key));
			}
			return sum;
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			return 0;
		} 
	}

	//////////////////////////////////
	// New methods
	// @author Ross Richardson
	//////////////////////////////////
	
	private static Map<String, String> extractMapNumbersBooleansEnumsAndStrings(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, String> resultMap = new HashMap<String, String>();
		
		Map<?, ?> describedData = PropertyUtils.describe(object);
		
		for (Iterator<?> iterator = describedData.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Object value = describedData.get(key);
			if(value != null)
			{
				if (value.getClass().equals(Double.class)) {
					final Double r = (Double) value;
					resultMap.put(key, ((Double)(r != null ? r : 0.0)).toString());				
				} else if (value.getClass().equals(Float.class)) {
					final Float r = (Float) value;
					resultMap.put(key, ((Float)(r != null ? r : 0.0f)).toString());				
				} else if (value.getClass().equals(Long.class)) {
					final Long r = (Long) value;
					resultMap.put(key, ((Long)(r != null ? r : 0L)).toString());				
				} else if (value.getClass().equals(Integer.class)) {
					Integer r = (Integer) value;
					resultMap.put(key, ((Integer)(r != null ? r : 0)).toString());				
				} else if (value.getClass().equals(Boolean.class)) {
					Boolean r = (Boolean) value;
					boolean b = (Boolean)(r != null ? r : false);
					resultMap.put(key, (b ? "true" : "false"));				
				} else if (value.getClass().isEnum()) {
					final String e = value.toString();
					resultMap.put(key, e);								
				} else if (value.getClass().equals(String.class)) {
					final String s = (String) value;
					resultMap.put(key, s);	
				}
			} 
		}
		
		return resultMap;
	}
	
//	@Override
	public <T extends Enum<T>> double getScore(IDoubleSource iDblSrc, Class<T> enumType) {
		return computeScore(map, iDblSrc, enumType);
	}	
	
	/**
	 * Requires the first column entry of the MultiKeyCoefficientMap (i.e. the first entry of coeffMultiMap's multiKey) to be the name of the regressor variables.  
	 * The names of the other keys of the coeffMultiMap must match the (case sensitive) name of the corresponding fields of the iDblSrc class. 
	 * @param coeffMultiMap is a MultiKeyCoefficientMap that has a MultiKey whose first Key is the name of the regressor variable.  The names of the other keys of the coeffMultiMap must match the (case sensitive) name of the corresponding fields of the iDblSrc class.
	 * @param iDblSrc is an object that implements the IDoubleSource interface, and hence has a method getDoubleValue(enum), where the enum determines the appropriate double value to return.  It must have some fields that match the (case sensitive) name of the keys of coeffMultiMap's MultiKey
	 * @param enumType specifies the enum type that is used in the getDoubleValue(Enum.valueOf(enumType, String)) method of the iDblSrc object.  The String is the name of the enum case, used as a switch to determine the appropriate double value to return
	 * @author Ross Richardson  
	 */
	public static <T extends Enum<T>> double computeScore(MultiKeyCoefficientMap coeffMultiMap, IDoubleSource iDblSrc, Class<T> enumType) 
	{		
		try {
			final Map<String, Double> valueMap = new HashMap<String, Double>();  			//Will contains the values of the regressor variables from the object implementing IDoubleSource interface
			final Map<String, Double> regCoeffMap = new HashMap<String, Double>();			
			
			final Map<String, String> propertiesMap = extractMapNumbersBooleansEnumsAndStrings(iDblSrc);
			Vector<String> attributesVector = new Vector<String>();		
			
			int iMax = coeffMultiMap.getKeys().length;
			for (int i = 1; i < iMax; i++)	//Ignore first entry of multiKey, as this corresponds to "regressors" column that does not have an associated field in iDblSrc
			{	
				String str = coeffMultiMap.getKeys()[i];
				if(propertiesMap.containsKey(str)) {
					attributesVector.add(propertiesMap.get(str));		//Gets the name of members of the iDblSrc that match the entries in the MultiKey of coeffMultiMap.  No need to worry about duplicate entries as propertiesMap cannot have duplicates
				}
				else throw new NoSuchFieldException("Error in Regression: Could not find LinearRegression.map key named \'" + str + "\' among the fields of the iDoubleSource argument to computeScore(MultiKeyCoefficientMap, IDoubleSource, Class<T>).  Check the character cases of \'" + str + "\' match, if the field exists in object implementing iDoubleSource, to ensure they match.");
						//TODO: Get simulation to stop when this exception is thrown.
			}	
						
			HashSet<String> regressors = new HashSet<String>();
			for(Object multiKey : coeffMultiMap.keySet())
			{
				regressors.add((String) ((MultiKey) multiKey).getKey(0)); 
			}
			
			for(String regressor : regressors) {
				attributesVector.insertElementAt(regressor, 0);								//Inserts the regressor variable at the front of the vector, to preserve the correct ordering of the multiKey
				MultiKey mk = new MultiKey(attributesVector.toArray());				//Creates an Object[1], however coeffMultiMap expects an Object[3] as there are 3 keys (the actual entries as opposed to the MultiKey itself).  So try passing attributesVector.toArray() directly into the getValue function...			
				if(coeffMultiMap.containsKey(mk))		//Need to check that the multiKey exists in the coeffMultiMap, as it is possible to construct an attributesVector with entries that do not exist in the coeffMultiMap if there is a case where there is no entry for a specific Key(0) for the other attributes in Key(1) or Key(2) etc.  This is because Key(0) is currently taken from scanning through all the entries of the keySet of coeffMultiMap, and it is possible to combine this with Key(1), Key(2) etc. that are taken from the iDblSrc properties.  But no MultiKey with these values may exist in the coeffMultiMap!  
				{
					double coeff = (Double)coeffMultiMap.getValue(attributesVector.toArray());
					//Gets the relevant coefficient of the MultiKeyCoefficient map, so even though ther first key in the MultiKey may not be unique (i.e. MultiKey.get(0)), the appropriate value of the MultiKeyCoefficientMap is distinguished by the rest of the keys, i.e. MultiKey.get(1), MultiKey.get(2), etc. (if they exist)
					regCoeffMap.put(regressor, coeff);

					double value = iDblSrc.getDoubleValue(Enum.valueOf(enumType, regressor));		//Gets value of variable called "key" from object implementing IDoubleSource interface
					valueMap.put(regressor, value);
				}
				attributesVector.remove(0);				
			}
						
			double sum = 0.0;
			for (String key : regCoeffMap.keySet()) {
				if (key.contains("@"))
					sum += (Double) (regCoeffMap.get(key) == null ? 0.0 : regCoeffMap.get(key));
				else
					sum += (Double) (regCoeffMap.get(key) == null ? 0.0 : regCoeffMap.get(key)) * (Double) (valueMap.get(key) == null ? 0.0 : valueMap.get(key));
			}
			return sum;
//			return multiplyCoeffsWithValues(regCoeffMap, valueMap);			//TODO: Test that we can delegate to multiplyCoeffsWithValues()

		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (IllegalAccessException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (InvocationTargetException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (NoSuchMethodException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (NoSuchFieldException e) {
			System.err.println(e.getMessage());
			return 0;
		}
	}
			
	//New version to make compatible with MultiKeyCoefficientMaps
	public static double computeScore(MultiKeyCoefficientMap coeffMultiMap, Object indivdual) {		
		try {
			final Map<String, String> propertiesMap = extractMapNumbersBooleansEnumsAndStrings(indivdual);		//Extracts all properties from the individual, to determine the correct multiKey to use in the MultiKeyCoefficientMap that holds the regression coefficients
			final Map<String, Double> valueMap = extractMapNumbersAndBooleans(indivdual);						//Extracts numerical properties from the individual, which can be the regressor values.  TODO: Is there a way we can make this more efficient by get numerical values from the propertiesMap above? 
			final Map<String, Double> regCoeffMap = new HashMap<String, Double>();			
			
			Vector<String> attributesVector = new Vector<String>();		
			
			int iMax = coeffMultiMap.getKeys().length;
			for (int i = 1; i < iMax; i++)	//Ignore first entry of multiKey, as this corresponds to "regressors" column that does not have an associated field in individual
			{	
				String str = coeffMultiMap.getKeys()[i];
				if(propertiesMap.containsKey(str)) {
					attributesVector.add(propertiesMap.get(str));		//Gets the name of members of the individual that match the entries in the MultiKey of coeffMultiMap.  No need to worry about duplicate entries as propertiesMap cannot have duplicates
				}
				else throw new NoSuchFieldException("Error in Regression: Could not find LinearRegression.map key named \'" + str + "\' among the fields of the Object argument to computeScore(MultiKeyCoefficientMap, Object).  Check the character cases of \'" + str + "\' match, if the field exists in object, to ensure they match.");
						//TODO: Get simulation to stop when this exception is thrown.
			}	
						
			HashSet<String> regressors = new HashSet<String>();
			for(Object multiKey : coeffMultiMap.keySet())
			{
				regressors.add((String) ((MultiKey) multiKey).getKey(0)); 
			}
			
			for(String regressor : regressors) {
				attributesVector.insertElementAt(regressor, 0);								//Inserts the regressor variable at the front of the vector, to preserve the correct ordering of the multiKey
				MultiKey mk = new MultiKey(attributesVector.toArray());				//Creates an Object[1], however coeffMultiMap expects an Object[3] as there are 3 keys (the actual entries as opposed to the MultiKey itself).  So try passing attributesVector.toArray() directly into the getValue function...			
				if(coeffMultiMap.containsKey(mk))		//Need to check that the multiKey exists in the coeffMultiMap, as it is possible to construct an attributesVector with entries that do not exist in the coeffMultiMap if there is a case where there is no entry for a specific Key(0) for the other attributes in Key(1) or Key(2) etc.  This is because Key(0) is currently taken from scanning through all the entries of the keySet of coeffMultiMap, and it is possible to combine this with Key(1), Key(2) etc. that are taken from the iDblSrc properties.  But no MultiKey with these values may exist in the coeffMultiMap!  
				{
					double coeff = (Double)coeffMultiMap.getValue(attributesVector.toArray());
					//Gets the relevant coefficient of the MultiKeyCoefficient map, so even though ther first key in the MultiKey may not be unique (i.e. MultiKey.get(0)), the appropriate value of the MultiKeyCoefficientMap is distinguished by the rest of the keys, i.e. MultiKey.get(1), MultiKey.get(2), etc. (if they exist)
					regCoeffMap.put(regressor, coeff);
				}
				attributesVector.remove(0);				
			}	
			
			return multiplyCoeffsWithValues(regCoeffMap, valueMap);			
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (IllegalAccessException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (InvocationTargetException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (NoSuchMethodException e) {
			System.err.println(e.getMessage());
			return 0;
		} catch (NoSuchFieldException e) {
			System.err.println(e.getMessage());
			return 0;
		}
	}
	
	public static double multiplyCoeffsWithValues(Map<String, Double> regCoeffMap, Map<String, Double> valueMap) {
		
		double sum = 0.0;
		try {
			for (String key : regCoeffMap.keySet()) {
				if (key.contains("@"))
					sum += (Double) (regCoeffMap.get(key) == null ? 0.0 : regCoeffMap.get(key));
				else
					sum += (Double) (regCoeffMap.get(key) == null ? 0.0 : regCoeffMap.get(key)) * (Double) (valueMap.get(key) == null ? 0.0 : valueMap.get(key));
			}
			return sum;

		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			return 0;
		} 
	}

	
}
