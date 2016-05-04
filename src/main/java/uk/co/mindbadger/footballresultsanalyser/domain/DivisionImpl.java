package uk.co.mindbadger.footballresultsanalyser.domain;

public class DivisionImpl implements Division {

	private static final long serialVersionUID = -8316700603709236655L;
	
	private String divisionId;
	private String divisionName;
	
	@Override
	public String getDivisionId() {
		return divisionId;
	}

	@Override
	public String getDivisionName() {
		return divisionName;
	}

	@Override
	public void setDivisionId(String divisionId) {
		this.divisionId = divisionId;
	}

	@Override
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}
}
