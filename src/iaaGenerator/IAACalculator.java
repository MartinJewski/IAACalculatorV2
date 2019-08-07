package iaaGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.dkpro.statistics.agreement.coding.BennettSAgreement;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.CohenKappaAgreement;
import org.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import org.dkpro.statistics.agreement.coding.HubertKappaAgreement;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.coding.PercentageAgreement;
import org.dkpro.statistics.agreement.coding.RandolphKappaAgreement;
import org.dkpro.statistics.agreement.coding.ScottPiAgreement;
import org.dkpro.statistics.agreement.coding.WeightedKappaAgreement;

import Annotators.Annotator;
import Annotators.AnnotatorManager;
import Display.Show;

public class IAACalculator {

	public static enum IAAMethods{
							 Percentage,
							 Bennetts,
							 RandolphKappa,
							 FleissKappa};
	IAAExtractor _extractor;

	private List<String[]> _numSensesPV = new ArrayList<String[]>();
	private List<String[]> _numSentencesPV = new ArrayList<String[]>();


	/**
	 * Constructor that initializes the calculator with an extractor
	 * to deliver methods to calculate the IAA for a set(two or more) of annotator's.
	 * 
	 * @param extractor
	 */
	public IAACalculator(IAAExtractor extractor, AnnotatorManager annoManager) {
		_extractor = extractor;
		
		this.calculateNumbPV();
		
	}
	
	private void calculateNumbPV() {
		
	
		for(String key : _extractor.getAllVerbs()) {
			String[] numSentences = new String[2];
			String[] numSenses = new String[2];
			
			numSentences[0] = key;
			numSentences[1] = Integer.toString(_extractor.getPackedVerbs().get(key).size());
			_numSentencesPV.add(numSentences);
			
			numSenses[0] = key;
			numSenses[1] = Integer.toString(_extractor.getSensesPerVerb().get(key).size());
			_numSensesPV.add(numSenses);
		}
		
	}
	
	/**
	 * Calculates the ratio between verbs per sentence and senses per Verb.
	 * Important: the List must have a the following structure:
	 * [name, value] 
	 * and both list must have the same length.
	 * 
	 * -hasHeader is the first row (pos 0) that contains the column definitions;
	 * 		
	 * @param sentencePV the calculated sentence per verb inside a List
	 * @param sensesPV the calculated senses per verb inside a List
	 * @param hasHeaderX true if x list has a header, false if not.
	 * @param hasHeaderY true if y list has a header, false if not.
	 * @return a List with ratios
	 */
	public Double calculateCorr(List<String[]> xVal, List<String[]> yVal){

		PearsonsCorrelation perCor = new  PearsonsCorrelation();
		double[] x = new double[xVal.size()];
		double[] y = new double[yVal.size()];
		
		for(int i = 0; i < xVal.size(); i++) {
			x[i] = Double.parseDouble(xVal.get(i)[1]); 
			y[i] = Double.parseDouble(yVal.get(i)[1]);
		}
		
		
		return perCor.correlation(x, y);
	}
	
	
	
	/**
	 *Chooses a desired IAA method and calculates the agreement value.
	 * By Default it uses Percentage Agreement
	 * @param study the study that contains all rows from your tsv
	 * @param iaaMethod an enum that represents the name of the algorithm
	 * 				(e.g Percentage for Percentage agreement)
	 * @return the calculated IAA agreement value 
	 */
	private Double useIAAAlgorithm(CodingAnnotationStudy study, IAACalculator.IAAMethods iaaMethod,
			int numAnnotators) {
		
		Double iaaResult;
		switch(iaaMethod) 
		{
			case Percentage:
				PercentageAgreement pa = new PercentageAgreement(study);
				iaaResult = pa.calculateAgreement();
				break;
		
			case Bennetts:
				if(numAnnotators > 2) {
					System.out.println("Bennetts algorithm is not applicable for more than 2 annotators");
					iaaResult = -999.9;//very large negative to show an error
					break;
				}
				BennettSAgreement bs = new BennettSAgreement (study);
				iaaResult = bs.calculateAgreement();
				break;

			case RandolphKappa:	
				RandolphKappaAgreement  rka = new RandolphKappaAgreement (study);
				iaaResult = rka.calculateAgreement();
				break;

			default:
				PercentageAgreement paDefault = new PercentageAgreement(study);
				iaaResult = paDefault.calculateAgreement();
				break;
		}
		
		return iaaResult;
	}
	
