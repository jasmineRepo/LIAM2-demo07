package it.unito.jas2.demo07.experiment;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.PersonsModel;
import it.zero11.microsim.data.db.DatabaseUtils;
import it.zero11.microsim.engine.AbstractSimulationCollectorManager;
import it.zero11.microsim.engine.SimulationManager;
import it.zero11.microsim.event.EventGroup;
import it.zero11.microsim.event.EventListener;
import it.zero11.microsim.statistics.CrossSection;

import org.apache.log4j.Logger;

public class PersonsCollector extends AbstractSimulationCollectorManager implements EventListener {

	private static Logger log = Logger.getLogger(PersonsCollector.class);

	private CrossSection.Integer ageCS;
	private CrossSection.Integer nonEmploymentCS;
	private CrossSection.Integer employmentCS;
	private CrossSection.Integer retiredCS;
	private CrossSection.Integer inEducationCS;
	private CrossSection.Integer lowEducationCS;
	private CrossSection.Integer midEducationCS;
	private CrossSection.Integer highEducationCS;
	
	public PersonsCollector(SimulationManager manager) {
		super(manager);		
	}
	
	// ---------------------------------------------------------------------
	// Event Listener
	// ---------------------------------------------------------------------
	
	public enum Processes {
		Update,
		DumpInfo;
	}
	
	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case Update:
			ageCS.updateSource();
			nonEmploymentCS.updateSource();
			employmentCS.updateSource();
			retiredCS.updateSource();
			inEducationCS.updateSource();
			lowEducationCS.updateSource();
			midEducationCS.updateSource();
			highEducationCS.updateSource();
			break;
			
		case DumpInfo:
			try {
				DatabaseUtils.snap(((PersonsModel) getManager()).getPersons());
				DatabaseUtils.snap(((PersonsModel) getManager()).getHouseholds());
			} catch (Exception e) {
				log.error(e.getMessage());				
			}
			break;	
		}
	}
	
	// ---------------------------------------------------------------------
	// Manager
	// ---------------------------------------------------------------------
	
	@Override
	public void buildObjects() {
		
		final PersonsModel model = (PersonsModel) getManager();
		ageCS = new CrossSection.Integer(model.getPersons(), Person.class, "age", false);
		nonEmploymentCS = new CrossSection.Integer(model.getPersons(), Person.class, "getNonEmployed", true);
		employmentCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
		retiredCS = new CrossSection.Integer(model.getPersons(), Person.class, "getRetired", true);
		inEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getStudent", true);
		lowEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getLowEducation", true);
		midEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getMidEducation", true);
		highEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getHighEducation", true);	
			
	}
	
	@Override
	public void buildSchedule() {	
		
		EventGroup collectorSchedule = new EventGroup();

	    collectorSchedule.addEvent(this, Processes.Update);
	    collectorSchedule.addEvent(this, Processes.DumpInfo);

	    getEngine().getEventList().schedule(collectorSchedule, 0, 1);		
	}
	
	
	// ---------------------------------------------------------------------
	// getters and setters
	// ---------------------------------------------------------------------

	public CrossSection.Integer getAgeCS() {
		return ageCS;
	}

	public void setAgeCS(CrossSection.Integer ageCS) {
		this.ageCS = ageCS;
	}

	public CrossSection.Integer getNonEmploymentCS() {
		return nonEmploymentCS;
	}

	public CrossSection.Integer getEmploymentCS() {
		return employmentCS;
	}

	public CrossSection.Integer getRetiredCS() {
		return retiredCS;
	}

	public CrossSection.Integer getInEducationCS() {
		return inEducationCS;
	}

	public CrossSection.Integer getLowEducationCS() {
		return lowEducationCS;
	}

	public CrossSection.Integer getMidEducationCS() {
		return midEducationCS;
	}

	public CrossSection.Integer getHighEducationCS() {
		return highEducationCS;
	}

	

}
