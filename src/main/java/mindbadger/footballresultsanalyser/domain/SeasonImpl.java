package mindbadger.footballresultsanalyser.domain;

import java.util.HashSet;
import java.util.Set;

public class SeasonImpl implements Season {
	private static final long serialVersionUID = 1965225871957347148L;
	
	private Set<SeasonDivision> seasonDivisions;
	private Integer seasonNumber;
	
	public SeasonImpl() {
		this.seasonDivisions = new HashSet<SeasonDivision> ();
	}
	
	@Override
	public Integer getSeasonNumber() {
		return seasonNumber;
	}

	@Override
	public void setSeasonNumber(Integer seasonNumber) {
		this.seasonNumber = seasonNumber;
	}

	@Override
	public Set<SeasonDivision> getSeasonDivisions() {
		return seasonDivisions;
	}

	@Override
	public void setSeasonDivisions(Set<SeasonDivision> seasonDivisions) {
		this.seasonDivisions = seasonDivisions;
	}
}
