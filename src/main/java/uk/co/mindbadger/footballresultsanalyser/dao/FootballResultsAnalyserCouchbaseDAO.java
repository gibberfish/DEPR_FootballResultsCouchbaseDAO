package uk.co.mindbadger.footballresultsanalyser.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import com.couchbase.client.java.view.Stale;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;

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
	
	/* ****************** SEASON ****************** */
	
	private Season mapJsonToSeason(JsonObject jsonSeason) {
		return domainObjectFactory.createSeason(jsonSeason.getInt("seasonNumber"));
	}
	
	@Override
	public Season addSeason(Integer seasonNumber) {
		
		JsonArray divisions = JsonArray.empty();
		
		JsonObject season = JsonObject.empty()
				.put("type", "season")
				.put("seasonNumber", seasonNumber)
				.put("divisions", divisions);
		
		JsonDocument doc = JsonDocument.create("ssn_" + seasonNumber.toString(), season);
		bucket.upsert(doc);

		return mapJsonToSeason(doc.content());
	}
	
	@Override
	public List<Season> getSeasons() {
		List<Season> seasons = new ArrayList<Season> ();
		
		ViewResult result = bucket.query(ViewQuery.from("season", "by_id").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			JsonObject seasonRow = (JsonObject) row.value();
			seasons.add(mapJsonToSeason(seasonRow));
		}
		
		return seasons;
	}

	@Override
	public Season getSeason(Integer seasonNumber) {
		JsonDocument jsonDocument = bucket.get("ssn_"+seasonNumber);
		
		return (jsonDocument == null ? null : mapJsonToSeason(jsonDocument.content()));
	}

	/* ****************** DIVISION ****************** */
	
	private Division mapJsonToDivision (JsonObject jsonDivision) {
		Division divisionObject = domainObjectFactory.createDivision(jsonDivision.getString("divisionName"));
		Long divisionId = jsonDivision.getLong("divisionId");
		divisionObject.setDivisionId(divisionId.toString());
		return divisionObject;
	}
	
	@Override
	public Division addDivision(String divisionName) {
		JsonLongDocument newIdLongDoc = null;
		try {
			newIdLongDoc = bucket.counter("divisionId", +1);
		} catch (Exception e) {
			newIdLongDoc = JsonLongDocument.create("divisionId", 0L);
			bucket.insert(newIdLongDoc);
		}
		Long newId = newIdLongDoc.content();
		
		JsonObject division = JsonObject.empty()
				.put("type", "division")
				.put("divisionId", newId)
				.put("divisionName", divisionName);
		
		String generateIdString = "div_" + newId;
		JsonDocument doc = JsonDocument.create(generateIdString, division);
		bucket.upsert(doc);
		return mapJsonToDivision(doc.content());
	}
	
	@Override
	public Division getDivision(String divisionId) {
		String generateIdString = "div_" + divisionId;
		JsonDocument doc = bucket.get(generateIdString);
		
		return (doc == null ? null : mapJsonToDivision(doc.content()));
	}
	
	@Override
	public Map<String, Division> getAllDivisions() {
		Map<String, Division> divisions = new HashMap<String, Division> ();
		
		ViewResult result = bucket.query(ViewQuery.from("division", "by_id").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			JsonObject divisionRow = (JsonObject) row.value();
			Division division = mapJsonToDivision (divisionRow);
			divisions.put(division.getDivisionId(), division);
		}
		
		return divisions;
	}
	
	/* ****************** TEAM ****************** */
	
	private Team mapJsonToTeam (JsonObject jsonDivision) {
		Team teamObject = domainObjectFactory.createTeam(jsonDivision.getString("teamName"));
		Long teamId = jsonDivision.getLong("teamId");
		teamObject.setTeamId(teamId.toString());
		return teamObject;
	}

	@Override
	public Team addTeam(String teamName) {
		JsonLongDocument newIdLongDoc = null;
		try {
			newIdLongDoc = bucket.counter("teamId", +1);
		} catch (Exception e) {
			newIdLongDoc = JsonLongDocument.create("teamId", 0L);
			bucket.insert(newIdLongDoc);
		}
		Long newId = newIdLongDoc.content();
		
		JsonObject team = JsonObject.empty()
				.put("type", "team")
				.put("teamId", newId)
				.put("teamName", teamName);
		
		String generatedIdString = "team_" + newId;
		JsonDocument doc = JsonDocument.create(generatedIdString, team);
		bucket.upsert(doc);

		return mapJsonToTeam(doc.content());
	}

	@Override
	public Team getTeam(String teamId) {
		String generateIdString = "team_" + teamId;
		JsonDocument doc = bucket.get(generateIdString);
		
		return (doc == null ? null : mapJsonToTeam(doc.content()));
	}

	@Override
	public Map<String, Team> getAllTeams() {
		Map<String, Team> teams = new HashMap<String, Team> ();
		
		ViewResult result = bucket.query(ViewQuery.from("team", "by_id").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			JsonObject teamRow = (JsonObject) row.value();
			Team team = mapJsonToTeam (teamRow);
			teams.put(team.getTeamId(), team);
		}
		
		return teams;
	}

	/* ****************** FIXTURE ****************** */
	
	@Override
	public Fixture addFixture(Season season, Calendar fixtureDate, Division division, Team homeTeam, Team awayTeam, Integer homeGoals,
			Integer awayGoals) {
		
		JsonLongDocument newIdLongDoc = null;
		try {
			newIdLongDoc = bucket.counter("fixtureId", +1);
		} catch (Exception e) {
			newIdLongDoc = JsonLongDocument.create("fixtureId");
			bucket.insert(newIdLongDoc);
		}
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
		bucket.upsert(doc);
		
		return fixtureObject;
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
	public List<Fixture> getUnplayedFixturesBeforeToday() {
		// TODO Auto-generated method stub
		return null;
	}

	/* ****************** SEASON DIVISION ****************** */
	
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
	public Set<SeasonDivision> getDivisionsForSeason(Season arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
	public SeasonDivision getSeasonDivision(Season arg0, Division arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SeasonDivisionTeam> getTeamsForDivisionInSeason(SeasonDivision arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public SeasonDivision getSeasonDivision(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* ****************** SEASON DIVISION TEAM ****************** */
	
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
					String teamId = (String) teamObject;
					
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
		
		bucket.upsert(seasonJson);
		
		return domainObjectFactory.createSeasonDivisionTeam(seasonDivision, team);
	}
	
	/* ****************** UTILS, GETTERS & SETTERS ****************** */
	
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
