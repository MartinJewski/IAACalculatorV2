package Annotators;

import java.util.ArrayList;
import java.util.List;

public class Annotator {

	//all sentences the Annotator annotated something
	//private List<String> _SatzIDs = new ArrayList<String>();
	private String _name;
	private int _columnPosition;
	
	public Annotator(String name, int colPos) {
		_name = name;
		_columnPosition = colPos;
	}
	/*
	public void addSatzID(String sID) {
		this._SatzIDs.add(sID);
	}
	*/
	
	public String getName() {
		return _name;
	}
	
	public int getColumn() {
		return _columnPosition;
	}
	
}
