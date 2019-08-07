package iaaGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import Annotators.Annotator;
import Display.Show;

public class IAAExtractor {

	private String _filePath;
	private TsvParser _parser;
	
	String[] _firstRow;
	
	private List<String[]> _parsedTSV;
	private List<String> _allVerbs = new ArrayList<String>();
	private List<String> _allSenses = new ArrayList<String>();
	private HashMap<String, List<String[]>> _packedVerbs;
	private HashMap<String, List<String>> _sensesPerVerb = new HashMap<String, List<String>>();
	
	//keys are the senses, the List contains the sentences with the given sense
	private HashMap<String, List<String[]>> _packedSenses;

	private HashMap<String, String[]> _sentencesMap = new HashMap<String, String[]>();
	
	//holds ids e.g tueba-s10855-1, tueba-s11185-2
	private List<String> _sentencesKeys = new ArrayList<String>();
	
	/**
	 * Constructor for extracting raw annotation data from a tsv file.
	 * line separators(settings):
	 * 0 => \n
	 * 1 => \r
	 * 2 => \t
	 * 3 => \\
	 * @param filePath path to the tsv file
	 * @param setting that separate lines (e.g 0 for \t)
	 */
	public IAAExtractor(String filePath, int setting) 
	{
		
		_parser = new TsvParser(this.setParserSetting(setting));
		_filePath = filePath; //used for extracting and parsing
		_parsedTSV = parseFileExtra();//use extra because last column is null
		
		sentenceMapping();
		
		extractAllVerbs();
		
		packVerbs();
		
		extractSensesPerVerb();
		
		packSenses();
		
		/*
		for(String key: sentecesKeys) {
			String[] tmp = sentencesMap.get(key);
			for(String s : tmp) {
				System.out.print(s + " ");
			}
			System.out.println(" ");
		}
		*/
	}
	
	/**
	 * parses the raw tsv file and changes every star "*" 
	 * to a null
	 * @param filePath
	 * @return
	 */
	public List<String[]> parseFile() {
		
		//allRows is the parsedRaw data
		File file = new File(_filePath);
		List<String[]> allRows = _parser.parseAll(file);
			
		for(int i = 0; i < allRows.size(); i++) {

			//check all words inside the array
			for(int j = 0; j < allRows.get(i).length; j++) {
				String star= "*";
				
				if(star.equals(allRows.get(i)[j])) {
					allRows.get(i)[j] = null;
				}
			}
		}
		return allRows;
	}
	
	
	/**
	 * parses the raw tsv file and changes every star "*"
	 * Extra:Removes the last null column inside the tsv
	 * to a null
	 * @param filePath
	 * @return
	 */
	public List<String[]> parseFileExtra() {
		
		//allRows is the parsedRaw data
		File file = new File(_filePath);
		List<String[]> allRows = _parser.parseAll(file);
		
		List<String[]> allRowsClean = new ArrayList<String[]>();
		
		_firstRow = allRows.get(0);
		//don't use first head 
		for(int i = 1; i < allRows.size(); i++) {
			
			String[] tmp = new String[allRows.get(i).length-1];
			
			for(int j = 0; j < allRows.get(i).length-1; j++) {
				tmp[j] = allRows.get(i)[j];
				//System.out.print(tmp[j] + " ");
			}
			//System.out.println(" ");
			allRowsClean.add(tmp);
			
		}
		
		for(int i = 0; i < allRowsClean.size(); i++) {

			//check all words inside the array
			for(int j = 0; j < allRowsClean.get(i).length; j++) {
				String star= "*";
				
				if(star.equals(allRowsClean.get(i)[j])) {
					allRowsClean.get(i)[j] = null;
				}
			}
		}
		
		return allRowsClean;
	}
	
	/**
	 * creates annotator objects with their name
	 * and column position
	 * @return
	 */
	public List<Annotator> extractAnnotators() {
		String[] firstRow = _firstRow;
		
		List<Annotator> annotators = new ArrayList<Annotator>();
		List<String> usedAnnotators = new ArrayList<String>();
		
		int counter = 0;
		for(String s : firstRow) {
			
			if(!(s == null)){
				
				if(s.contains("Annotator")){
					if(!usedAnnotators.contains(s)) {
						usedAnnotators.add(s);
						Annotator anno = new Annotator(s, counter);
						annotators.add(anno);
					}
				}
			}
			counter++;
		}
		return annotators;
	}
	
	
	/**
	 * maps all sentences to a specific sentencesID.
	 * The keys are saved in a separate list.
	 */
	public void sentenceMapping() {
		
		for(String[] sa : _parsedTSV) {
			String[] rowNoSID = new String[sa.length-1];
			
			for(int i = 0 ; i < sa.length-1; i++) {
				rowNoSID[i] = sa[i];
			}
			
			if(!_sentencesMap.containsKey(sa[0])) {
				_sentencesKeys.add(sa[0]);
				_sentencesMap.put(sa[0], rowNoSID);
			}
		}
	}
	
