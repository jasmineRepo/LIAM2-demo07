package it.unito.jas2.demo07.experiment;

import it.unito.jas2.demo07.model.PersonsModel;
import microsim.annotation.GUIparameter;
import microsim.data.DataExport;
import microsim.engine.AbstractSimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;

import org.apache.log4j.Logger;

public class PersonsCollector extends AbstractSimulationCollectorManager implements EventListener {

	private static Logger log = Logger.getLogger(PersonsCollector.class);
	
	@GUIparameter(description="Toggle to persist data to database")
	private Boolean exportToDatabase = false;
	
	@GUIparameter(description="Toggle to export data to CSV files")
	private Boolean exportToCSV = true;
	
	@GUIparameter(description="number of timesteps to wait before persisting database")
	private Integer databaseDumpStartsAfterTimestep = 0;		//Allows the user to control when the simulation starts exporting to the database, in case they want to delay exporting until after an initial 'burn-in' period.	

	@GUIparameter(description="number of timesteps between database dumps")
	private Integer numTimestepsBetweenDatabaseDumps = 1;
	
	final PersonsModel model = (PersonsModel) getManager();
	
	DataExport personsData;
	DataExport householdsData;
	
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
			personsData.export();
			householdsData.export();
			break;	
		}
	}
	
	// ---------------------------------------------------------------------
	// Manager
	// ---------------------------------------------------------------------
	
	@Override
	public void buildObjects() {
		personsData = new DataExport(((PersonsModel) getManager()).getPersons(), exportToDatabase, exportToCSV);
		householdsData = new DataExport(((PersonsModel) getManager()).getHouseholds(), exportToDatabase, exportToCSV);

	}
	
	@Override
	public void buildSchedule() {	
			
		//Schedule periodic dumps of data to database and/or .csv files during the simulation
	    getEngine().getEventList().scheduleRepeat(new SingleTargetEvent(this, Processes.DumpInfo), model.getStartYear() + databaseDumpStartsAfterTimestep, Order.AFTER_ALL.getOrdering()-1, numTimestepsBetweenDatabaseDumps);
		    
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

	public Boolean getExportToDatabase() {
		return exportToDatabase;
	}

	public void setExportToDatabase(Boolean exportToDatabase) {
		this.exportToDatabase = exportToDatabase;
	}

	public Boolean getExportToCSV() {
		return exportToCSV;
	}

	public void setExportToCSV(Boolean exportToCSV) {
		this.exportToCSV = exportToCSV;
	}
	
}
