package it.unito.jas2.demo07.data.filters;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.enums.Gender;

import org.apache.commons.collections.Predicate;

public class MaleToCoupleFilter implements Predicate {

	@Override
	public boolean evaluate(Object object) {
		Person agent = (Person) object;
		return (agent.getGender().equals(Gender.Male) && agent.getToCouple());
	}

}
