package uk.co.mindbadger.footballresultsanalyser.dao;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Fixture;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivision;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionTeam;
import uk.co.mindbadger.footballresultsanalyser.domain.Team;

public class FootballResultsAnalyserCouchbaseDAOFixtureTest {

	private static CouchbaseUtilities cbUtils = new CouchbaseUtilities ();

	private static final Integer SEASON_1 = 2001;
	private static final Integer SEASON_2 = 2002;
	private static final String DIV_NAME_1 = "Premier";
	private static final String TEAM_NAME_1 = "Porstmouth";
	private static final String TEAM_NAME_2 = "Arsenal";
	private static final String TEAM_NAME_3 = "Walsall";
	private static final String TEAM_NAME_4 = "Bristol City";

	private FootballResultsAnalyserCouchbaseDAO dao;
	
	private String divisionId1;
	private String divisionId2;
	private String teamId1;
	private String teamId2;
	private String teamId3;
	private String teamId4;

	private DomainObjectFactory domainObjectFactory;
	
	private static final String TEST_BUCKET_NAME = "footballTest";
	
	@BeforeClass
	public static void ensureWeAreCleanBeforeWeStart () {
		cbUtils.flushBucket(TEST_BUCKET_NAME);
	}
	
	@Before
	public void setup () {
		dao = new FootballResultsAnalyserCouchbaseDAO ();
		domainObjectFactory = new DomainObjectFactoryImpl();
		
		dao.setDomainObjectFactory(domainObjectFactory);
		dao.setBucketName("footballTest");
		
		dao.startSession();
	}

	@After
	public void teardown () {
		dao.closeSession();
		cbUtils.tearDownSeasons(new Integer[] {SEASON_1, SEASON_2}, TEST_BUCKET_NAME);
		cbUtils.tearDownDivisions(new String[] {divisionId1, divisionId2}, TEST_BUCKET_NAME);
		cbUtils.tearDownTeams(new String[] {teamId1, teamId2, teamId3, teamId4}, TEST_BUCKET_NAME);
	}
	
	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutASeason () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(null, fixtureDate, division, homeTeam, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a season");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a season when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutADivision () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, fixtureDate, null, homeTeam, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a division");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a division when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutAHomeTeam () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, fixtureDate, division, null, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a home team");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a home team when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAFixtureWithoutAnAwayTeam () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, fixtureDate, division, homeTeam, null, 1, 2);
			fail ("An exception should be thrown when we don't supply a away team");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply an away team when creating a fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldThrowAnExceptionWhenTryingToAddAPlayedFixtureWithoutAFixtureDate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		try {
			// When
			dao.addFixture(season, null, division, homeTeam, awayTeam, 1, 2);
			fail ("An exception should be thrown when we don't supply a date for a played fixture");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Please supply a fixture date team when creating a played fixture", e.getMessage());
		}
	}	

	@Test
	public void shouldAddAnUnplayedFixtureWithoutADate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		Fixture fixture = dao.addFixture(season, null, division, homeTeam, awayTeam, null, null);
		Fixture retrievedFixture = dao.getFixture(fixture.getFixtureId());
		
		// Then
		//TODO Add the asserts here
	}
	
	@Test
	public void shouldAddAnUnplayedFixtureWithADate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, null, null);
	}
	
	@Test
	public void shouldAddAnPlayedFixture () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team homeTeam = dao.addTeam(TEAM_NAME_1);
		Team awayTeam = dao.addTeam(TEAM_NAME_2);
		Calendar fixtureDate = Calendar.getInstance();
		
		// When
		dao.addFixture(season, fixtureDate, division, homeTeam, awayTeam, 2, 1);
	}
	
	@Test
	public void shouldGetFixturesWithNoFixtureDate () {
		// Given
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		Team team1 = dao.addTeam(TEAM_NAME_1);
		teamId1 = team1.getTeamId();
		Team team2 = dao.addTeam(TEAM_NAME_2);
		teamId2 = team1.getTeamId();
		Team team3 = dao.addTeam(TEAM_NAME_3);
		teamId3 = team1.getTeamId();
		Team team4 = dao.addTeam(TEAM_NAME_4);
		teamId4 = team1.getTeamId();
		Calendar fixtureDate = Calendar.getInstance();
		Fixture fixture1 = dao.addFixture(season, fixtureDate, division, team1, team2, 2, 1);
		Fixture fixture2 = dao.addFixture(season, fixtureDate, division, team3, team4, 1, 3);
		Fixture fixture3 = dao.addFixture(season, null, division, team2, team1, null, null);
		Fixture fixture4 = dao.addFixture(season, null, division, team4, team3, null, null);
		
		// When
		List<Fixture> fixtures = dao.getFixturesWithNoFixtureDate();
		
		// Then
		assertEquals (2, fixtures.size());
		assertEquals (fixture3.getFixtureId(), fixtures.get(0).getFixtureId());
		assertEquals (fixture4.getFixtureId(), fixtures.get(1).getFixtureId());
	}
}
