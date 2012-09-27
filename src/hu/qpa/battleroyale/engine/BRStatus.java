package hu.qpa.battleroyale.engine;

import java.io.Serializable;
import java.util.Date;

public class BRStatus implements Serializable{
	private String username;
	private String team;
	private boolean alive;
	private int score;
	private long warnsince;
	private double[] nearestserum;
	private String code;
	
	public BRStatus(String username, String team, boolean alive, int score,
			 long warnsince, double[] nearestserum, String code) {
		super();
		this.username = username;
		this.team = team;
		this.alive = alive;
		this.score = score;
		this.warnsince = warnsince;
		this.nearestserum = nearestserum;
		this.code = code;
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


	public long getWarnsince() {
		return warnsince;
	}

	public void setWarnsince(long warnsince) {
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
	
	
	
	
}