	/**
	 * Calculates how they annotator's agreed in general.
	 * calculates the iaa for many annotator's over all given sentences.
	 * 
	 * 
	 * @param annoManager
	 * @param sentences
	 */
	public Double calculateIAAOverall(AnnotatorManager annoManager, List<String[]> sentences, 
					IAACalculator.IAAMethods iaaMethod) {
		
		//initialize a new study with n annotator's
		CodingAnnotationStudy study = new CodingAnnotationStudy(annoManager.getAllAnnotators().size());
		
		//extend current study with the senses as categories
		//since we compare senses
		study = this.addCategoriesToStudy(study);
		
		//for every row in parsedTSV except first row
		for(int i = 0; i < sentences.size(); i++) {
			
			//obj that holds the annotation for the annotator
			Object[] objArray = new Object[annoManager.getAllAnnotators().size()];
			
			int pos = 0;
			for(Annotator anno : annoManager.getAllAnnotators()) {
				//get the annotation for every annotator
				objArray[pos] = sentences.get(i)[anno.getColumn()];
				pos++;
			}
			study.addItemAsArray(objArray);
		}
		
		double result = this.useIAAAlgorithm(study, iaaMethod , annoManager.getAllAnnotators().size());
		return result;
	}
	
	//-------------------------------------------------test------
	/**
	 * calculates the iaa for many annotator's.
	 * 
	 * 
	 * @param annoManager
	 * @param sentences
	 */
	public Double calculateIAAOverallCF(AnnotatorManager annoManager, List<String[]> sentences, 
					IAACalculator.IAAMethods iaaMethod, String key) {
		
		//initialize a new study with n annotator's
		CodingAnnotationStudy study = new CodingAnnotationStudy(annoManager.getAllAnnotators().size());
		
		double result;
		//extend current study with the senses as categories
		//since we compare senses
		study = this.addCategoriesToStudyAdvanced(study, key);
		
		if(study.getCategoryCount() > 1) { //we need at least 2 categories for an iaa calculation
			//for every row in parsedTSV except first row
			for(int i = 0; i < sentences.size(); i++) {
				
				//obj that holds the annotation for the annotator
				Object[] objArray = new Object[annoManager.getAllAnnotators().size()];
				
				int pos = 0;
				for(Annotator anno : annoManager.getAllAnnotators()) {
					//get the annotation for every annotator
					objArray[pos] = sentences.get(i)[anno.getColumn()];
					pos++;
				}
				study.addItemAsArray(objArray);
			}
			result = this.useIAAAlgorithm(study, iaaMethod , annoManager.getAllAnnotators().size());
		}else {
			result = 1.0; //with only one category, there must be a 100% agreement since
			//annotator's cannot chose something else
		}
		
		
		return result;
	}
	
	
	public List<List<String[]>> calculateIAAPairwiseCF(AnnotatorManager annoManager, 
					List<String[]> sentences, IAACalculator.IAAMethods iaaMethod, boolean overAllRows,
					String key) {
		//saves all pairwise agreements for every annotator
		//with the annotator's name as key for his results
		List<List<String[]>> pairwiseAgreement = new ArrayList<List<String[]>>();
		//double for loop to calculate an iaa calculate pairwise
		for(Annotator anno1 : annoManager.getAllAnnotators()) {
		
			List<String[]> iaaValues = new ArrayList<String[]>();
			//we need a space of 4 to create the following structure
			//[verb, AnnotatorX, AnnotatorY, iaaVlaue]
		
			for(Annotator anno2: annoManager.getAllAnnotators()) {
				String[] result = new String[4];
				
				//create an new study with two anntotator's
				CodingAnnotationStudy study = new CodingAnnotationStudy(2);
				//add all categories to your study
				//an category is a sense like "777232"
				study = this.addCategoriesToStudyAdvanced(study, key);
				
				//every study needs at lest 2 categories to compare
				//1 category means 100% agreement
				if(study.getCategoryCount() > 1) {
					//add all values from the annotator's column 
					//to the study 
					for(String[] sa : sentences) {
						
						Object[] tmp = new Object[2];
						tmp[0] = (Object) sa[anno1.getColumn()];
						tmp[1] = (Object) sa[anno2.getColumn()];
						study.addItem(tmp);
					}
			
					//calculates the iaa value for anno1 with anno1,....,annotN
					double iaa = this.useIAAAlgorithm(study, iaaMethod, 2);
					
					if(overAllRows == true) {
						result[0] = "overall"; 
					}else {
						result[0] = sentences.get(0)[1];//the verb 
					}
					result[1] = anno1.getName();//name of annotatorX
					result[2] = anno2.getName();//name of annotatorY
					
					//if the iaa calculation results in not a number
					if(Double.isNaN(iaa)) {
						result[3] = Double.toString(-1);
					}else{
						result[3] = Double.toString(iaa);
					}
			
					iaaValues.add(result);
					
				}else {
					
					if(overAllRows == true) {
						result[0] = "overall"; 
					}else {
						result[0] = sentences.get(0)[1];//the verb 
					}
					result[1] = anno1.getName();
					result[2] = anno2.getName();
					result[3] = Double.toString(1.0);
					iaaValues.add(result);
				}
			}
			
			pairwiseAgreement.add(iaaValues);;
		}
		//Show.showNestedList(pairwiseAgreement);
		return pairwiseAgreement;
	}
	
