package hu.qpa.battleroyale.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BRStatus implements Serializable{
	private String username;
	private String team;
	private boolean alive;
	private int score;
	private String warnsince;
	private double[] nearestserum;
	private String code;
	private ArrayList<double[]> borders;
	
	public BRStatus(String username, String team, boolean alive, int score,
			 String warnsince, double[] nearestserum, String code, ArrayList<double[]> borders) {
		super();
		this.username = username;
		this.team = team;
		this.alive = alive;
		this.score = score;
		this.warnsince = warnsince;
		this.nearestserum = nearestserum;
		this.code = code;
		this.borders = borders;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}


	public String getWarnsince() {
		return warnsince;
	}

	public void setWarnsince(String warnsince) {
		this.warnsince = warnsince;
	}

	public double[] getNearestserum() {
		return nearestserum;
	}

	public void setNearestserum(double[] nearestserum) {
		this.nearestserum = nearestserum;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public ArrayList<double[]> getBorders() {
		return borders;
	}

	public void setBorders(ArrayList<double[]> borders) {
		this.borders = borders;
	}
	
	
	
	
}
