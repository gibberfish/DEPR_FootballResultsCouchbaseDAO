package uk.co.mindbadger.footballresultsanalyser.domain;

public class SeasonImpl implements Season {
	private static final long serialVersionUID = 1965225871957347148L;
	
	private Integer seasonNumber;
	
	@Override
	public Integer getSeasonNumber() {
		return seasonNumber;
	}

	@Override
	public void setSeasonNumber(Integer seasonNumber) {
		this.seasonNumber = seasonNumber;
	}
}
