package uk.co.mindbadger.footballresultsanalyser.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import uk.co.mindbadger.footballresultsanalyser.domain.Division;
import uk.co.mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import uk.co.mindbadger.footballresultsanalyser.domain.Fixture;
import uk.co.mindbadger.footballresultsanalyser.domain.Season;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivision;
import uk.co.mindbadger.footballresultsanalyser.domain.SeasonDivisionTeam;
import uk.co.mindbadger.footballresultsanalyser.domain.Team;

public class FootballResultsAnalyserCouchbaseDAO implements FootballResultsAnalyserDAO {
	Logger logger = Logger.getLogger(FootballResultsAnalyserCouchbaseDAO.class);

	private String bucketName = "footballTest";
	
	private DomainObjectFactory domainObjectFactory;
	private Cluster cluster;
	private Bucket bucket;
	
	@Override
	public Season addSeason(Integer seasonNumber) {
		
		JsonArray divisions = JsonArray.empty();
		
		JsonObject season = JsonObject.empty()
				.put("type", "season")
				.put("seasonNumber", seasonNumber)
				.put("divisions", divisions);
		
		JsonDocument doc = JsonDocument.create("ssn_" + seasonNumber.toString(), season);
		JsonDocument response = bucket.upsert(doc);

		return domainObjectFactory.createSeason(seasonNumber);
	}
	
	@Override
	public Division addDivision(String divisionName) {
		JsonLongDocument newIdLongDoc = bucket.counter("divisionId", +1);
		Long newId = newIdLongDoc.content();

		JsonObject division = JsonObject.empty()
				.put("type", "division")
				.put("divisionId", newId)
				.put("divisionName", divisionName);
		
		String generateIdString = "div_" + newId;
		JsonDocument doc = JsonDocument.create(generateIdString, division);
		JsonDocument response = bucket.upsert(doc);
		
		Division divisionObject = domainObjectFactory.createDivision(divisionName);
		divisionObject.setDivisionId(generateIdString);
		return divisionObject;
	}
	
	@Override
	public Team addTeam(String teamName) {
		JsonLongDocument newIdLongDoc = bucket.counter("teamId", +1);
		Long newId = newIdLongDoc.content();
		
		JsonObject team = JsonObject.empty()
				.put("type", "team")
				.put("teamId", newId)
				.put("teamName", teamName);
		
		String generatedIdString = "team_" + newId;
		JsonDocument doc = JsonDocument.create(generatedIdString, team);
		JsonDocument response = bucket.upsert(doc);

		Team teamObject = domainObjectFactory.createTeam(teamName);
		teamObject.setTeamId(generatedIdString);
		return teamObject;
	}

	@Override
	public Fixture addFixture(Season season, Calendar fixtureDate, Division division, Team homeTeam, Team awayTeam, Integer homeGoals,
			Integer awayGoals) {
		JsonLongDocument newIdLongDoc = bucket.counter("fixtureId", +1);
		Long newId = newIdLongDoc.content();
		String generatedIdString = "fix_" + newId;
		
		SimpleDateFormat niceSdf = new SimpleDateFormat("dd/MM/yyyy");
		
		String fixtureDateAsString = niceSdf.format(fixtureDate.getTime());
		
		JsonObject fixture = JsonObject.empty()
				.put("type", "fixture")
				.put("fixtureId", newId)
				.put("seasonNumber", season.getSeasonNumber())
				.put("homeTeamId", homeTeam.getTeamId())
				.put("awayTeamId", awayTeam.getTeamId())
				.put("divisionId", division.getDivisionId())				
				;
		
		Fixture fixtureObject = domainObjectFactory.createFixture(season, homeTeam, awayTeam);
		fixtureObject.setDivision(division);
		fixtureObject.setFixtureId(generatedIdString);
		
		if (fixture != null) {
			fixture.put("fixtureDate", fixtureDateAsString);
			fixtureObject.setFixtureDate(fixtureDate);
		}
		
		if (homeGoals != null) {
			fixture.put("homeGoals", homeGoals);
			fixtureObject.setHomeGoals(homeGoals);
		}
		
		if (awayGoals != null) {
			fixture.put("awayGoals", awayGoals);
			fixtureObject.setAwayGoals(awayGoals);
		}
		
		JsonDocument doc = JsonDocument.create(generatedIdString, fixture);
		JsonDocument response = bucket.upsert(doc);
		
		return fixtureObject;
	}

