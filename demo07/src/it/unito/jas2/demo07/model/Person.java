package it.unito.jas2.demo07.model;

import it.unito.jas2.demo07.algorithms.IObjectSource;
import it.unito.jas2.demo07.algorithms.MapAgeSearch;
import it.unito.jas2.demo07.algorithms.RegressionUtils;
import it.unito.jas2.demo07.data.Parameters;
import it.unito.jas2.demo07.model.enums.CivilState;
import it.unito.jas2.demo07.model.enums.Education;
import it.unito.jas2.demo07.model.enums.Gender;
import it.unito.jas2.demo07.model.enums.WorkState;
import it.unito.jas2.demo07.algorithms.MultiKeyCoefficientMap;
//import it.zero11.microsim.data.MultiKeyCoefficientMap;
import it.zero11.microsim.data.db.PanelEntityKey;
import it.zero11.microsim.engine.SimulationEngine;
import it.zero11.microsim.event.EventListener;
import it.zero11.microsim.statistics.IDoubleSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.jfree.util.Log;

@Entity
public class Person implements Comparable<Person>, EventListener, IDoubleSource, IObjectSource
{
	public static long personIdCounter = 100000;
	
	@Transient
	private PersonsModel model;
	
	@Id
	private PanelEntityKey id;
		
	private int age;
	
	@Enumerated(EnumType.STRING)
	private Gender gender;
	
	@Column(name="workstate")			//TODO: Can we remove this column naming?
	@Enumerated(EnumType.STRING)
	private WorkState workState;

	@Enumerated(EnumType.STRING)
	private CivilState civilState;

	@Column(name="dur_in_couple")
	private Integer durationInCouple;

	@Column(name="mother_id")
	private Long motherId;
	
	@Transient
	private Person mother;
	
	@Column(name="partner_id")
	private Long partnerId;
	
	@Transient
	private Person partner;
	
	@Column(name="hh_id")
	private long householdId;

	@Transient
	private Household household;
	
	@Column(name="alone")
	private Boolean alone;

//	@Transient
//	@Column(name="age_group_work")		//Why define the column heading for a Transient field (the same in the field below)?
//	private Integer ageGroupWork;		//TODO: remove if not being used
//
//	@Transient
//	@Column(name="age_group_civilstate")	//Why define the column heading for a Transient field?
//	private Integer ageGroupCivilState;		//TODO: remove if not being used

	@Enumerated(EnumType.STRING)
	private Education educationlevel;

	@Transient
	private boolean toCouple;

	@Transient
	private boolean toDivorce;
	
	@Transient
	private double divorceProb;

	@Transient
	private double workProb;
	
//	@Transient
//	private long potentialPartnerId;
	
	@Transient
	private Person potentialPartner;

//	@Transient
//	private LaggedVariables lagged;
	
	
	// ---------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------

	//Used when loading the initial population from the input database
	public Person() {
		super();
		model = (PersonsModel) SimulationEngine.getInstance().getManager(PersonsModel.class.getCanonicalName());		
	}
	
	//Used when creating new people during the birth process
	public Person( long idNumber) {
		this();
		
		id = new PanelEntityKey();
		id.setId(idNumber);		 	
	}
	

	// ---------------------------------------------------------------------
	// Event Listener
	// ---------------------------------------------------------------------
	