	//_-----------------------------------------------------------------------------
	
	public List<List<String[]>> calculateIAAPairwise(AnnotatorManager annoManager, 
					List<String[]> sentences, IAACalculator.IAAMethods iaaMethod, boolean overAllRows) {
		//saves all pairwise agreements for every annotator
		//with the annotator's name as key for his results
		List<List<String[]>> pairwiseAgreement = new ArrayList<List<String[]>>();
		//double for loop to calculate an iaa calculate pairwise
		for(Annotator anno1 : annoManager.getAllAnnotators()) {

			List<String[]> iaaValues = new ArrayList<String[]>();
			//we need a space of 4 to create the following structure
			//[verb, AnnotatorX, AnnotatorY, iaaVlaue]

			for(Annotator anno2: annoManager.getAllAnnotators()) {
				String[] result = new String[4];
				
				//create an new study with two anntotator's
				CodingAnnotationStudy study = new CodingAnnotationStudy(2);
				//add all categories to your study
				//an category is a sense like "777232"
				study = this.addCategoriesToStudy(study);
				
				//add all values from the annotator's column 
				//to the study 
				for(String[] sa : sentences) {
					
					Object[] tmp = new Object[2];
					tmp[0] = (Object) sa[anno1.getColumn()];
					tmp[1] = (Object) sa[anno2.getColumn()];
					study.addItem(tmp);
				}
	
				//calculates the iaa value for anno1 with anno1,....,annotN
				double iaa = this.useIAAAlgorithm(study, iaaMethod, 2);
				
				if(overAllRows == true) {
					result[0] = "overall"; 
				}else {
					result[0] = sentences.get(0)[1];//the verb 
				}
				result[1] = anno1.getName();//name of annotatorX
				result[2] = anno2.getName();//name of annotatorY
				
				//if the iaa calculation results in not a number
				if(Double.isNaN(iaa)) {
					result[3] = Double.toString(-1);
				}else{
					result[3] = Double.toString(iaa);
				}

				iaaValues.add(result);
			}
			
			pairwiseAgreement.add(iaaValues);;
		}
		//Show.showNestedList(pairwiseAgreement);
		return pairwiseAgreement;
	}
	
	
	
	
	/**
	 * Adds all senses as a category to the CodyingAnnotationStudy
	 * @param study the study you want to extend with categories
	 * @return
	 */
	public CodingAnnotationStudy addCategoriesToStudy(CodingAnnotationStudy study) {
		
		HashMap<String, List<String>> sensesPV =  _extractor.getSensesPerVerb();
		
		CodingAnnotationStudy tmpStudy = study;

		//add all categories to the tmpStudy
		
		for(String key : _extractor.getAllVerbs())
		{
			for(String s : sensesPV.get(key)) {
				tmpStudy.addCategory(s);
			}
		}

		return tmpStudy;
	}

	
	/**
	 * Adds all senses as a category to the CodyingAnnotationStudy
	 * @param study the study you want to extend with categories
	 * @return
	 */
	public CodingAnnotationStudy addCategoriesToStudyAdvanced(CodingAnnotationStudy study, 
																		String verbKey) {
		
		HashMap<String, List<String>> sensesPV =  _extractor.getSensesPerVerb();
		
		CodingAnnotationStudy tmpStudy = study;

		//System.out.println(verbKey);
		//add all categories to the tmpStudy
		for(String s : sensesPV.get(verbKey)) {
			tmpStudy.addCategory(s);
		}
		
		//System.out.println(tmpStudy.getCategoryCount());

		return tmpStudy;
	}
	
	
	
	
	public IAACalculator.IAAMethods parseIAAMethod(String iaaMethod) {
		
		if(iaaMethod.equals("Percentage")) {
			return IAACalculator.IAAMethods.Percentage;
		}else if(iaaMethod.equals("RandolphKappa")) {
			return IAACalculator.IAAMethods.RandolphKappa;
		}else if(iaaMethod.equals("Bennetts")) { 
			return IAACalculator.IAAMethods.Bennetts;
		}
		return null;
	}
	
	/**
	 * List that holds the amount of senses per verb
	 * @return List of String[] of form [verb, #Sense]
	 */
	public List<String[]> getNumSensesPV(){
		return _numSensesPV;
	}
	
	/**
	 * List that holds the amount of sentences per verb
	 * @return List of Strings of form [verb, #sentences]
	 */
	public List<String[]> getNumSentencesPV(){
		return _numSentencesPV;
	}
	
	
}
