package mindbadger.footballresultsanalyser.domain;

import java.util.Calendar;

public class FixtureImpl implements Fixture {
	private static final long serialVersionUID = -7544751416115166446L;

	private String fixtureId;
	private Calendar fixtureDate;
	private Season season;
	private Division division;
	private Team homeTeam;
	private Team awayTeam;
	private Integer homeGoals;
	private Integer awayGoals;
	
	@Override
	public Integer getAwayGoals() {
		return awayGoals;
	}

	@Override
	public Team getAwayTeam() {
		return awayTeam;
	}

	@Override
	public Division getDivision() {
		return division;
	}

	@Override
	public Calendar getFixtureDate() {
		return fixtureDate;
	}

	@Override
	public String getFixtureId() {
		return fixtureId;
	}

	@Override
	public Integer getHomeGoals() {
		return homeGoals;
	}

	@Override
	public Team getHomeTeam() {
		return homeTeam;
	}

	@Override
	public Season getSeason() {
		return season;
	}

	@Override
	public void setAwayGoals(Integer awayGoals) {
		this.awayGoals = awayGoals;
	}

	@Override
	public void setAwayTeam(Team awayTeam) {
		this.awayTeam = awayTeam;
	}

	@Override
	public void setDivision(Division division) {
		this.division = division;
	}

	@Override
	public void setFixtureDate(Calendar fixtureDate) {
		this.fixtureDate = fixtureDate;
	}

	@Override
	public void setFixtureId(String fixtureId) {
		this.fixtureId = fixtureId;
	}

	@Override
	public void setHomeGoals(Integer homeGoals) {
		this.homeGoals = homeGoals;
	}

	@Override
	public void setHomeTeam(Team homeTeam) {
		this.homeTeam = homeTeam;
	}

	@Override
	public void setSeason(Season season) {
		this.season = season;
	}
}
