package mindbadger.footballresultsanalyser.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mindbadger.footballresultsanalyser.dao.FootballResultsAnalyserCouchbaseDAO;
import mindbadger.footballresultsanalyser.domain.Team;

@Component
public class TeamRepositoryImpl implements TeamRepository {

	@Autowired
	private FootballResultsAnalyserCouchbaseDAO dao;
	
	@Override
	public void delete(Team arg0) {
		throw new UnsupportedOperationException("delete team not yet supported");
	}

	@Override
	public Iterable<Team> findAll() {
		Map<String, Team> teams = dao.getAllTeams();
		return teams.values();
	}

	@Override
	public Team findOne(String teamId) {
		return dao.getTeam(teamId);
	}

	@Override
	public Team save(Team team) {
		Team retrievedTeam = dao.getTeam(team.getTeamId());
		if (retrievedTeam == null) {
			retrievedTeam = dao.addTeam(team.getTeamName());
		}
		
		return retrievedTeam;
	}

	@Override
	public Team findTeamByName(String name) {
		return dao.getTeamByName(name);
	}

}
