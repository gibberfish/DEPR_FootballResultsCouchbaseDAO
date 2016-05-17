package uk.co.mindbadger.footballresultsanalyser.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactoryImpl;
import uk.co.mindbadger.footballresultsanalyser.domain.Team;

public class FootballResultsAnalyserCouchbaseDAOTeamTest {
	private FootballResultsAnalyserCouchbaseDAO dao;

	private static CouchbaseUtilities cbUtils = new CouchbaseUtilities ();

	private static final String TEAM_NAME_1 = "Porstmouth";
	private static final String TEAM_NAME_2 = "Arsenal";
	private static final String TEST_BUCKET_NAME = "footballTest";
	
	private DomainObjectFactory domainObjectFactory;
	private String teamId1;
	private String teamId2;
	
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
		cbUtils.tearDownTeams(new String[] {teamId1, teamId2}, TEST_BUCKET_NAME);
	}
	
	@Test
	public void shouldBeNoTeamsToStartWith () {
		// When
		Map<String, Team> teams = dao.getAllTeams();
		
		// Then
		assertEquals (0, teams.size());		
	}

	@Test
	public void shouldAddATeam() {
		// When
		Team team = dao.addTeam(TEAM_NAME_1);
		Map<String, Team> teams = dao.getAllTeams();
		
		// Then (check return value)
		assertEquals (TEAM_NAME_1, team.getTeamName());
		teamId1 = team.getTeamId();
		
		// Then (get it from the list)
		assertEquals (1, teams.size());
		assertEquals (TEAM_NAME_1, teams.get(teamId1).getTeamName());
		assertEquals (teamId1, teams.get(teamId1).getTeamId());
		
		// Then (get it by key)
		assertEquals (TEAM_NAME_1, dao.getTeam(teamId1).getTeamName());
	}

	@Test
	public void shouldAddTwoTeams () {
		// When
		Team team1 = dao.addTeam(TEAM_NAME_1);
		Team team2 = dao.addTeam(TEAM_NAME_2);
		Map<String, Team> teams = dao.getAllTeams();
		
		// Then (check return value)
		assertEquals (TEAM_NAME_1, team1.getTeamName());
		teamId1 = team1.getTeamId();
		assertEquals (TEAM_NAME_2, team2.getTeamName());
		teamId2 = team2.getTeamId();
		
		// Then (get it from the list)
		assertEquals (2, teams.size());
		assertEquals (TEAM_NAME_1, teams.get(teamId1).getTeamName());
		assertEquals (teamId1, teams.get(teamId1).getTeamId());
		assertEquals (TEAM_NAME_2, teams.get(teamId2).getTeamName());
		assertEquals (teamId2, teams.get(teamId2).getTeamId());
		
		// Then (get it by key)
		assertEquals (TEAM_NAME_1, dao.getTeam(teamId1).getTeamName());
		assertEquals (TEAM_NAME_2, dao.getTeam(teamId2).getTeamName());
	}

	@Test
	public void shouldUpdateATeam() {
		// When
		dao.addTeam(TEAM_NAME_1);
		Team team = dao.addTeam(TEAM_NAME_1);
		Map<String, Team> teams = dao.getAllTeams();
		
		// Then (check return value)
		assertEquals (TEAM_NAME_1, team.getTeamName());
		teamId1 = team.getTeamId();
		
		// Then (get it from the list)
		assertEquals (1, teams.size());
		assertEquals (TEAM_NAME_1, teams.get(teamId1).getTeamName());
		assertEquals (teamId1, teams.get(teamId1).getTeamId());
		
		// Then (get it by key)
		assertEquals (TEAM_NAME_1, dao.getTeam(teamId1).getTeamName());
	}

	@Test
	public void shouldThrowAnExceptionWhenTryingToGetANonExistentSeason () {
		// Given

		try {
			// When
			dao.getTeam(teamId1);
			fail ("An exception should be thrown if a division does not exist");
		} catch (IllegalArgumentException e) {
			// Then
			assertEquals ("Team "+teamId1+" does not exist", e.getMessage());
		}
	}	
}
