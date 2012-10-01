package hu.qpa.battleroyale.engine;

import java.util.ArrayList;
import java.util.List;

public class WSResponse {
	String username;
	String team;
	int alive;
	double[] pos;
	String items; // TODO ez mi?
	int score;
	String token;
	List<String[]> events;
	List<String[]> warnings;
	boolean nokill;
	ArrayList<double[]> borders;
	String lastupdate;
	double[] nearestserum;
	String code;
	double cspeed; 
}