	public enum Processes {
		Ageing,
		Death,	
		Birth,
		ToCouple,
		Divorce,
		GetALife,
		InEducation;
	}
		
	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case Ageing:
//			System.out.println("Ageing");
			ageing();		
			break;
		case Death:
//			System.out.println("Death");
			death();
			break;
		case Birth:
//			System.out.println("Birth");
			birth();
			break;	
		case ToCouple:
//			System.out.println("ToCouple");
			toCouple();
			break;	
		case Divorce:
//			System.out.println("Divorce");
			divorce();
			break;	
		case GetALife:
//			System.out.println("GetALife");
			getALife();
			break;			
		case InEducation:
//			System.out.println("InEducation");
			inEducation();
			break;
		}
	}

	// ---------------------------------------------------------------------
	// implements IObjectSource for use with Regression classes
	// ---------------------------------------------------------------------	
		
	public enum RegressionKeys {
		gender,
		workState,
	}

	public Object getObjectValue(Enum<?> variableID) {
		switch ((RegressionKeys) variableID) {
		
		//For marriage regression
		case gender:
			return gender;
		case workState:
			return workState;
		default:
			throw new IllegalArgumentException("Unsupported regressor " + variableID.name() + " in Person#getObjectValue");
		}
	}
	
	
	// ---------------------------------------------------------------------
	// implements IDoubleSource for use with Regression classes
	// ---------------------------------------------------------------------	
	
	public enum Regressors {
		// For marriage regression, check with potential partner's properties
		potentialPartnerAge, potentialPartnerAgeSq,	potentialPartnerAgeCub,
		potentialAgeDiff, potentialAgeDiffSq, potentialAgeDiffCub,
		inWorkAndPotentialPartnerInWork, notInWorkAndPotentialPartnerInWork, inWorkAndPotentialPartnerNotInWork,
		// For divorce regression
		nChildren,
		durationInCouple,		//Is it necessary to include this here, as it is already an attribute of person?  Should we include for consistency?
		ageDiff, ageDiffSq,	ageDiffCub,
		bothWork,
		divorceIntercept,
		//For in work regression
		age, ageSq,	ageCub,
		isMarried,
		workIntercept;
	}
	
	public double getDoubleValue(Enum<?> variableID) {
		
		switch ((Regressors) variableID) {
					
		//For marriage regression
		case potentialPartnerAge:
			return getPotentialPartnerAge();
		case potentialPartnerAgeSq:
			return getPotentialPartnerAge() * getPotentialPartnerAge();
		case potentialPartnerAgeCub:
			return getPotentialPartnerAge() * getPotentialPartnerAge() * getPotentialPartnerAge();
		case potentialAgeDiff:
			return getPotentialAgeDiff();
		case potentialAgeDiffSq:
			return getPotentialAgeDiff() * getPotentialAgeDiff();
		case potentialAgeDiffCub:
			return getPotentialAgeDiff() * getPotentialAgeDiff() * getPotentialAgeDiff();
		case inWorkAndPotentialPartnerInWork:
			return getInWorkAndPotentialPartnerInWork();
		case notInWorkAndPotentialPartnerInWork:
			return getNotInWorkAndPotentialPartnerInWork();
		case inWorkAndPotentialPartnerNotInWork:
			return getInWorkAndPotentialPartnerNotInWork();
			
		//For divorce regression
		case nChildren:
			return getNbChildren();
		case durationInCouple:
			return (double)getDurationInCouple();
		case ageDiff:
			return getAgeDiff();
		case ageDiffSq:
			return getAgeDiff()*getAgeDiff();
		case ageDiffCub:
			return getAgeDiff()*getAgeDiff()*getAgeDiff();
		case bothWork:
			return getBothWork();			
		case divorceIntercept:
			return 1.;			//Is the constant intercept, so regression coefficient is multiplied by 1
			
		//For work regression
		case age:
			return (double) age;
		case ageSq:
			return (double) age * age;
		case ageCub:
			return (double) age * age * age;
		case isMarried:
			return civilState.equals(CivilState.Married)? 1. : 0.;
		case workIntercept:
			return 1.;			//Is the constant intercept, so regression coefficient is multiplied by 1
			
		default:
			throw new IllegalArgumentException("Unsupported regressor " + variableID.name() + " in Person#getDoubleValue");
		}
	}
	
	public double getPartnerAge()
	{
//		if(partnerId != null)
//		{
			return partner.getAge();	
//		}
//		else return 0.;
				
	}

	public double getPotentialPartnerAge()
	{
//		if(potentialPartnerId != null)
//		{
			return potentialPartner.getAge();
//		}
//		else return 0.;
	}
	
	public double getPotentialAgeDiff()
	{
//		if(potentialPartnerId != null)
//		{
			return (double)(age - potentialPartner.getAge());
//		}
//		else return 0.;
	}
	// ---------------------------------------------------------------------
	// own methods
	// ---------------------------------------------------------------------

	protected void ageing() {
//		if (age<100) {			//Why have this 'artificial' age restriction?  The probability of death is 1.0 for people aged 99 and over, so people should be removed from the simulation anyway when they reach 100, so why worry about putting in this condition?
			age += 1;
			
			//TODO: The ageGroup fields do not appear to be used anywhere in the simulation (and are @Transient).  Should we remove them?
//			ageGroupCivilState = (age < 50 ? age - age % 5 : age - age % 10);		//This puts the person into a higher Civil State 'bin' after every 5 years of ageing up to 50 years old, then after every 10 years for 50 years or older.  I.e. 23 year old has ageGroupCivilState of 20, a 26 year old has ageGroupCivilState of 25 etc.    
//			ageGroupWork = (age < 70 ? 	age - age % 5 : 70);						//This does similar thing as to ageGroupCivilState up to the age of 70 (people older than 65 are retired)

//		}
		
		//Retire person if age equals retirement age (or older for initial population)
		if(!this.getWorkState().equals(WorkState.Retired)) {
			if ( (this.getGender().equals(Gender.Male) && (this.getAge() >= 65)) || (this.getGender().equals(Gender.Female) && (this.getAge() >= model.getWemra())))		
			{			
				setWorkState(WorkState.Retired);
			}
		}

//		lagged.push("workState", workState);		//Records the workState once per year at the moment when the ageing() method is called.
		
		if (civilState.equals(CivilState.Married))
		{
			setDurationInCouple(getDurationInCouple() + 1);
		}
		
	}
	
	protected void death() {
		
		MultiKeyCoefficientMap map = ( gender.equals(Gender.Male) ? Parameters.getpDeathM() : Parameters.getpDeathF() ) ;
		
		double deathProbability = ((Number) map.getValue(this.age, SimulationEngine.getInstance().getTime())).doubleValue();
		
		if ( RegressionUtils.event(deathProbability) ) {
			// update partner's status
			if (this.getCivilState().equals(CivilState.Married)) {			//if (this.getPartnerId() != null) { 
//				Person partner = model.getPerson(partnerId);				//Throws illegal argument exception if partnerId doesn't exist
				if(partner == null) {
					System.out.println("id ," + this.getId().getId() + ", partnerId ," + partnerId + ", partner ," + partner);
				}
				partner.setCivilState(CivilState.Widow);
//				partner.setPartnerId(null);
				partner.setPartner(null);
			}
			// remove from household (this removes household if no other members are left)
//			Household hh = model.getHousehold(householdId);
			household.removePerson(this);
			household = null;
			
			mother = null;
			// remove from model
			model.removePerson(this);
		}
						
	}

	// Setup own household if aged 24 or over and not married (and still living with others)
	protected void getALife() {

		//Bug where householdId still in existence after house has been removed...?
//		setAlone(model.getHousehold(householdId).getHouseholdMembers().size() == 1);		//Throws illegal argument exception if householdID doesn't exist
		setAlone(household.getHouseholdMembers().size() == 1);
		
		if (!(civilState.equals(CivilState.Married)) && !(alone) && (age >= 24)) {
			
			// create new household
//			Household newHousehold = new Household( (Household.householdIdCounter)++ );
	        
	        resetHousehold(new Household( (Household.householdIdCounter)++));
	        model.getHouseholds().add(household);
	        // record household id
//	        setHouseholdId(newHousehold.getId().getId());		//Now within setHousehold
	        

		}
		
	}
	
	protected void birth() {
		if ((this.gender.equals(Gender.Female)) && (this.age >= 15) && (this.age <= 50)) {
			double birthProbability;
			try{
				birthProbability = ((Number) Parameters.getpBirth().getValue(this.age, SimulationEngine.getInstance().getTime())).doubleValue();
				if ( RegressionUtils.event(birthProbability) ) {
					
					Person newborn = new Person( (Person.personIdCounter)++ );
					newborn.setAge(0);			//Why aren't all these person attributes set in the constructor?  TODO: Move them to constructor if possible
//					newborn.setMotherId(this.getId().getId());
					newborn.setMother(this);
//					newborn.setHouseholdId(this.getHouseholdId());
					newborn.setHousehold(this.household);
					newborn.setGender( RegressionUtils.event(Gender.class, new double[] {0.49, 0.51}) );		//0.49 for females, 0.51 for males.  As I swapped enum definition of gender around to be consistent with input data, this also needs to be swapped.
					newborn.setEducationlevel( RegressionUtils.event(Education.class, new double[] {0.25, 0.39, 0.36}) );  // RMK: education is predetermined at birth
					newborn.setCivilState(CivilState.Single);
					newborn.setWorkState(WorkState.Student);

					//Add newborn to the model and add it to the mother's household
					model.getPersons().add(newborn);
//					model.getHousehold(householdId).addPerson(newborn);		//Done within setHoushold() now					
				}
			} catch(Exception e) {
				Log.error("birth exception " + this.age);
			}
			
		}			

	}	
	
	// Rmk: this method is applied to both males and females as both have to "break free"
	protected void divorce() {

		if (getPartnerId() != null) {
//			Person partner = model.getPerson(getPartnerId());
			if (this.getToDivorce() || partner.getToDivorce()) {				//The first condition used the toDivorce boolean flag directly, but caused an Exception as it can be null.  Instead, if we use this.getToDivorce(), null values are caught by the getter.			

				//# break link to partner
				setPartnerId(null);
				setCivilState(CivilState.Divorced);
				setDurationInCouple(0);

				//# move out males (females retain their own household id)
				if (getGender().equals(Gender.Male) && this.getToDivorce()) {

					// create new household
//					long newHouseholdId = (Household.householdIdCounter)++;
					resetHousehold(new Household( (Household.householdIdCounter)++ ));		//TODO: make a model.createNewHousehold() method so that we can make these 2 lines of code a simple call to the new method.  Shouldn't have to explicitly add the household to the model
					model.getHouseholds().add(household);
//					setHouseholdId(newHouseholdId);		// record household id
				}		
			}
		}
	}
	
	protected void toCouple() {
		
		toCouple = false;
		if (age >= 18 && age <= 90 && !civilState.equals(CivilState.Married)) {
		
			MultiKeyCoefficientMap map = Parameters.getpMarriage();
			double coupleProbability = MapAgeSearch.getValue(map, getAge(), getGender(), getCivilState().toString());	
			toCouple = ( RegressionUtils.event(coupleProbability) );
		}	
		
	}
	
	protected void inEducation() {
		//# unemployed if left education
		if ( workState != null ) 
			if ( workState.equals(WorkState.Student) ) {
				if ( 
						(age >= 16 && educationlevel.equals(Education.LowerSecondary)) ||
						(age >= 19 && educationlevel.equals(Education.UpperSecondary)) ||
						(age >= 24 && educationlevel.equals(Education.Tertiary)) 
						) {
					setWorkState(WorkState.NotEmployed);					
				}
			}
	}
	
	public double getMarriageScore(Person potentialPartner) {
				
//		this.setPotentialPartnerId(potentialPartner.getId().getId()); 	//Set Person#potentialPartnerId field, to calculate regression score for potential match between this person and potential partner.
		this.setPotentialPartner(potentialPartner); 	//Set Person#potentialPartnerId field, to calculate regression score for potential match between this person and potential partner.
//		double marriageScore = Parameters.getRegMarriageFit().getScore(this, Person.Regressors.class, this, Person.RegressionKeys.class);
		double marriageScore = Parameters.getRegMarriageFit().getScore(this, Person.Regressors.class);
//		this.setPotentialPartnerId(-1);		//After regression, set to null, ready for calculating regression with next potential partner candidate.// Now set to -1 as null not allowed now that potentialPartnerId is a primitive.  -1 should indicate a problem as id should be non-negative
		
		return marriageScore;
	}
	
	public void marry(Person partner) {
		
		this.partner = partner;
		setPartnerId(partner.getId().getId());

		if (gender.equals(Gender.Female)) {			//TODO: Female partner should always be called before male partner, given the ordering of the collection of females and males supplied to the matching() method in PersonsModel#marriageMatching().  But do we want to add a check?

			// create new household
//			long newHouseholdId = (Household.householdIdCounter)++;
			resetHousehold(new Household( (Household.householdIdCounter)++ ));		//TODO: make a model.createNewHousehold() method so that we can make these 2 lines of code a simple call to the new method.  Shouldn't have to explicitly add the household to the model
			model.getHouseholds().add(household);
//			setHouseholdId(newHouseholdId);		// record household id
			
//			// create new household
//			Household newHousehold = new Household( (Household.householdIdCounter)++ );
//			model.getHouseholds().add(newHousehold);
//
//			// record household id
//			setHouseholdId(newHousehold.getId().getId());			//Whenever we setHouseholdId, we automatically remove the person from the previous house and add to the new house!  There used to be an IllegalArgumentException as the person was explicitly removed from the household at the beginning of this marry() method! 

		} else {
//			setHouseholdId(model.getPerson(getPartnerId()).getHouseholdId());
//			setHouseholdId(partner.getHouseholdId());
			resetHousehold(partner.getHousehold());
		}

		setCivilState(CivilState.Married);
		setDurationInCouple(0);	
	}

	// this method returns a double in order to allow invocation by the model in the alignment closure
	public double computeDivorceProb() {

//		double divorceProb = Parameters.getRegDivorce().getProbability(this, Person.Regressors.class, this, Person.RegressionKeys.class);
		double divorceProb = Parameters.getRegDivorce().getProbability(this, Person.Regressors.class);
		if (divorceProb < 0 || divorceProb > 1) {
			Log.error("divorce prob. not in range [0,1]");
		}
		
		return divorceProb;
	}		
		
	public boolean atRiskOfWork() {
		return (age > 15 && age < ( gender.equals(Gender.Male) ? 65 : model.getWemra() ) 
				&& !workState.equals(WorkState.Student) 
				&& !workState.equals(WorkState.Retired)		//Why check this here?  Shouldn't workState only be retired if age doesn't satisfy first condition?  Perhaps remove first condition for neatness... 
				);			
	}
	

	// this method returns a double in order to allow invocation by the model in the alignment closure
	protected double computeWorkProb() {

		double workProb = -1;

		if(atRiskOfWork()) {
			workProb = Parameters.getRegInWork().getProbability(this, Person.Regressors.class, this, Person.RegressionKeys.class);		//Has multiple keys, so use the IObjectSource mechanism (instead of the slow reflection mechanism)
//			System.out.println("computeWorkProb");
//			workProb = Parameters.getRegInWork().getProbability(this, Person.Regressors.class);			//Would use reflection in order to work (so slower)
		}
		if (workProb < 0 || workProb > 1) {
			Log.error("work prob. not in range [0,1]");
		}
		return workProb;
		
	}
	
	// ---------------------------------------------------------------------
	// Comparator
	// ---------------------------------------------------------------------
	// Person comparators are defined "on the fly" with closures in Model.marriageMatching(), 
	// but a compareTo method has to be defined as the class implements the Comparable interface. 
	
	@Override
	public int compareTo(Person p) {

			return -1;
	}
	
	
	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	
	public PanelEntityKey getId() {
		return id;
	}

	public void setId(PanelEntityKey id) {
		this.id = id;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public WorkState getWorkState() {
		return workState;
	}
	
	public int getEmployed()
	{
		if (workState.equals(WorkState.Employed)) return 1;
		else return 0;
	}
	
	public int getNonEmployed()
	{
		if (workState.equals(WorkState.NotEmployed)) return 1;
		else return 0;
	}
	
	public int getRetired()
	{
		if (workState.equals(WorkState.Retired)) return 1;
		else return 0;
	}
	
	public int getStudent()
	{
		if (workState.equals(WorkState.Student)) return 1;
		else return 0;
	}

//	public LaggedVariables getLagged() {
//		return lagged;
//	}

	public void setWorkState(WorkState workState) {
		this.workState = workState;
	}

	public CivilState getCivilState() {
		return civilState;
	}

	public void setCivilState(CivilState civilState) {
		this.civilState = civilState;
	}

	public Integer getDurationInCouple() {
		return (durationInCouple != null ? durationInCouple : 0);
	}

	public void setDurationInCouple(Integer durationInCouple) {
		this.durationInCouple = durationInCouple;
	}

	public Long getMotherId() {
		return motherId;
	}

	public void setMotherId(Long motherId) {
		this.motherId = motherId;
	}

	public Long getPartnerId() {
		return partnerId;
	}
	
 	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}
 	
