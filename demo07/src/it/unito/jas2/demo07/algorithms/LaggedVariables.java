package it.unito.jas2.demo07.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LaggedVariables {

	private Map<String, Integer> lengths;
	private Map<String, Stack<Object>> values;
	
	public LaggedVariables() {
		lengths = new HashMap<String, Integer>();
		values = new HashMap<String, Stack<Object>>();
	}
	
	public LaggedVariables configure(String variable, int length) {
		lengths.put(variable, length);
		values.put(variable, new Stack<Object>());
		return this;
	}
	
	public LaggedVariables push(String variable, Object value) {
		final Integer len = lengths.get(variable);
		final Stack<Object> stack = values.get(variable);
		stack.push(value);
		if (stack.size() > len)
			stack.remove(0);
		
		return this;
	}
	
	public Object get(String variable) {
		final Stack<Object> stack = values.get(variable);
		return stack.peek();
	}
	
	public Object get(String variable, int lag) {
		final Stack<Object> stack = values.get(variable);
		return stack.get(lag);
	}
}