	/**
	 * this method extracts all used verbs
	 */
	public void extractAllVerbs() {
		List<String> usedVerbs = new ArrayList<String>();
		
		for(String[] sa : _parsedTSV) {
			if(!sa.equals(_firstRow)) {
				if(!usedVerbs.contains(sa[1])) {
					usedVerbs.add(sa[1]);
				}	
			}
		}	
		_allVerbs = usedVerbs;
	}
	
	
	/**
	 * we create packages with sentences with the same verb.
	 * The Verb itself is used as a Key to lookup the list which contains
	 * all sentences with the verb.
	 */
	public void packVerbs() {
		_packedVerbs = new HashMap<String, List<String[]>>();
		
		//for every verb
		for(String verb : _allVerbs) {
			//System.out.println("USED VERB: " + verb);
			//create a list that will contain all sentences
			List<String[]> verbList = new ArrayList<String[]>();
			
			//if the verb is the annotation verb, add it to our list
			for(String[] sa : _parsedTSV) {
				
				if(sa[1].equals(verb)){
		
					verbList.add(sa);
				}
			}
			_packedVerbs.put(verb, verbList);
		}
	}
	
	/**
	 * packs all sentences that contain a specific sense to a list
	 */
	public void packSenses() {
		//keys are the senses, the List contains the sentences with the given sense
		_packedSenses = new HashMap<String, List<String[]>>();
		
		//for every verb
		for(String verbKey : _allVerbs) {
			
			
			//get the list with sentences that contain senses for that particular verb
			for(String senseKey : _sensesPerVerb.get(verbKey)) {
				List<String[]> tmpSentences = new ArrayList<String[]>();
				
				//all sentences that could contain the senseKey
				for(String[] sa : _packedVerbs.get(verbKey)) {
					
					//check if sentence contains the key
					for(String s : sa) {
		
						if(Objects.equals(s, senseKey)) {

							tmpSentences.add(sa);
							break; 
						}
					}

				}
				
				_packedSenses.put(senseKey, tmpSentences);
			}
			
		}
		
		//Show.showList(_packedSenses.get("76015"));
	}
	
	
	
	
	/**
	 * This method extracts all senses per verb
	 */
	public void extractSensesPerVerb() {
		_sensesPerVerb = new HashMap<String, List<String>>();
		
		
		for(String verb : _allVerbs) {
			
			List<String[]> verbList = _packedVerbs.get(verb);
			//we will save every sense id, so we don't count twice
			List<String> usedSenses = new ArrayList<String>();
			
			for(String[] sa : verbList) {
				//i = 3 to skip the satzID, verb, satz
				for(int i = 3; i < sa.length ; i++) {

					if(sa[i] != null && !usedSenses.contains(sa[i]) && !_allSenses.contains(sa[i])) {
						//System.out.print(sa[i] + " ");
							usedSenses.add(sa[i]);
							_allSenses.add(sa[i]);//add senses
						
					}
				}

			}
			//add all senses in a list, with a key, which is the verb itself
			_sensesPerVerb.put(verb, usedSenses);
		}
		
		/*debug print
		for(String ts : _allVerbs) {
			List<String> tmpo = sensesPerVerb.get(ts);
			System.out.println("USED VERB: " + ts);
			for(String s :  tmpo) {
				
				System.out.print(s + " ");
			}
			System.out.println(" ");
		}
		*/
	}
	
	/**
	 * Parse an tsv file with a specific line separator
	 * line sepperators(Settings):
	 * 0 => \n
	 * 1 => \r
	 * 2 => \t
	 * 3 => \\
	 * @param setting an integer number representing your line seperator
	 */
	public TsvParserSettings setParserSetting(int setting) {
		TsvParserSettings settings = new TsvParserSettings();
		
		switch(setting) {
		
		case 0:
			settings.getFormat().setLineSeparator("\n");
			break;
		case 1:
			settings.getFormat().setLineSeparator("\r");
			break;
		case 2:
			settings.getFormat().setLineSeparator("\t");
			break;
		default:
			settings.getFormat().setLineSeparator(" \\ ");
			break;
		}
		return settings;
	}
	
	public List<String[]> getParsedTSV(){
		return _parsedTSV;
	}
	
	public List<String> getAllVerbs(){
		return _allVerbs;
	}
	
	public HashMap<String, List<String[]>> getPackedVerbs() {
		return _packedVerbs;
	}
	
	public HashMap<String, List<String>> getSensesPerVerb() {
		return _sensesPerVerb;
	}
	
	public HashMap<String, List<String[]>> getPackedSenses(){
		return _packedSenses;
	}
	
	public List<String> getAllSenses(){
		return _allSenses;
	}
	
}