	@Override
	public SeasonDivision addSeasonDivision(Season season, Division division, int divisionPosition) {
		JsonDocument seasonJson = bucket.get("ssn_" + season.getSeasonNumber());
		
		if (seasonJson == null) throw new IllegalArgumentException("Season " + season + " does not exist");
		
		JsonArray divisions = seasonJson.content().getArray("divisions");
		
		if (divisions == null) {
			divisions = JsonArray.empty();
		}
		
		boolean found = false;
		for (Object object : divisions) {
			JsonObject jsonObject = (JsonObject) object;
			
			String divisionId = jsonObject.getString("id");
			
			if (division.getDivisionId().equals(divisionId)) {
				found = true;
				jsonObject.put ("position", divisionPosition);
			}
		}
		
		if (!found) {
			JsonArray teams = JsonArray.empty();
			
			JsonObject newDivision = JsonObject.empty()
					.put("id", division.getDivisionId())
					.put("position", divisionPosition)
					.put("teams", teams);
			
			divisions.add(newDivision);
		}
		
		bucket.upsert(seasonJson);
		
		return domainObjectFactory.createSeasonDivision(season, division, divisionPosition);
	}

	@Override
	public SeasonDivisionTeam addSeasonDivisionTeam(SeasonDivision seasonDivision, Team team) {
		JsonDocument seasonJson = bucket.get("ssn_" + seasonDivision.getSeason().getSeasonNumber());
		
		if (seasonJson == null) throw new IllegalArgumentException("Season " + seasonDivision.getSeason().getSeasonNumber() + " does not exist");

		JsonArray divisions = seasonJson.content().getArray("divisions");
		
		boolean found = false;
		for (Object object : divisions) {
			JsonObject jsonObject = (JsonObject) object;
			
			String divisionId = jsonObject.getString("id");
			
			if (seasonDivision.getDivision().getDivisionId().equals(divisionId)) {
				found = true;
				
				JsonArray teams = jsonObject.getArray("teams");
				
				boolean teamFound = false;
				for (Object teamObject : teams) {
					String teamId = (String) object;
					
					if (team.getTeamId().equals(teamId)) {
						teamFound = true;
					}
				}
				
				if (!teamFound) {
					teams.add(team.getTeamId());
				}
			}
		}

		if (!found) throw new IllegalArgumentException ("Division " + seasonDivision.getDivision().getDivisionId() + " does not exist");
		
		return domainObjectFactory.createSeasonDivisionTeam(seasonDivision, team);
	}

	@Override
	public Map<String, Division> getAllDivisions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Team> getAllTeams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Division getDivision(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SeasonDivision> getDivisionsForSeason(Season arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fixture getFixture(Season arg0, Division arg1, Team arg2, Team arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fixture> getFixtures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fixture> getFixturesForDivisionInSeason(SeasonDivision arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fixture> getFixturesForTeamInDivisionInSeason(Season arg0, Division arg1, Team arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fixture> getFixturesWithNoFixtureDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Season getSeason(Integer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeasonDivision getSeasonDivision(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeasonDivision getSeasonDivision(Season arg0, Division arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Season> getSeasons() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Team getTeam(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SeasonDivisionTeam> getTeamsForDivisionInSeason(SeasonDivision arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fixture> getUnplayedFixturesBeforeToday() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startSession() {
		cluster = CouchbaseCluster.create();
		bucket = cluster.openBucket(bucketName);
	}
	
	@Override
	public void closeSession() {
		cluster.disconnect();
	}

	public DomainObjectFactory getDomainObjectFactory() {
		return domainObjectFactory;
	}

	public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
		this.domainObjectFactory = domainObjectFactory;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
}
