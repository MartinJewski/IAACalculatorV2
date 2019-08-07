package Annotators;

import java.util.ArrayList;
import java.util.List;

public class AnnotatorManager {

	private List<Annotator> _annotators = new ArrayList<Annotator>();
	
	public AnnotatorManager() {
		
	}
	
	public void addAnnotator(Annotator a) {
		_annotators.add(a);
	}
	
	public void addAnnotators(List<Annotator> annotators) {
		for(Annotator a: annotators) {
			addAnnotator(a);
		}
	}
	
	
	public Annotator getAnnotator(String name) {
		Annotator tmp = null;
		for(Annotator anno : _annotators) {
			if(anno.getName().equals(name)){
				tmp = anno;
			}
		}	
		return tmp;
	}
	
	public List<Annotator> getAllAnnotators(){
		return _annotators;
	}
	
	public List<String> getAnnotatorsNames() {
		
		List<String> tmp = new ArrayList<String>();
		
		for(Annotator anno : _annotators) {
			tmp.add(anno.getName());
		}
		return tmp;
	}
}
