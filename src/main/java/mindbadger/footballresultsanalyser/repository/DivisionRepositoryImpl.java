package mindbadger.footballresultsanalyser.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mindbadger.footballresultsanalyser.dao.FootballResultsAnalyserCouchbaseDAO;
import mindbadger.footballresultsanalyser.domain.Division;
import mindbadger.footballresultsanalyser.domain.Team;

@Component
public class DivisionRepositoryImpl implements DivisionRepository {

	@Autowired
	private FootballResultsAnalyserCouchbaseDAO dao;
	
	@Override
	public void delete(Division arg0) {
		throw new UnsupportedOperationException("delete division not yet supported");
	}

	@Override
	public Iterable<Division> findAll() {
		Map<String,Division> divisions = dao.getAllDivisions();
		return divisions.values();
	}

	@Override
	public Division findOne(String divisionId) {
		return dao.getDivision(divisionId);
	}

	@Override
	public Division save(Division division) {
		Division retrievedDivision = dao.getDivision(division.getDivisionId());
		if (retrievedDivision == null) {
			retrievedDivision = dao.addDivision(division.getDivisionName());
		}
		
		return retrievedDivision;
	}

	@Override
	public Division findDivisionByName(String name) {
		return dao.getDivisionByName(name);
	}

}
