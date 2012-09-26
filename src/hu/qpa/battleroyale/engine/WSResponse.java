package hu.qpa.battleroyale.engine;

import java.util.Date;
import java.util.List;

public class WSResponse {
	String username;
	String team;
	boolean alive;
	float[] pos;
	String items; // TODO ez mi?
	int score;
	String token;
	List<String[]> events;
	Date warnsince;
	float[] nearestserum;
	String code;
}
