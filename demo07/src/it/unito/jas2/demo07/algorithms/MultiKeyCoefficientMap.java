package it.unito.jas2.demo07.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.AbstractHashedMap;
import org.apache.commons.collections.map.MultiKeyMap;

public class MultiKeyCoefficientMap extends MultiKeyMap {

	private static final long serialVersionUID = 5049597007431364596L;

	protected String[] keys;
	protected Map<String, Integer> valuesMap;
	
	public MultiKeyCoefficientMap(String[] keys, String[] values) {
		super();
		this.keys = keys;
		if (values != null) {
			valuesMap = new HashMap<String, Integer>();
			for (int i = 0; i < values.length; i++) {
				valuesMap.put(values[i], i);
			}
		}
		
		if (keys == null)
			throw new IllegalArgumentException("Keys array cannot be null");		
	}

	public MultiKeyCoefficientMap(AbstractHashedMap map, String[] keys, String[] values) {
		super(map);
		this.keys = keys;
		if (values != null) {
			valuesMap = new HashMap<String, Integer>();
			for (int i = 0; i < values.length; i++) {
				valuesMap.put(values[i], i);
			}
		}
		
		if (keys == null)
			throw new IllegalArgumentException("Keys array cannot be null");
	}

	public static String toStringKey(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof Double) {
			return ((Double) value).toString();
		} else if (value instanceof Boolean) {
			return ((Boolean) value).toString();
		} else
			return value.toString();
		
	}
	
	private Object extractValueFromVector(String key, Object[] vector) {
		if (vector == null)
			return null;
		else {
			final Integer k = valuesMap.get(key);
			if (k == null)
				return null;
			else
				return vector[k];			
		}
	}
	
	private void putValueToVector(String key, Object[] vector, Object value) {
		if (vector == null)
			return;
		else {
			final Integer k = valuesMap.get(key);
			if (k == null)
				return;			
			else
				vector[k] = value;			
		}
	}
	
	public Object getValue(Object ... key) {
		if (key.length == keys.length) {
			switch (key.length) {
				case 1:
					if (key[0] instanceof MultiKey)
						return super.get(key[0]);
					else
						return super.get(new MultiKey( new Object[] { key[0] } ));
				case 2:
					return super.get(key[0], key[1]);
				case 3:
					return super.get(key[0], key[1], key[2]);
				case 4:
					return super.get(key[0], key[1], key[2], key[3]);
				case 5:
					return super.get(key[0], key[1], key[2], key[3], key[4]);
				default:
					throw new IllegalArgumentException("Wrong number of key parameters");
			}
		} else if (key.length == keys.length + 1) {
			Object[] value = null;  
			switch (key.length) {
				case 1:
					throw new IllegalArgumentException("Wrong number of key parameters");
				case 2:					
					value = (Object[]) super.get(new MultiKey( new Object[] { key[0] } ));
					return extractValueFromVector(toStringKey(key[1]), value);
				case 3:
					value = (Object[]) super.get(key[0], key[1]);
					return extractValueFromVector(toStringKey(key[2]), value);
				case 4:
					value = (Object[]) super.get(key[0], key[1], key[2]);
					return extractValueFromVector(toStringKey(key[3]), value);
				case 5:
					value = (Object[]) super.get(key[0], key[1], key[2], key[3]);
					return extractValueFromVector(toStringKey(key[4]), value);
				case 6:
					value = (Object[]) super.get(key[0], key[1], key[2], key[3], key[4]);
					return extractValueFromVector(toStringKey(key[5]), value);
				default:
					throw new IllegalArgumentException("Wrong number of key parameters");
			}			
		} else
			throw new IllegalArgumentException("Wrong number of key parameters");		
	}

	public void putValue(Object ... keyValues) {		
		if (keyValues.length == keys.length + 1) {
			switch (keyValues.length) {
				case 1:
					throw new IllegalArgumentException("Wrong number of key parameters");			
				case 2:
					super.put(new MultiKey(new Object[] { keyValues[0] }), keyValues[1]);
					break;
				case 3:
					super.put(keyValues[0], keyValues[1], keyValues[2]);
					break;
				case 4:
					super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3]);
					break;
				case 5:
					super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4]);
					break;
				case 6:
					super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4], keyValues[5]);
					break;
				default:
					throw new IllegalArgumentException("Wrong number of key parameters");
			}
		} else if (keyValues.length == keys.length + 2) {
			Object[] value;
			switch (keyValues.length) {
				case 1:
				case 2:
					throw new IllegalArgumentException("Wrong number of key parameters");
				case 3:
					value = (Object[]) super.get(keyValues[0]);
					if (value == null)
						value = new Object[valuesMap.size()];
					putValueToVector((String) keyValues[1], value, keyValues[2]);
					super.put(new MultiKey(new Object[] { keyValues[0] }), value);
					break;
				case 4:
					value = (Object[]) super.get(keyValues[0], keyValues[1]);
					if (value == null)
						value = new Object[valuesMap.size()];
					putValueToVector((String) keyValues[2], value, keyValues[3]);
					super.put(keyValues[0], keyValues[1], value);
					break;
				case 5:
					value = (Object[]) super.get(keyValues[0], keyValues[1], keyValues[2]);
					if (value == null)
						value = new Object[valuesMap.size()];
					putValueToVector((String) keyValues[3], value, keyValues[4]);
					super.put(keyValues[0], keyValues[1], keyValues[2], value);
					break;
				case 6:
					value = (Object[]) super.get(keyValues[0], keyValues[1], keyValues[2], keyValues[3]);
					if (value == null)
						value = new Object[valuesMap.size()];
					putValueToVector((String) keyValues[4], value, keyValues[5]);
					super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], value);
					break;
				case 7:
					value = (Object[]) super.get(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4]);
					if (value == null)
						value = new Object[valuesMap.size()];
					putValueToVector((String) keyValues[5], value, keyValues[6]);					
					super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4], value);
					break;
				default:
					throw new IllegalArgumentException("Wrong number of key parameters");
			}			
		} else
			throw new IllegalArgumentException("Wrong number of key parameters");
	}

	////////////////////////////////////
	// New methods
	////////////////////////////////////
	public String[] getKeys() {		//This is so that the LinearRegression class can call the instance of a MultiKeyCoeffientMap to find out what the name of the variables used as keys are.
		return keys;
	}
	
}
