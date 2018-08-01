package it.unito.jas2.demo07.data.filters;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.enums.Gender;

import org.apache.commons.collections4.Predicate;

public class FemaleToCoupleFilter<T extends Person> implements Predicate<T> {

	@Override
	public boolean evaluate(T agent) {

		return (agent.getGender().equals(Gender.Female) && agent.getToCouple());
	}

}
