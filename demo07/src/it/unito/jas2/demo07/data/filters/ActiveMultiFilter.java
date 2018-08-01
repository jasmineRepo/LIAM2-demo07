package it.unito.jas2.demo07.data.filters;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.enums.Gender;

import org.apache.commons.collections4.Predicate;

// This filter is a multi-filter, 
// meaning that it is able to refine the filter depending on the arguments

public class ActiveMultiFilter<T extends Person> implements Predicate<T> {

	private int ageFrom;
	private int ageTo;
	private Gender gender;
	
	public ActiveMultiFilter(int ageFrom, int ageTo, Gender gender) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
		this.gender = gender;
	}

	@Override
	public boolean evaluate(T object) {
		Person agent = (Person) object;
		return (agent.atRiskOfWork() && 
				agent.getGender().equals(gender) && 
				agent.getAge() >= ageFrom &&
				agent.getAge() <= ageTo );
	}

}
