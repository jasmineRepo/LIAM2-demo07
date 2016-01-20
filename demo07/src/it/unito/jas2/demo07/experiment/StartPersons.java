package it.unito.jas2.demo07.experiment;

import it.unito.jas2.demo07.model.PersonsModel;
import microsim.engine.ExperimentBuilder;
import microsim.engine.SimulationEngine;
import microsim.gui.shell.MicrosimShell;

public class StartPersons implements ExperimentBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean showGui = true;
		
		StartPersons experimentBuilder = new StartPersons();
		final SimulationEngine engine = SimulationEngine.getInstance();
		MicrosimShell gui = null;
		if (showGui) {
			gui = new MicrosimShell(engine);		
			gui.setVisible(true);
		}
		
		engine.setExperimentBuilder(experimentBuilder);
		
		engine.setup();				
		
	}

	@Override
	public void buildExperiment(SimulationEngine engine) {
		PersonsModel model = new PersonsModel();
		PersonsCollector collector = new PersonsCollector(model);
		PersonsObserver observer = new PersonsObserver(model, collector);
				
		engine.addSimulationManager(model);
		engine.addSimulationManager(collector);
		engine.addSimulationManager(observer);	
	}

}
