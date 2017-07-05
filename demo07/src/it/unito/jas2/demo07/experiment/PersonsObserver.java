package it.unito.jas2.demo07.experiment;

import it.unito.jas2.demo07.algorithms.ScatterplotSimulationPlotter;
import it.unito.jas2.demo07.algorithms.TimeSeriesSimulationPlotter;
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
//import microsim.gui.plot.ScatterplotSimulationPlotter;
//import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.statistics.CrossSection;
import microsim.statistics.IIntSource;
import microsim.statistics.functions.MeanArrayFunction;
import microsim.statistics.functions.MultiTraceFunction;
import microsim.statistics.functions.SumArrayFunction;

public class PersonsObserver extends AbstractSimulationObserverManager implements EventListener{

	@GUIparameter(description="Toggle to turn off Observer for increased execution speed")
	private Boolean observerOn = true; 

	@GUIparameter
	private Integer displayFrequency = 1;
	
	@GUIparameter(description="Maximum number of persons to display in scatter plot")
	private Integer maxPersonsInScatterplot = 500;
	
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
	
	private ScatterplotSimulationPlotter scatterPlotter;
	private ScatterplotSimulationPlotter scatterPlotter2;
	
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
//		case Update:
//			ageCS.updateSource();
//			nonEmploymentCS.updateSource();
//			employmentCS.updateSource();
//			retiredCS.updateSource();
//			inEducationCS.updateSource();
//			lowEducationCS.updateSource();
//			midEducationCS.updateSource();
//			highEducationCS.updateSource();
//			break;
			
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
		    
		    workPlotter = new TimeSeriesSimulationPlotter("Work status", "Proportion", true, 10);	//Show legend, show only last 10 time-steps
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
		    
		    scatterPlotter = new ScatterplotSimulationPlotter("Scatter plot demo", "education (proportion)", "work status (sum)");
		    scatterPlotter.setMaxSamples(10);		//Show only the previous 10 time-steps of data.
		    scatterPlotter.addSeries("lowEd-nonEmploy", new MeanArrayFunction(lowEducationCS), new SumArrayFunction.Integer(nonEmploymentCS));
		    scatterPlotter.addSeries("highEd-employ", new MeanArrayFunction(highEducationCS), new SumArrayFunction.Integer(employmentCS));
		    
		    GuiUtils.addWindow(scatterPlotter, 100, 150, 400, 400);
		    
		    scatterPlotter2 = new ScatterplotSimulationPlotter("Scatter plot demo enums", "civil status", "work status", false, 1);		//Create scatterplot with no legend ('false' in the argument) and only data from the last '1' time-steps, i.e. only the most recent data.  Set the last argument to 0 to accumulate all historic data.
		    int count = 0;		//Counter to limit the number of people included in the chart to prevent over-crowding.
		    for(Person person : model.getPersons()){
				if(count >= maxPersonsInScatterplot) break;
				scatterPlotter2.addSeries("Person " + person.getKey().getId(), (IIntSource) new MultiTraceFunction.Integer(person, Person.ScatterplotVariables.civilStateValue), (IIntSource) new MultiTraceFunction.Integer(person, Person.ScatterplotVariables.workStateValue));
				count++;
			}
		    GuiUtils.addWindow(scatterPlotter2, 500, 150, 400, 400);
		    
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
		    observerSchedule.addEvent(scatterPlotter, CommonEventType.Update);
		    observerSchedule.addEvent(scatterPlotter2, CommonEventType.Update);
		    getEngine().getEventList().scheduleRepeat(observerSchedule, model.getStartYear(), Order.AFTER_ALL.getOrdering()-1, displayFrequency);
	
		}							
	}

	public Boolean getObserverOn() {
		return observerOn;
	}

	public void setObserverOn(Boolean observerOn) {
		this.observerOn = observerOn;
	}

	public Integer getMaxPersonsInScatterplot() {
		return maxPersonsInScatterplot;
	}

	public void setMaxPersonsInScatterplot(Integer maxPersonsInScatterplot) {
		this.maxPersonsInScatterplot = maxPersonsInScatterplot;
	}

}
