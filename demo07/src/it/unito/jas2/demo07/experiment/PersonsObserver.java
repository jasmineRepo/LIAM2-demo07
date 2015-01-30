package it.unito.jas2.demo07.experiment;

import it.zero11.microsim.annotation.ModelParameter;
import it.zero11.microsim.engine.AbstractSimulationObserverManager;
import it.zero11.microsim.engine.SimulationCollectorManager;
import it.zero11.microsim.engine.SimulationManager;
import it.zero11.microsim.event.CommonEventType;
import it.zero11.microsim.event.SingleTargetEvent;
import it.zero11.microsim.gui.GuiUtils;
import it.zero11.microsim.gui.plot.TimeSeriesSimulationPlotter;
import it.zero11.microsim.statistics.functions.MeanArrayFunction;

public class PersonsObserver extends AbstractSimulationObserverManager {

	@ModelParameter
	private Integer displayFrequency = 1;
	
	private TimeSeriesSimulationPlotter agePlotter;
	private TimeSeriesSimulationPlotter workPlotter;
	private TimeSeriesSimulationPlotter eduPlotter;
	
	public Integer getDisplayFrequency() {
		return displayFrequency;
	}

	public void setDisplayFrequency(Integer displayFrequency) {
		this.displayFrequency = displayFrequency;
	}
	
	public PersonsObserver(SimulationManager manager, SimulationCollectorManager simulationCollectionManager) {
		super(manager, simulationCollectionManager);		
	}
	
	
	// ---------------------------------------------------------------------
	// Manager
	// ---------------------------------------------------------------------

	@Override
	public void buildObjects() {
		final PersonsCollector collector = (PersonsCollector) getCollectorManager();
		
	    agePlotter = new TimeSeriesSimulationPlotter("Age", "age");
	    agePlotter.addSeries("avg", new MeanArrayFunction(collector.getAgeCS()));
	    GuiUtils.addWindow(agePlotter, 250, 50, 500, 500);	
	    
	    workPlotter = new TimeSeriesSimulationPlotter("Work status", "");
	    workPlotter.addSeries("employed", new MeanArrayFunction(collector.getEmploymentCS()));
	    workPlotter.addSeries("non-employed", new MeanArrayFunction(collector.getNonEmploymentCS()));
	    workPlotter.addSeries("retired", new MeanArrayFunction(collector.getRetiredCS()));
	    workPlotter.addSeries("students", new MeanArrayFunction(collector.getInEducationCS()));
	    GuiUtils.addWindow(workPlotter, 750, 50, 500, 500);	
	    
	    eduPlotter = new TimeSeriesSimulationPlotter("Education level", "");
	    eduPlotter.addSeries("low", new MeanArrayFunction(collector.getLowEducationCS()));
	    eduPlotter.addSeries("mid", new MeanArrayFunction(collector.getMidEducationCS()));
	    eduPlotter.addSeries("high", new MeanArrayFunction(collector.getHighEducationCS()));
	    GuiUtils.addWindow(eduPlotter, 1250, 50, 500, 500);	
	}
	
	@Override
	public void buildSchedule() {
		
		getEngine().getEventList().schedule(new SingleTargetEvent(agePlotter, CommonEventType.Update), 0, displayFrequency);
		getEngine().getEventList().schedule(new SingleTargetEvent(workPlotter, CommonEventType.Update), 0, displayFrequency);
		getEngine().getEventList().schedule(new SingleTargetEvent(eduPlotter, CommonEventType.Update), 0, displayFrequency);		
							
	}

}