// 	public long getPotentialPartnerId() {
//		return potentialPartnerId;
//	}
	
// 	public void setPotentialPartnerId(long potentialPartnerId) {
//		this.potentialPartnerId = potentialPartnerId;
//	}

	public long getHouseholdId() {
		return householdId;
	}

	public void setHouseholdId(long householdId) {
//		if (this.householdId != null) 
//			model.getHousehold(this.householdId).removePerson(this);		//Now done in setHousehold()
		this.householdId = householdId;
//		model.getHousehold(householdId).addPerson(this);					//Now done in setHousehold()
		
	}

//	public Integer getAgeGroupWork() {
//		return ageGroupWork;
//	}
//
//	public void setAgeGroupWork(Integer ageGroupWork) {
//		this.ageGroupWork = ageGroupWork;
//	}
//
//	public Integer getAgeGroupCivilState() {
//		return ageGroupCivilState;
//	}
//
//	public void setAgeGroupCivilState(Integer ageGroupCivilState) {
//		this.ageGroupCivilState = ageGroupCivilState;
//	}

	public Education getEducationlevel() {
		return educationlevel;
	}
	
	public int getLowEducation() {
		if (educationlevel.equals(Education.LowerSecondary)) return 1;
		else return 0;
	}
	
	public int getMidEducation() {
		if (educationlevel.equals(Education.UpperSecondary)) return 1;
		else return 0;
	}
	
	public int getHighEducation() {
		if (educationlevel.equals(Education.Tertiary)) return 1;
		else return 0;
	}

	public void setEducationlevel(Education educationlevel) {
		this.educationlevel = educationlevel;
	}

	public Boolean getAlone() {
		return (alone != null ? alone : false);
	}

	public void setAlone(Boolean alone) {
		this.alone = alone;
	}

	public boolean getToDivorce() {
//		return (toDivorce != null ? toDivorce : false);
		return toDivorce;
	}

	public void setToDivorce(boolean toDivorce) {
		this.toDivorce = toDivorce;
	}

	public boolean getToCouple() {
//		return (toCouple != null ? toCouple : false);
		return toCouple;
	}

	public void setToCouple(boolean toCouple) {
		this.toCouple = toCouple;
	}
	
	public double getWorkProb() {
		return workProb;
	}

	public double getDivorceProb() {
		return divorceProb;
	}
	
	public void setDivorceProb(double p) {
		divorceProb = p;
	}
	
	private double getBothWork() {
//		if(partnerId != null)
//		{
			return (getWorkState().equals(WorkState.Employed) && partner.getWorkState().equals(WorkState.Employed) ? 1.0 : 0.0);	
//		}
//		else return 0.;		
	}
		
	private double getNotInWorkAndPotentialPartnerInWork() {
//		if(potentialPartnerId != null)
//		{
			return (!getWorkState().equals(WorkState.Employed) && potentialPartner.getWorkState().equals(WorkState.Employed) ? 1.0 : 0.0);
//		}
//		else return 0.;
	}

	private double getInWorkAndPotentialPartnerNotInWork() {
//		if(potentialPartnerId != null)
//		{
			return (getWorkState().equals(WorkState.Employed) && !potentialPartner.getWorkState().equals(WorkState.Employed) ? 1.0 : 0.0);
//		}
//		else return 0.;
	}
	
	private double getInWorkAndPotentialPartnerInWork() {
//		if(potentialPartnerId != null)
//		{
			return (getWorkState().equals(WorkState.Employed) && potentialPartner.getWorkState().equals(WorkState.Employed) ? 1.0 : 0.0);	
//		}
//		else return 0.;		
	}

	public double getAgeDiff() {
//		if(partnerId != null)
//		{
			return (double)(age - partner.getAge());	
//		}
//		else return 0.; 
	}

	public double getNbChildren() {
		if(this.gender == Gender.Female)		//We assume it doesn't matter whether the current partner is also the biological parent of the children in the household.  This is justified, as the divorceRegression is only called by females, who keep their household on divorce and who never live with children whose mother is another female.  TODO:CHECK!!!
		{										//Another assumption is that, in the divorce regression, when the number of children is taken into account, this is only measuring the sons and daughters under 18 years old, and that sons and daughters of age 18 or over have no influence in whether a couple decide to divorce.
			return (double) household.getNbChildren();	//Another slower way would be to loop through all people and add up the number of people whose motherId matches this person.
		}
		else return 0.;		
	}

	public Person getMother() {
		return mother;
	}

	public void setMother(Person mother) {
		this.setMotherId(mother.getId().getId());
		this.mother = mother;
	}

	public Person getPartner() {
		return partner;
	}

	public void setPartner(Person partner) {
		if(partner != null) {
			this.setPartnerId(partner.getId().getId());
		}
		else {
			partnerId = null;
		}
		this.partner = partner;
	}

	public Household getHousehold() {
		return household;
	}
	
	public void setHousehold(Household household) {
		this.household = household;
		this.householdId = household.getId().getId();
	}

	public void resetHousehold(Household household) {
		this.household.removePerson(this);
		this.household = household;
		household.addPerson(this);
		this.householdId = household.getId().getId();
	}

	public Person getPotentialPartner() {
		return potentialPartner;
	}

	public void setPotentialPartner(Person potentialPartner) {
		this.potentialPartner = potentialPartner;
	}	
	
}