package hu.qpa.battleroyale.engine.types;

import java.io.Serializable;
import java.util.List;

public class Spell implements Serializable{
	int ID;
	List<Point> Parameter;
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public List<Point> getParameter() {
		return Parameter;
	}
	public void setParameter(List<Point> parameter) {
		Parameter = parameter;
	}
	
	
}
