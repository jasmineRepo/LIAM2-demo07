package it.unito.jas2.demo07.data.filters;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.enums.Gender;

import org.apache.commons.collections4.Predicate;

public class MaleToCoupleFilter<T extends Person> implements Predicate<T> {

	@Override
	public boolean evaluate(T object) {
		Person agent = (Person) object;
		return (agent.getGender().equals(Gender.Male) && agent.getToCouple());
	}

}
