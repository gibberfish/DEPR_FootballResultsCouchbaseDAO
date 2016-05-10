package uk.co.mindbadger.footballresultsanalyser.domain;

public class SeasonDivisionTeamImpl implements SeasonDivisionTeam {
	private static final long serialVersionUID = 2167813774158616986L;
	
	private SeasonDivision seasonDivision;
	private Team team;
	
	@Override
	public SeasonDivision getSeasonDivision() {
		return seasonDivision;
	}

	@Override
	public Team getTeam() {
		return team;
	}

	@Override
	public void setSeasonDivision(SeasonDivision seasonDivision) {
		this.seasonDivision = seasonDivision;
	}

	@Override
	public void setTeam(Team team) {
		this.team = team;
	}
}
