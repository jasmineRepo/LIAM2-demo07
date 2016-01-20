package it.unito.jas2.demo07.model;

import it.unito.jas2.demo07.data.Parameters;
import it.unito.jas2.demo07.data.filters.ActiveMultiFilter;
import it.unito.jas2.demo07.data.filters.FemaleToCoupleFilter;
import it.unito.jas2.demo07.data.filters.FemaleToDivorce;
import it.unito.jas2.demo07.data.filters.MaleToCoupleFilter;
import it.unito.jas2.demo07.experiment.PersonsCollector;
import it.unito.jas2.demo07.model.enums.Education;
import it.unito.jas2.demo07.model.enums.Gender;
import it.unito.jas2.demo07.model.enums.WorkState;
import microsim.alignment.probability.AlignmentProbabilityClosure;
import microsim.alignment.probability.SBDAlignment;
import microsim.annotation.ModelParameter;
import microsim.collection.Aggregate;
import microsim.collection.AverageClosure;
import microsim.data.MultiKeyCoefficientMap;
import microsim.data.db.DatabaseUtils;
import microsim.engine.AbstractSimulationManager;
import microsim.engine.SimulationEngine;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;
import microsim.matching.MatchingClosure;
import microsim.matching.MatchingScoreClosure;
import microsim.matching.SimpleMatching;
import microsim.statistics.regression.RegressionUtils;

import java.lang.Math;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;

public class PersonsModel extends AbstractSimulationManager implements EventListener {

	private static Logger log = Logger.getLogger(PersonsModel.class);
	
	@ModelParameter(description="Simulation first year (valid range 2002-2060)")
	private Double startYear = 2002.0;

	@ModelParameter(description="Simulation ends at year [valid range 2003-2061]")
	private Double endYear = 2060.0;
	
	@ModelParameter(description="Retirement age for women")
	private Integer wemra = 61;
	
	@ModelParameter(description="Toggle to turn off verbose information on time to complete each method")
	private Boolean printElapsedTime = false; 

	private List<Person> persons;
	
	private List<Household> households;
	
	private long elapsedTime;
	private int methodId = 0;
	private int year;
	

	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------
	
