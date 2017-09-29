package it.unito.jas2.demo07.data.filters;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.enums.CivilState;
import it.unito.jas2.demo07.model.enums.Gender;

import org.apache.commons.collections4.Predicate;

public class FemaleToDivorce implements Predicate<Person> {
	
	private int ageFrom;
	private int ageTo;
	
	public FemaleToDivorce(int ageFrom, int ageTo) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
	}

	@Override
	public boolean evaluate(Person agent) {
		return (agent.getGender().equals(Gender.Female) &&
				agent.getCivilState().equals(CivilState.Married) &&
				agent.getDurationInCouple() > 0 &&
				agent.getAge() >= ageFrom &&
				agent.getAge() <= ageTo);
	}

}
