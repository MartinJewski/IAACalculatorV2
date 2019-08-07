package Factory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Annotators.AnnotatorManager;
import iaaGenerator.IAACalculator;
import iaaGenerator.IAAExtractor;

public class IAAFactory {

	//that double value represents an iaa between all annotator's 
	//over the whole list
	List<String[]> _overall;
	List<String[]> _overallPerVerb;
	List<String[]> _overallSensesPerVerb;
	HashMap<String, List<List<String[]>>> _pairwiseVerbs = new HashMap<String, List<List<String[]>>>();
	
	HashMap<String, List<List<String[]>>> _pairwiseSenses = new HashMap<String, List<List<String[]>>>();
	
	List<List<String[]>> _pairwiseOverAllRows;

	
	public IAAFactory(IAACalculator iaaCalc,IAAExtractor iaaEx, AnnotatorManager annoManager,
			IAACalculator.IAAMethods iaaMethod) {

		this.calculateOverallIAAs(iaaCalc, iaaEx, annoManager, iaaMethod);
		this.calculatePairwiseIAAs(iaaCalc, iaaEx, annoManager, iaaMethod);
		
	}
	
	public void calculateOverallIAAs(IAACalculator iaaCalc,IAAExtractor iaaEx, AnnotatorManager annoManager,
			IAACalculator.IAAMethods iaaMethod) {
		iaaEx.getPackedVerbs();
		//that double value represents an iaa between all annotator's 
		//over the whole list
		_overall = new ArrayList<String[]>();
		String[] hold = new String[2];
		
		double d = iaaCalc.calculateIAAOverall(annoManager, iaaEx.getParsedTSV(), iaaMethod);
		hold[0] = "iaa between all over all rows";
		hold[1] = Double.toString(d);
		_overall.add(hold);
		
		//will hold arrays with size two of form
		//the iaa value is an iaa between all annotator's for the verb
		//[verb, iaaValue]
		_overallPerVerb = new ArrayList<String[]>();
		for(String key : iaaEx.getAllVerbs()) {

			String[] tmp = new String[2];
			tmp[0] = key;
			//we use calculateIAAOverallCF to add only categories for the specific verb
			tmp[1] = Double.toString(
					iaaCalc.calculateIAAOverallCF(annoManager, iaaEx.getPackedVerbs().get(key), iaaMethod, key));
			_overallPerVerb.add(tmp);
		}
		
		_overallSensesPerVerb = new ArrayList<String[]>();
		for(String key: iaaEx.getAllSenses()) {
			String[] tmp = new String[2];
			tmp[0] = key;
			tmp[1] = Double.toString(iaaCalc.calculateIAAOverallCF(annoManager, 
					iaaEx.getPackedSenses().get(key), iaaMethod, 
					iaaEx.getPackedSenses().get(key).get(0)[1]));//verb associated with the sense
			_overallSensesPerVerb.add(tmp);
		}
		
	}
	
	public void calculatePairwiseIAAs(IAACalculator iaaCalc,IAAExtractor iaaEx, AnnotatorManager annoManager,
			IAACalculator.IAAMethods iaaMethod) {
		
		_pairwiseVerbs = new HashMap<String, List<List<String[]>>>();
		
		_pairwiseSenses = new HashMap<String, List<List<String[]>>>();
		
		//calculate pairwise the iaa value for over all rows
		_pairwiseOverAllRows = iaaCalc.calculateIAAPairwise(annoManager, 
														iaaEx.getParsedTSV(), iaaMethod, true);
		
		//calculate pairwise for all verbs
		for(String key : iaaEx.getAllVerbs()) {
			
			_pairwiseVerbs.put(key, 
					iaaCalc.calculateIAAPairwiseCF(annoManager,
														iaaEx.getPackedVerbs().get(key), iaaMethod, false, key));
	
		}
		//calculate pairwise for all senses, categories are the senses for the specific verb
		for(String key: iaaEx.getAllSenses()) {
			//System.out.println(key);
			_pairwiseSenses.put(key, iaaCalc.calculateIAAPairwiseCF(annoManager, iaaEx.getPackedSenses().get(key),
					iaaMethod, false, iaaEx.getPackedSenses().get(key).get(0)[1]));
		}
	}
	

	/**
	 * calculates an iaa value over all rows for all annotator's
	 * @return
	 */
	public List<String[]> getOverallIaa() {
		return _overall;
	}
	
	/**
	 * calculates an overall iaa value per verb for all annotator's
	 * @return
	 */
	public List<String[]> getOverallIaaPerVerb(){
		return _overallPerVerb;
	}
	
	
	/**
	 * calculates an overall iaa value per verb for all annotator's
	 * @return
	 */
	public List<String[]> getOverallIaaSensesPerVerb(){
		return this._overallSensesPerVerb;
	}
	
	/**
	 * pairwise iaa calculation for all annotator's over all verbs
	 * Use a verb name as key
	 * @return
	 */
	public HashMap<String, List<List<String[]>>> getPairwiseVerbIaa(){
		return _pairwiseVerbs;
	}
	
	
	/**
	 * pairwise iaa calculation for all annotator's over all senses
	 * Use a sense number string as key
	 * @return
	 */
	public HashMap<String, List<List<String[]>>> getPairwiseSensesIaa(){
		return this._pairwiseSenses;
	}
	
	/**
	 * Pairwise iaa calculation for all annotator's 
	 * @return
	 */
	public List<List<String[]>> getpairwiseOverAllRows(){
		return _pairwiseOverAllRows;
	}
	
}
