package it.unito.jas2.demo07.experiment;

import it.unito.jas2.demo07.model.Person;
import it.unito.jas2.demo07.model.PersonsModel;
import it.zero11.microsim.annotation.ModelParameter;
import it.zero11.microsim.engine.AbstractSimulationObserverManager;
import it.zero11.microsim.engine.SimulationCollectorManager;
import it.zero11.microsim.engine.SimulationManager;
import it.zero11.microsim.event.CommonEventType;
import it.zero11.microsim.event.EventGroup;
import it.zero11.microsim.event.EventListener;
import it.zero11.microsim.gui.GuiUtils;
import it.zero11.microsim.gui.plot.TimeSeriesSimulationPlotter;
import it.zero11.microsim.statistics.CrossSection;
import it.zero11.microsim.statistics.functions.MeanArrayFunction;

public class PersonsObserver extends AbstractSimulationObserverManager implements EventListener{

	@ModelParameter(description="Toggle to turn off Observer for increased execution speed")
	private Boolean observerOn = true; 

	@ModelParameter
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
				
		    agePlotter = new TimeSeriesSimulationPlotter("Age", "age");
		    agePlotter.addSeries("avg", new MeanArrayFunction(ageCS));
		    GuiUtils.addWindow(agePlotter, 250, 60, 500, 500);	
		    
		    workPlotter = new TimeSeriesSimulationPlotter("Work status", "");
		    workPlotter.addSeries("employed", new MeanArrayFunction(employmentCS));
		    workPlotter.addSeries("non-employed", new MeanArrayFunction(nonEmploymentCS));
		    workPlotter.addSeries("retired", new MeanArrayFunction(retiredCS));
		    workPlotter.addSeries("students", new MeanArrayFunction(inEducationCS));
		    GuiUtils.addWindow(workPlotter, 750, 60, 500, 500);	
		    
		    eduPlotter = new TimeSeriesSimulationPlotter("Education level", "");
		    eduPlotter.addSeries("low", new MeanArrayFunction(lowEducationCS));
		    eduPlotter.addSeries("mid", new MeanArrayFunction(midEducationCS));
		    eduPlotter.addSeries("high", new MeanArrayFunction(highEducationCS));
		    GuiUtils.addWindow(eduPlotter, 1250, 60, 500, 500);
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
		    getEngine().getEventList().schedule(observerSchedule, 0, displayFrequency);
	
	//		getEngine().getEventList().schedule(new SingleTargetEvent(this, Processes.Update), 0, displayFrequency);	
	//		getEngine().getEventList().schedule(new SingleTargetEvent(agePlotter, CommonEventType.Update), 0, displayFrequency);
	//		getEngine().getEventList().schedule(new SingleTargetEvent(workPlotter, CommonEventType.Update), 0, displayFrequency);
	//		getEngine().getEventList().schedule(new SingleTargetEvent(eduPlotter, CommonEventType.Update), 0, displayFrequency);		
		}							
	}

	public Boolean getObserverOn() {
		return observerOn;
	}

	public void setObserverOn(Boolean observerOn) {
		this.observerOn = observerOn;
	}

}
