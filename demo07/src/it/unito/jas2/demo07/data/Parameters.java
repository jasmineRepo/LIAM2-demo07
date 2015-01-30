package it.unito.jas2.demo07.data;

//import it.zero11.microsim.data.MultiKeyCoefficientMap;
import it.unito.jas2.demo07.algorithms.MultiKeyCoefficientMap;
//import it.zero11.microsim.data.excel.ExcelAssistant;
import it.unito.jas2.demo07.algorithms.ExcelAssistant;
import it.unito.jas2.demo07.algorithms.LinearRegression;
import it.unito.jas2.demo07.algorithms.LogitRegression;


public class Parameters {
	// probabilities
	private static MultiKeyCoefficientMap pBirth;
	private static MultiKeyCoefficientMap pDeathM;
	private static MultiKeyCoefficientMap pDeathF;
	private static MultiKeyCoefficientMap pDivorce;
	private static MultiKeyCoefficientMap pInWork;
	private static MultiKeyCoefficientMap pMarriage;

	// regression coefficients
	private static MultiKeyCoefficientMap coeffMarriageFit;
	private static MultiKeyCoefficientMap coeffDivorce;
	private static MultiKeyCoefficientMap coeffInWork;
	
	// regression objects
	private static LinearRegression regMarriageFit;
	private static LogitRegression regDivorce;
	private static LogitRegression regInWork;
	
	
	public static void loadParameters() {
		
		// probabilities
		pBirth = ExcelAssistant.loadCoefficientMap("input/p_birth.xls", "Foglio1", 1, 59);
		pDeathM = ExcelAssistant.loadCoefficientMap("input/p_death_m.xls", "Foglio1", 1, 59);
		pDeathF = ExcelAssistant.loadCoefficientMap("input/p_death_f.xls", "Foglio1", 1, 59);
		pMarriage = ExcelAssistant.loadCoefficientMap("input/p_marriage.xls", "Foglio1", 3, 4);
		pDivorce = ExcelAssistant.loadCoefficientMap("input/p_divorce.xls", "Foglio1", 2, 59);
		pInWork = ExcelAssistant.loadCoefficientMap("input/p_inwork.xls", "Foglio1", 3, 59);
		
		// regression coefficients
		coeffMarriageFit = ExcelAssistant.loadCoefficientMap("input/reg_marriage.xls", "Foglio1", 1, 1);
		coeffDivorce = ExcelAssistant.loadCoefficientMap("input/reg_divorce.xls", "Foglio1", 1, 1);
		coeffInWork = ExcelAssistant.loadCoefficientMap("input/reg_inwork.xls", "Foglio1", 3, 1);
		
		// definition of regression models		
		regMarriageFit = new LinearRegression(coeffMarriageFit);
		regDivorce = new LogitRegression(coeffDivorce);
		regInWork = new LogitRegression(coeffInWork);

	}

	// getters
	public static MultiKeyCoefficientMap getpBirth() { return pBirth; }
	public static MultiKeyCoefficientMap getpDeathM() { return pDeathM; }
	public static MultiKeyCoefficientMap getpDeathF() { return pDeathF;	}
	public static MultiKeyCoefficientMap getpDivorce() { return pDivorce; }	
	public static MultiKeyCoefficientMap getpInWork() { return pInWork; }
	public static MultiKeyCoefficientMap getpMarriage() { return pMarriage; }

	public static MultiKeyCoefficientMap getCoeffMarriageFit() { return coeffMarriageFit; }
	public static MultiKeyCoefficientMap getCoeffDivorce() { return coeffDivorce; }
	public static MultiKeyCoefficientMap getCoeffInWork() { return coeffInWork; }
	
	public static LinearRegression getRegMarriageFit() { return regMarriageFit; }
	public static LogitRegression getRegDivorce() { return regDivorce; }
	public static LogitRegression getRegInWork() { return regInWork; }
	
}