	public enum Processes {
		MarriageMatching,
		DivorceAlignment,
		InWorkAlignment,
		Stop,
		Timer, 
		UpdateYear;
	}
	
	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {	
		case DivorceAlignment:
//			System.out.println("DivorceAlignment");
			divorceAlignment();
			break;
		case InWorkAlignment:
//			System.out.println("InWorkAlignment");
			inWorkAlignment();
			break;
		case MarriageMatching:
//			System.out.println("MarriageMatching");
			marriageMatching();
			break;
		case Stop:
			((PersonsCollector)SimulationEngine.getInstance().getManager(PersonsCollector.class.getCanonicalName())).dumpInfo();
			log.info("Model completed.");
//			System.out.println("Model completed.");
			getEngine().pause();		
			break;
		case Timer:
			printElapsedTime();		//Comment or Uncomment depending on whether you want more System.out calls (which slow down the simulation, although useful to record the time to execute the methods for benchmarking. 
			break;
		case UpdateYear:
			year++;
			break;
		}
	}
	
	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------
	
	@SuppressWarnings("unchecked")
	@Override
	public void buildObjects() {
		
		SimulationEngine.getRnd().setSeed(0);
		Parameters.loadParameters();
		System.out.println("Parameters loaded.");
		 
		households = (List<Household>) DatabaseUtils.loadTable(Household.class);
		persons = (List<Person>) DatabaseUtils.loadTable(Person.class);
		System.out.println("Initial population loaded from input database.");
		
		//Initialisation and Cleanup
		initializeNonDatabaseAttributes();		//Initialize attributes that do not appear in the input database
		addPersonsToHouseholds();				//Add the population to the households.
		cleanInitialPopulation();				//This ensures that marriage partnerships can only occur between reciprocal partners who live in the same household.
			
	}
	
	@Override
	public void buildSchedule() {
		
		EventGroup modelSchedule = new EventGroup();		
		// 1: Aging
		modelSchedule.addCollectionEvent(persons, Person.Processes.Ageing);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}
		
		// 2: Death
		modelSchedule.addCollectionEvent(persons, Person.Processes.Death, false);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}
		
		// 3: Birth 
		modelSchedule.addCollectionEvent(persons, Person.Processes.Birth, false);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}
		
		// 4: Marriage
		modelSchedule.addCollectionEvent(persons, Person.Processes.ToCouple);
		modelSchedule.addEvent(this,  Processes.MarriageMatching);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}
		
		// 5: Exit from parental home
		modelSchedule.addCollectionEvent(persons, Person.Processes.GetALife);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}
		
		// 6: Divorce 
		modelSchedule.addEvent(this,  Processes.DivorceAlignment);
		modelSchedule.addCollectionEvent(persons, Person.Processes.Divorce);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}	
		
		// 7: Household composition (for reporting only: household composition is updated whenever needed throughout the simulation
		modelSchedule.addCollectionEvent(households, Household.Processes.HouseholdComposition);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}	
		
		// 8: Education
		modelSchedule.addCollectionEvent(persons, Person.Processes.InEducation);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}
		
		// 9: Work 
		modelSchedule.addEvent(this, Processes.InWorkAlignment);
		if(printElapsedTime) {
			modelSchedule.addEvent(this, Processes.Timer);
		}
	
		modelSchedule.addEvent(this, Processes.UpdateYear);
		getEngine().getEventList().scheduleRepeat(modelSchedule, startYear, 0, 1.);
		
		//Schedule model to stop
		getEngine().getEventList().scheduleOnce(new SingleTargetEvent(this, Processes.Stop), endYear, Order.AFTER_ALL.getOrdering());
		
		year = startYear.intValue();
		elapsedTime = System.currentTimeMillis();
		
	}
	
	// ---------------------------------------------------------------------
		// Initialization and cleaning methods
		// ---------------------------------------------------------------------	
		
	private void initializeNonDatabaseAttributes() {
		for(Person person : persons)
		{
			// initialize vbles which are not in the input dbase
			
			//education level
			person.setEducationlevel( RegressionUtils.event(Education.class, new double[] {0.25, 0.39, 0.36}) );		//Sets educationlevel of initial population randomly. 
			person.inEducation();		//Changes the workstate of students over certain age to NotEmployed
			
			Long partnerId = person.getPartnerId();
			Long motherId = person.getMotherId();
			Long householdId = person.getHouseholdId();
			if(partnerId != null)
				person.setPartner(getPerson( partnerId ));
			if(motherId != null) 
				person.setMother(getPerson( motherId));
			if(householdId != null) {
				person.setHousehold(getHousehold(householdId));
			}

		}
	}

	private void addPersonsToHouseholds() {
		for(Person person : persons)
		{
				person.getHousehold().addPerson(person);
		}
	}
				
	private void cleanInitialPopulation() {		//This ensures that marriage partnerships can only occur between reciprocal partners who live in the same household.
		// Clean input database 
		HashSet<Person> peopleToRemove = new HashSet<Person>();
		for(Person thisPerson : persons)
		{
			if(thisPerson.getPartnerId() != null)
			{
				Person otherPerson = thisPerson.getPartner();

				// 1) Check for persons with non-reciprocal partners.
				if ( thisPerson != otherPerson.getPartner() ) {			//This can handle love triangles (or longer loops) and unrequited marriages (i.e. one person is married to someone, who is already married to someone else in a normal arrangement).  As we are not yet removing persons, we do not create IllegalArgument Exceptions, which would happen when removed people are referenced by their partners.
//					System.out.println("Person " + thisPerson.getId().getId() + " has PartnerID " + thisPerson.getPartnerId() + " and Person " + otherPerson.getId().getId() + " has PartnerID " + otherPerson.getPartnerId() + " so we remove Person " + thisPerson.getId().getId() + " for having a  non-reciprocal partner");
					peopleToRemove.add(thisPerson);
				}
				
				// 2) remove married persons not living in the same household
				else if ( thisPerson.getHousehold() != otherPerson.getHousehold() ) {
//					System.out.println("Person " + thisPerson.getId().getId() + " has HouseholdID " + thisPerson.getHouseholdId() + " whereas their partner " + thisPerson.getPartnerId() + " who should have ID " + otherPerson.getId().getId() + " has householdID " + otherPerson.getHouseholdId());
					peopleToRemove.add(thisPerson);
				}				
			}
		}
		
		int nRemovedPersons = 0;
		for(Person person : peopleToRemove)
		{
			person.getHousehold().removePerson(person);		//Remove person from household (removes household if person was alone)
			this.removePerson(person);
			nRemovedPersons++;
		}
		if(nRemovedPersons > 0)
			System.out.println(nRemovedPersons + " persons removed because of erroneous information in the input database");
		
		int nRemovedHouseholds = 0;
		
		for(Household household : households)		//Check for any empty households and remove if found.
		{
			if(household.getHouseholdMembers().isEmpty())
			{
				this.removeHousehold(household);
				nRemovedHouseholds++;
			}
		}
		if(nRemovedHouseholds > 0)
			System.out.println(nRemovedHouseholds + " empty households removed because of erroneous information in the input database");
	}
	
	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------	
	
	//	All-in-one alignment: the closure computes the probability, and then assigns the outcome.
	
	private void divorceAlignment() {
		
		// perform alignment on each cell of parameter file 
		MultiKeyCoefficientMap pDivorceMap = Parameters.getpDivorce();

		for (MapIterator iterator = pDivorceMap.mapIterator(); iterator.hasNext();) {
			iterator.next();
			MultiKey mk = (MultiKey) iterator.getKey();
			Integer ageFrom = (Integer) mk.getKey(0);
			Integer ageTo = (Integer) mk.getKey(1);
			double divorceTarget = ((Number) pDivorceMap.getValue(ageFrom, ageTo, (int)SimulationEngine.getInstance().getTime())).doubleValue();

			//Align
			new SBDAlignment<Person>().align(
					getPersons(), 
					new FemaleToDivorce(ageFrom, ageTo), 
					new AlignmentProbabilityClosure<Person>() {

						@Override
						public double getProbability(Person agent) {
							return agent.computeDivorceProb();
						}

						@Override
						public void align(Person agent, double alignedProbability) {
//							boolean divorce = RegressionUtils.event(alignedProbability, SimulationEngine.getRnd());		//An unnecessary complicated step when considered with the implementation of the SBDAlgorithm, as the alignedProbability is either 1 or 0.  A simpler implementation of the functionality here would simply be to replace the RegressionUtils.event() method by setting the boolean divorce to the true/false depending on whether alignedProbability is 1.0 or 0.0
							boolean divorce = (alignedProbability == 1);
							agent.setToDivorce(divorce);

						}
					}, divorceTarget); 				
		}
//		System.out.println("Divorce aligned.");
	}
	
	private void inWorkAlignment() {
		MultiKeyCoefficientMap map = Parameters.getpInWork();
		
		for (MapIterator iterator = map.mapIterator(); iterator.hasNext();) {
			iterator.next();
			MultiKey mk = (MultiKey) iterator.getKey();
			Integer ageFrom = (Integer) mk.getKey(0);
			Integer ageTo = (Integer) mk.getKey(1);
//			Gender gender = Gender.values()[(Integer) mk.getKey(2)];		//When 1 / 0 are entries for Male / Female respectively in p_inwork.xls
			Gender gender = Gender.valueOf((String) mk.getKey(2));			//When Male / Female are entries in p_inwork.xls

			double inWorkTarget = ((Number) map.getValue(ageFrom, ageTo, gender.toString(), (int)SimulationEngine.getInstance().getTime())).doubleValue();

			//Align
			new SBDAlignment<Person>().align(
					getPersons(), 
					new ActiveMultiFilter(ageFrom, ageTo, gender), 
					new AlignmentProbabilityClosure<Person>() {

						@Override
						public double getProbability(Person agent) {
							return agent.computeWorkProb();
						}

						@Override
						public void align(Person agent, double alignedProabability) {
							if ( RegressionUtils.event( alignedProabability, SimulationEngine.getRnd() ) )
							{
								agent.setWorkState(WorkState.Employed);
							}
							else agent.setWorkState(WorkState.NotEmployed);
						}
					}, inWorkTarget); 				
		}
//		System.out.println("inWork aligned.");
	}

	private void marriageMatching() {
		
		//Compute age average for difficult match
		final AverageClosure averageAge = new AverageClosure() {				
			@Override
			public void execute(Object input) {
				add( ((Person) input).getAge() );					
			}
		};
		
		Aggregate.applyToFilter(getPersons(), new FemaleToCoupleFilter(), averageAge);
		
		// Do matching
		SimpleMatching.getInstance().matching(
			getPersons(), new FemaleToCoupleFilter(), new Comparator<Person>() {	
				@Override
				public int compare(Person female1, Person female2) {					
					return (int) Math.signum(Math.abs(female1.getAge() - averageAge.getAverage()) - 
									Math.abs(female2.getAge() - averageAge.getAverage())	
							);					
				}			
			}, 
			getPersons(), new MaleToCoupleFilter(), new MatchingScoreClosure<Person>() {				
				@Override
				public Double getValue(Person female, Person male) {
					return female.getMarriageScore(male);
				}
			}, 
			new MatchingClosure<Person>() {				
				@Override
				public void match(Person female, Person male) {		//The SimpleMatching.getInstance().matching() assumes the first collection in the argument (females in this case) is also the collection that the first argument of the MatchingClosure.match() is sampled from.
					
					female.marry(male);				
					male.marry(female);
				}
			}
		);	
//		System.out.println("Marriage matched.");
	}
	

	private void printElapsedTime() {
		methodId++;
		long timeDiff = System.currentTimeMillis() - elapsedTime;
		System.out.println("Year " + year + " Method "+ (((methodId-1) % 9)+1) +" completed in " + timeDiff + "ms.");
		elapsedTime = System.currentTimeMillis();
	}

	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------
	
	public Double getStartYear() {
		return startYear;
	}

	public List<Person> getPersons() {
		return persons;
	}
	
	public List<Household> getHouseholds() {
		return households;
	}
	
	public Double getEndYear() {
		return endYear;
	}

	public Integer getWemra() {
		return wemra;
	}
	
	public Person getPerson(Long id) {
		
		for (Person person : persons) {
			if ((person.getId() != null) && (person.getId().getId().equals(id)))
				return person;
		}
		throw new IllegalArgumentException("Person with id " + id + " is not present!");		
	}
	
	public Household getHousehold(Long id) {
		
		for (Household household : households) {
			if  (household.getId() != null && household.getId().getId().equals(id) )
				return household;
		}
		
		throw new IllegalArgumentException("Household with id " + id + " is not present!");		
	}
	
	public boolean removePerson(Person person) {
		return persons.remove(person);
	}
	
	public boolean removeHousehold(Household household) {
		return households.remove(household);
	}

	public Boolean getPrintElapsedTime() {
		return printElapsedTime;
	}

	public void setPrintElapsedTime(Boolean printElapsedTime) {
		this.printElapsedTime = printElapsedTime;
	}	

	
}
