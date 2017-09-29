package it.unito.jas2.demo07.data.filters;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.enums.Gender;

import org.apache.commons.collections4.Predicate;

public class MaleToCoupleFilter implements Predicate<Person> {

	@Override
	public boolean evaluate(Person agent) {
		return (agent.getGender().equals(Gender.Male) && agent.getToCouple());
	}

}
