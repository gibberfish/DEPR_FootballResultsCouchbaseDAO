package mindbadger.footballresultsanalyser.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mindbadger.footballresultsanalyser.dao.FootballResultsAnalyserCouchbaseDAO;
import mindbadger.footballresultsanalyser.domain.Fixture;
import mindbadger.footballresultsanalyser.domain.Season;
import mindbadger.footballresultsanalyser.domain.SeasonDivision;
import mindbadger.footballresultsanalyser.domain.Team;

@Component
public class FixtureRepositoryImpl implements FixtureRepository {

	@Autowired
	private FootballResultsAnalyserCouchbaseDAO dao;
	
	@Override
	public void delete(Fixture arg0) {
		throw new UnsupportedOperationException("delete fixture not yet supported");
	}

	@Override
	public Iterable<Fixture> findAll() {
		return dao.getFixtures();
	}

	@Override
	public Fixture findOne(String fixtureId) {
		return dao.getFixture(fixtureId);
	}

	@Override
	public Fixture save(Fixture fixture) {
		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());
		if (retrievedFixture == null) {
			retrievedFixture = dao.addFixture(fixture.getSeason(), fixture.getFixtureDate(), fixture.getDivision(), fixture.getHomeTeam(), fixture.getAwayTeam(), fixture.getHomeGoals(), fixture.getAwayGoals());
		}
		
		return retrievedFixture;
	}

	@Override
	public Fixture getExistingFixture(Season season, Team homeReam, Team awayTeam) {
		throw new UnsupportedOperationException("getExistingFixture not yet supported");
	}

	@Override
	public List<Fixture> getFixturesForDivisionInSeason(SeasonDivision arg0) {
		throw new UnsupportedOperationException("getFixturesForDivisionInSeason not yet supported");
	}

	@Override
	public List<Fixture> getFixturesForTeamInDivisionInSeason(SeasonDivision arg0, Team arg1) {
		throw new UnsupportedOperationException("getFixturesForTeamInDivisionInSeason not yet supported");
	}

	@Override
	public List<Fixture> getFixturesWithNoFixtureDate() {
		throw new UnsupportedOperationException("getFixturesWithNoFixtureDate not yet supported");
	}

	@Override
	public List<Fixture> getUnplayedFixturesBeforeToday() {
		throw new UnsupportedOperationException("getUnplayedFixturesBeforeToday not yet supported");
	}

}
