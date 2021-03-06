package it.unito.jas2.demo07.model;

import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

@Entity
public class Household implements EventListener {

	public static long householdIdCounter = 100000;
	
	@Transient
	private PersonsModel model;
	
	@Id
	private PanelEntityKey key;

	private Integer nbPersons = 0;			//For Database records
	
	private Integer nbChildren = 0;			//For Database records
	
	@Transient
	private List<Person> householdMembers;
	
	
	
	// ---------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------
	
	public Household() {
		super();
        
		model = (PersonsModel) SimulationEngine.getInstance().getManager(PersonsModel.class.getCanonicalName());	
		
		// initiate vbles which are not in database
		householdMembers = new ArrayList<Person>();
	}
	
	public Household( long idNumber ) {
		this();

		key = new PanelEntityKey(idNumber);
		
	}
	
	
	// ---------------------------------------------------------------------
	// Event Listener
	// ---------------------------------------------------------------------

	public enum Processes {
		HouseholdComposition;
	}
	
	@Override
	public void onEvent(Enum<?> type) {
		
		switch ((Processes) type) {		
			case HouseholdComposition:
				nbPersons = getNbPersons();
				nbChildren = getNbChildren();
				break;
		}
	}
	

	// ---------------------------------------------------------------------
	// own methods
	// ---------------------------------------------------------------------

	public boolean addPerson(Person person) {		
		return householdMembers.add(person);
	}
	
	public int getNbPersons() {
		return householdMembers.size();	
	}

	public int getNbChildren() {
		return 	CollectionUtils.countMatches(getHouseholdMembers(),  new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((Person) object).getAge() < 18;
			}
		});
	}
	
	public boolean removePerson(Person person) {

		boolean personRemoveSuccessful = householdMembers.remove(person);
		
		if (householdMembers.isEmpty())
		{
			boolean removeHouseholdSuccessful = false;
			removeHouseholdSuccessful = model.removeHousehold(this);
			return (personRemoveSuccessful && removeHouseholdSuccessful);
		} else {
			return personRemoveSuccessful;
		}
	}

	
	// ---------------------------------------------------------------------------
	// access methods
	// ---------------------------------------------------------------------------

	public PanelEntityKey getKey() {
		return key;
	}
	
	public void setKey (PanelEntityKey key) {
		this.key = key;
	}

	public List<Person> getHouseholdMembers() {
		return householdMembers;
	}
}
