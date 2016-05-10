package uk.co.mindbadger.footballresultsanalyser.domain;

public class SeasonDivisionImpl implements SeasonDivision {

	private static final long serialVersionUID = 3890259602088787437L;

	private Season season;
	private Division division;
	private int divisionPosition;
	
	@Override
	public Division getDivision() {
		return division;
	}

	@Override
	public int getDivisionPosition() {
		return divisionPosition;
	}

	@Override
	public Season getSeason() {
		return season;
	}

	@Override
	public void setDivision(Division division) {
		this.division = division;
	}

	@Override
	public void setDivisionPosition(int divisionPosition) {
		this.divisionPosition = divisionPosition;
	}

	@Override
	public void setSeason(Season season) {
		this.season = season;
	}

	@Override
	public int compareTo(SeasonDivision compareTo) {
		if (compareTo.getSeason().getSeasonNumber() != this.getSeason().getSeasonNumber()) {
			return this.getSeason().getSeasonNumber() - compareTo.getSeason().getSeasonNumber();
		} else if (compareTo.getDivisionPosition() != this.getDivisionPosition()) {
			return this.getDivisionPosition() - compareTo.getDivisionPosition();
		} else {
			return this.getDivision().getDivisionName().compareTo(compareTo.getDivision().getDivisionName());
		}
	}
}
