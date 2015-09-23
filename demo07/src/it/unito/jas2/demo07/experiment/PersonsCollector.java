package it.unito.jas2.demo07.experiment;

import it.unito.jas2.demo07.model.PersonsModel;
import it.zero11.microsim.annotation.ModelParameter;
import it.zero11.microsim.data.db.DatabaseUtils;
import it.zero11.microsim.engine.AbstractSimulationCollectorManager;
import it.zero11.microsim.engine.SimulationManager;
import it.zero11.microsim.event.EventListener;
import it.zero11.microsim.event.SingleTargetEvent;
import org.apache.log4j.Logger;

public class PersonsCollector extends AbstractSimulationCollectorManager implements EventListener {

	private static Logger log = Logger.getLogger(PersonsCollector.class);
	
	@ModelParameter(description="Toggle to persist data to database")
	private Boolean persistData = false;
	
	@ModelParameter(description="number of timesteps to wait before persisting database")
	private Integer databaseDumpStartsAfterTimestep = 10;		//Allows the user to control when the simulation starts exporting to the database, in case they want to delay exporting until after an initial 'burn-in' period.	

	@ModelParameter(description="number of timesteps between database dumps")
	private Integer numTimestepsBetweenDatabaseDumps = 10;
	
	final PersonsModel model = (PersonsModel) getManager();
	
	public PersonsCollector(SimulationManager manager) {
		super(manager);		
	}
	
	// ---------------------------------------------------------------------
	// Event Listener
	// ---------------------------------------------------------------------
	
	public enum Processes {
		DumpInfo;
	}
	
	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

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
			
	}
	
	@Override
	public void buildSchedule() {	
		if(persistData) {
			
			//Schedule periodic dumps of data to database during the simulation
		    getEngine().getEventList().schedule(new SingleTargetEvent(this, Processes.DumpInfo), model.getStartYear() + databaseDumpStartsAfterTimestep, numTimestepsBetweenDatabaseDumps);
		    
		}
	}
	
	//This is so that Model class can call at end of simulation just before stopping to dump the database
	public void dumpInfo() {
		try {
			DatabaseUtils.snap(((PersonsModel) getManager()).getPersons());
			DatabaseUtils.snap(((PersonsModel) getManager()).getHouseholds());
		} catch (Exception e) {
			log.error(e.getMessage());				
		}
	}
	
	// ---------------------------------------------------------------------
	// getters and setters
	// ---------------------------------------------------------------------

	public Integer getDatabaseDumpStartsAfterTimestep() {
		return databaseDumpStartsAfterTimestep;
	}

	public void setDatabaseDumpStartsAfterTimestep(
			Integer databaseDumpStartsAfterTimestep) {
		this.databaseDumpStartsAfterTimestep = databaseDumpStartsAfterTimestep;
	}

	public Integer getNumTimestepsBetweenDatabaseDumps() {
		return numTimestepsBetweenDatabaseDumps;
	}

	public void setNumTimestepsBetweenDatabaseDumps(
			Integer numTimestepsBetweenDatabaseDumps) {
		this.numTimestepsBetweenDatabaseDumps = numTimestepsBetweenDatabaseDumps;
	}

	public Boolean getPersistData() {
		return persistData;
	}

	public void setPersistData(Boolean persistData) {
		this.persistData = persistData;
	}
	
}
