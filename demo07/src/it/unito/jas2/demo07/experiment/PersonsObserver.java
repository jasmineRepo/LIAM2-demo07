package it.unito.jas2.demo07.experiment;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.PersonsModel;
import microsim.annotation.GUIparameter;
import microsim.engine.AbstractSimulationObserverManager;
import microsim.engine.SimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.CommonEventType;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.gui.GuiUtils;
import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.statistics.CrossSection;
import microsim.statistics.functions.MeanArrayFunction;

public class PersonsObserver extends AbstractSimulationObserverManager implements EventListener{

	@GUIparameter(description="Toggle to turn off Observer for increased execution speed")
	private Boolean observerOn = true; 

	@GUIparameter
	private Integer displayFrequency = 1;
	
	private CrossSection.Integer ageCS;
	private CrossSection.Integer nonEmploymentCS;
	private CrossSection.Integer employmentCS;
	private CrossSection.Integer retiredCS;
	private CrossSection.Integer inEducationCS;
	private CrossSection.Integer lowEducationCS;
	private CrossSection.Integer midEducationCS;
	private CrossSection.Integer highEducationCS;
	
	private TimeSeriesSimulationPlotter agePlotter;
	private TimeSeriesSimulationPlotter workPlotter;
	private TimeSeriesSimulationPlotter eduPlotter;
	
	public Integer getDisplayFrequency() {
		return displayFrequency;
	}

	public void setDisplayFrequency(Integer displayFrequency) {
		this.displayFrequency = displayFrequency;
	}
	
	final PersonsModel model = (PersonsModel) getManager();
	
	public PersonsObserver(SimulationManager manager, SimulationCollectorManager simulationCollectionManager) {
		super(manager, simulationCollectionManager);		
	}
	
	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------
	
	public enum Processes {
		Update,
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
			
		}
	}
			
	
	// ---------------------------------------------------------------------
	// Manager
	// ---------------------------------------------------------------------

	@Override
	public void buildObjects() {
		if(observerOn) {
			ageCS = new CrossSection.Integer(model.getPersons(), Person.class, "age", false);
			nonEmploymentCS = new CrossSection.Integer(model.getPersons(), Person.class, "getNonEmployed", true);
			employmentCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			retiredCS = new CrossSection.Integer(model.getPersons(), Person.class, "getRetired", true);
			inEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getStudent", true);
			lowEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getLowEducation", true);
			midEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getMidEducation", true);
			highEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getHighEducation", true);	
				
		    agePlotter = new TimeSeriesSimulationPlotter("Age", "Age (Years)");
		    agePlotter.addSeries("avg", new MeanArrayFunction(ageCS));
		    GuiUtils.addWindow(agePlotter, 0, 110, 500, 500);	
		    
		    workPlotter = new TimeSeriesSimulationPlotter("Work status", "Proportion");
		    workPlotter.addSeries("employed", new MeanArrayFunction(employmentCS));
		    workPlotter.addSeries("non-employed", new MeanArrayFunction(nonEmploymentCS));
		    workPlotter.addSeries("retired", new MeanArrayFunction(retiredCS));
		    workPlotter.addSeries("students", new MeanArrayFunction(inEducationCS));
		    GuiUtils.addWindow(workPlotter, 500, 110, 500, 500);	
		    
		    eduPlotter = new TimeSeriesSimulationPlotter("Education level", "Proportion");
		    eduPlotter.addSeries("low", new MeanArrayFunction(lowEducationCS));
		    eduPlotter.addSeries("mid", new MeanArrayFunction(midEducationCS));
		    eduPlotter.addSeries("high", new MeanArrayFunction(highEducationCS));
		    GuiUtils.addWindow(eduPlotter, 1000, 110, 500, 500);
		}
	}
	
	@Override
	public void buildSchedule() {
		if(observerOn) {
			EventGroup observerSchedule = new EventGroup();
	
		    observerSchedule.addEvent(this, Processes.Update);
		    observerSchedule.addEvent(agePlotter, CommonEventType.Update);
		    observerSchedule.addEvent(workPlotter, CommonEventType.Update);
		    observerSchedule.addEvent(eduPlotter, CommonEventType.Update);
		    getEngine().getEventQueue().scheduleRepeat(observerSchedule, model.getStartYear(), Order.AFTER_ALL.getOrdering()-1, displayFrequency);
	
		}							
	}

	public Boolean getObserverOn() {
		return observerOn;
	}

	public void setObserverOn(Boolean observerOn) {
		this.observerOn = observerOn;
	}

}
