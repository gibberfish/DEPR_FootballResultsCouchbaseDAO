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

	private FootballResultsAnalyserCouchbaseDAO dao;
	
	private String divisionId1;
	private String divisionId2;
	private String teamId1;
	private String teamId2;

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
		cbUtils.tearDownTeams(new String[] {teamId1, teamId2}, TEST_BUCKET_NAME);
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
			assertEquals ("Please supply a away team when creating a fixture", e.getMessage());
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
	
	
	
	
	
	
	@Ignore
	@Test
	public void shouldBeNoTeamsForANewDivisionsAddedToASeason () {
		// When
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 3);
		
		List<SeasonDivisionTeam> seasonDivisionTeams = dao.getTeamsForDivisionInSeason(seasonDivision);
		
		// Then
		assertEquals (0, seasonDivisionTeams.size());		
	}

	@Ignore
	@Test
	public void shouldAddATeamToADivisionInASeason() {
		// When
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 3);
		Team team = dao.addTeam(TEAM_NAME_1);

		SeasonDivisionTeam seasonDivisionTeam = dao.addSeasonDivisionTeam(seasonDivision, team);
		
		List<SeasonDivisionTeam> seasonDivisionTeams = dao.getTeamsForDivisionInSeason(seasonDivision);
		
		// Then (check return value)
		assertEquals(SEASON_1, seasonDivisionTeam.getSeasonDivision().getSeason().getSeasonNumber());
		assertEquals(division.getDivisionId(), seasonDivisionTeam.getSeasonDivision().getDivision().getDivisionId());
		assertEquals(team.getTeamId(), seasonDivisionTeam.getTeam().getTeamId());
		
		// Then (get it from the list)
		assertEquals (1, seasonDivisionTeams.size());
		SeasonDivisionTeam returnedSeasonDivisionTeam = (SeasonDivisionTeam)(seasonDivisionTeams.get(0));
		
		assertEquals(SEASON_1, returnedSeasonDivisionTeam.getSeasonDivision().getSeason().getSeasonNumber());
		assertEquals(division.getDivisionId(), returnedSeasonDivisionTeam.getSeasonDivision().getDivision().getDivisionId());
		assertEquals(team.getTeamId(), returnedSeasonDivisionTeam.getTeam().getTeamId());
	}

	@Ignore
	@Test
	public void shouldAddTwoTeamsToADivisionInASeason () {
		// When
		Season season = dao.addSeason(SEASON_1);
		Division division = dao.addDivision(DIV_NAME_1);
		SeasonDivision seasonDivision = dao.addSeasonDivision(season, division, 3);
		Team team1 = dao.addTeam(TEAM_NAME_1);
		Team team2 = dao.addTeam(TEAM_NAME_2);

		SeasonDivisionTeam seasonDivisionTeam1 = dao.addSeasonDivisionTeam(seasonDivision, team1);
		SeasonDivisionTeam seasonDivisionTeam2 = dao.addSeasonDivisionTeam(seasonDivision, team2);
		
		List<SeasonDivisionTeam> seasonDivisionTeams = dao.getTeamsForDivisionInSeason(seasonDivision);
		
		// Then (check return value)
		assertEquals(SEASON_1, seasonDivisionTeam1.getSeasonDivision().getSeason().getSeasonNumber());
		assertEquals(division.getDivisionId(), seasonDivisionTeam1.getSeasonDivision().getDivision().getDivisionId());
		assertEquals(team1.getTeamId(), seasonDivisionTeam1.getTeam().getTeamId());

		assertEquals(SEASON_1, seasonDivisionTeam2.getSeasonDivision().getSeason().getSeasonNumber());
		assertEquals(division.getDivisionId(), seasonDivisionTeam2.getSeasonDivision().getDivision().getDivisionId());
		assertEquals(team2.getTeamId(), seasonDivisionTeam2.getTeam().getTeamId());

		// Then (get it from the list)
		assertEquals (2, seasonDivisionTeams.size());
		
		SeasonDivisionTeam returnedSeasonDivisionTeam1 = (SeasonDivisionTeam)(seasonDivisionTeams.get(0));
		SeasonDivisionTeam returnedSeasonDivisionTeam2 = (SeasonDivisionTeam)(seasonDivisionTeams.get(1));
		
		assertEquals(SEASON_1, returnedSeasonDivisionTeam1.getSeasonDivision().getSeason().getSeasonNumber());
		assertEquals(division.getDivisionId(), returnedSeasonDivisionTeam1.getSeasonDivision().getDivision().getDivisionId());
		assertEquals(team1.getTeamId(), returnedSeasonDivisionTeam1.getTeam().getTeamId());
		
		assertEquals(SEASON_1, returnedSeasonDivisionTeam2.getSeasonDivision().getSeason().getSeasonNumber());
		assertEquals(division.getDivisionId(), returnedSeasonDivisionTeam2.getSeasonDivision().getDivision().getDivisionId());
		assertEquals(team2.getTeamId(), returnedSeasonDivisionTeam2.getTeam().getTeamId());
	}
}
