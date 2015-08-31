package it.unito.jas2.demo07.model;

import it.zero11.microsim.data.db.PanelEntityKey;
import it.zero11.microsim.engine.SimulationEngine;
import it.zero11.microsim.event.EventListener;

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
	private PanelEntityKey id;

	private Integer nbPersons = 0;
	
	private Integer nbChildren = 0;
	
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

		id = new PanelEntityKey();
		id.setId(idNumber);     
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


	
	// ---------------------------------------------------------------------------
	// access methods
	// ---------------------------------------------------------------------------

	public PanelEntityKey getId() {
		return id;
	}
	
	public void setId (PanelEntityKey id) {
		this.id = id;
	}

	public List<Person> getHouseholdMembers() {
		return householdMembers;
	}

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
			model.removeHousehold(this);
		}
		return personRemoveSuccessful;
	}
	
}
