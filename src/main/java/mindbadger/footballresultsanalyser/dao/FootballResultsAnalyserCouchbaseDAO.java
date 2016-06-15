package mindbadger.footballresultsanalyser.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import mindbadger.footballresultsanalyser.domain.Division;
import mindbadger.footballresultsanalyser.domain.DomainObjectFactory;
import mindbadger.footballresultsanalyser.domain.Fixture;
import mindbadger.footballresultsanalyser.domain.Season;
import mindbadger.footballresultsanalyser.domain.SeasonDivision;
import mindbadger.footballresultsanalyser.domain.SeasonDivisionTeam;
import mindbadger.footballresultsanalyser.domain.Team;

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

	private String generateCouchbaseSeasonKey (String seasonNumber) {
		return "ssn_" + seasonNumber;
	}

	private String generateCouchbaseSeasonKey (Integer seasonNumber) {
		return generateCouchbaseSeasonKey(seasonNumber.toString());
	}
	
	@Override
	public Season getSeason(Integer seasonNumber) {
		JsonDocument jsonDocument = bucket.get(generateCouchbaseSeasonKey(seasonNumber));
		return (jsonDocument == null ? null : mapJsonToSeason(jsonDocument.content()));
	}
	
	@Override
	public Season addSeason(Integer seasonNumber) {
		Season existingSeason = getSeason (seasonNumber);
		if (existingSeason != null)	return existingSeason;
		
		JsonArray divisions = JsonArray.empty();
		
		JsonObject season = JsonObject.empty()
				.put("type", "season")
				.put("seasonNumber", seasonNumber)
				.put("divisions", divisions);
		
		JsonDocument doc = JsonDocument.create(generateCouchbaseSeasonKey(seasonNumber), season);
		bucket.upsert(doc);
		
		return mapJsonToSeason(doc.content());
	}
	
	@Override
	public List<Season> getSeasons() {
		List<Season> seasons = new ArrayList<Season> ();
		
		ViewResult result = bucket.query(ViewQuery.from("season", "by_id").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject seasonRow = doc.content();
			//JsonObject seasonRow = (JsonObject) row.value();
			seasons.add(mapJsonToSeason(seasonRow));
		}
		
		return seasons;
	}

	/* ****************** DIVISION ****************** */
	
	private Division mapJsonToDivision (JsonObject jsonDivision) {
		Division divisionObject = domainObjectFactory.createDivision(jsonDivision.getString("divisionName"));
		Long divisionId = jsonDivision.getLong("divisionId");
		divisionObject.setDivisionId(divisionId.toString());
		return divisionObject;
	}

	private String generateCouchbaseDivisionKey (String divisionId) {
		return "div_" + divisionId;
	}

	public Division getDivisionByName(String divisionName) {
		Division division = null;
		
		ViewResult result = bucket.query(ViewQuery.from("division", "by_name").stale(Stale.FALSE).key(divisionName));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject divisionRow = doc.content();
			division = mapJsonToDivision (divisionRow);
		}
		
		return division;
	}

	@Override
	public Division getDivision(String divisionId) {
		JsonDocument doc = bucket.get(generateCouchbaseDivisionKey(divisionId));
		return (doc == null ? null : mapJsonToDivision(doc.content()));
	}
	
	@Override
	public Division addDivision(String divisionName) {
		Division existingDivision = getDivisionByName (divisionName);
		if (existingDivision != null) return existingDivision;
		
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
		
		JsonDocument doc = JsonDocument.create(generateCouchbaseDivisionKey(newId.toString()), division);
		bucket.upsert(doc);
		return mapJsonToDivision(doc.content());
	}
	
	@Override
	public Map<String, Division> getAllDivisions() {
		Map<String, Division> divisions = new HashMap<String, Division> ();
		
		ViewResult result = bucket.query(ViewQuery.from("division", "by_id").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject divisionRow = doc.content();
//			JsonObject divisionRow = (JsonObject) row.value();
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

	private String generateCouchbaseTeamKey (String teamId) {
		return "team_" + teamId;
	}

	public Team getTeamByName(String teamName) {
		Team team = null;
		
		ViewResult result = bucket.query(ViewQuery.from("team", "by_name").stale(Stale.FALSE).key(teamName));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject teamRow = doc.content();
			team = mapJsonToTeam (teamRow);
		}
		
		return team;
	}

	@Override
	public Team addTeam(String teamName) {
		Team existingTeam = getTeamByName(teamName);
		if (existingTeam != null) return existingTeam;
		
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
		
		JsonDocument doc = JsonDocument.create(generateCouchbaseTeamKey(newId.toString()), team);
		bucket.upsert(doc);

		return mapJsonToTeam(doc.content());
	}

	@Override
	public Team getTeam(String teamId) {
		JsonDocument doc = bucket.get(generateCouchbaseTeamKey(teamId));
		return (doc == null ? null : mapJsonToTeam(doc.content()));
	}

	@Override
	public Map<String, Team> getAllTeams() {
		Map<String, Team> teams = new HashMap<String, Team> ();
		
		ViewResult result = bucket.query(ViewQuery.from("team", "by_id").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject teamRow = doc.content();
			Team team = mapJsonToTeam (teamRow);
			teams.put(team.getTeamId(), team);
		}
		
		return teams;
	}

	/* ****************** SEASON DIVISION ****************** */
	
	@Override
	public SeasonDivision addSeasonDivision(Season season, Division division, int divisionPosition) {
		JsonDocument seasonJson = bucket.get(generateCouchbaseSeasonKey(season.getSeasonNumber()));
		
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
	public SeasonDivision getSeasonDivision(Season season, Division division) {
		List<SeasonDivision> seasonDivisions = getDivisionsForSeason(season);
		
		for (SeasonDivision seasonDivision : seasonDivisions) {
			if (division.getDivisionId().equals(seasonDivision.getDivision().getDivisionId())) {
				return seasonDivision;
			}
		}
		
		throw new IllegalArgumentException("Season/Division " + season.getSeasonNumber() + "/" + division.getDivisionName() + " does not exist");
	}
	
	@Override
	public List<SeasonDivision> getDivisionsForSeason(Season season) {
		List<SeasonDivision> seasonDivisions = new ArrayList<SeasonDivision> ();
		
		JsonDocument jsonDocument = bucket.get(generateCouchbaseSeasonKey(season.getSeasonNumber()));
		
		JsonArray divisions = jsonDocument.content().getArray("divisions");
		
		for (Object divisionObject : divisions) {
			JsonObject seasonDivisionObject = (JsonObject) divisionObject;
			String divisionId = seasonDivisionObject.getString("id");
			Integer position = seasonDivisionObject.getInt("position");
			
			Division division = getDivision(divisionId);
			
			SeasonDivision seasonDivision = domainObjectFactory.createSeasonDivision(season, division, position);
			
			seasonDivisions.add(seasonDivision);
		}
		
		return seasonDivisions;
	}
	
	/* ****************** SEASON DIVISION TEAM ****************** */
	
	@Override
	public SeasonDivisionTeam addSeasonDivisionTeam(SeasonDivision seasonDivision, Team team) {
		JsonDocument seasonJson = bucket.get(generateCouchbaseSeasonKey(seasonDivision.getSeason().getSeasonNumber()));
		
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
	
	@Override
	public List<SeasonDivisionTeam> getTeamsForDivisionInSeason(SeasonDivision seasonDivision) {
		List<SeasonDivisionTeam> seasonDivisionTeams = new ArrayList<SeasonDivisionTeam> ();
		
		JsonDocument jsonDocument = bucket.get(generateCouchbaseSeasonKey(seasonDivision.getSeason().getSeasonNumber()));
		
		JsonArray divisions = jsonDocument.content().getArray("divisions");
		
		for (Object divisionObject : divisions) {
			JsonObject seasonDivisionObject = (JsonObject) divisionObject;
			String divisionId = seasonDivisionObject.getString("id");
			
			if (seasonDivision.getDivision().getDivisionId().equals(divisionId)) {
				JsonArray jsonTeamsArray = seasonDivisionObject.getArray("teams");
				
				for (Object teamObject : jsonTeamsArray) {
					String teamId = (String) teamObject;
					
					Team team = getTeam(teamId);
					
					SeasonDivisionTeam seasonDivisionTeam = domainObjectFactory.createSeasonDivisionTeam(seasonDivision, team);
					
					seasonDivisionTeams.add(seasonDivisionTeam);
				}
			}
		}
		
		return seasonDivisionTeams;
	}
	
	/* ****************** FIXTURE ****************** */
	
	public Fixture getUnqiueFixture(Season season, Division division, Team homeTeam, Team awayTeam) {
		Fixture fixture = null;
		
		JsonArray jsonArrayKey = JsonArray.empty();
		jsonArrayKey.add(season.getSeasonNumber());
		jsonArrayKey.add(division.getDivisionId());
		jsonArrayKey.add(homeTeam.getTeamId());
		jsonArrayKey.add(awayTeam.getTeamId());
		
		ViewResult result = bucket.query(ViewQuery.from("fixture", "unique").key(jsonArrayKey).stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject teamRow = doc.content();
			fixture = mapJsonToFixture (teamRow);
		}
		
		return fixture;
	}

	private Fixture mapJsonToFixture(JsonObject jsonFixture) {
		Season season = getSeason(jsonFixture.getInt("seasonNumber"));
		Division division = getDivision(jsonFixture.getString("divisionId"));
		Team homeTeam = getTeam(jsonFixture.getString("homeTeamId"));
		Team awayTeam = getTeam(jsonFixture.getString("awayTeamId"));
		Integer fixtureId = jsonFixture.getInt("fixtureId");
		
		Fixture fixtureObject = domainObjectFactory.createFixture(season, homeTeam, awayTeam);
		fixtureObject.setFixtureId(fixtureId.toString());
		fixtureObject.setDivision(division);
		
		String fixtureDateString = jsonFixture.getString("fixtureDate");
		SimpleDateFormat niceSdf = new SimpleDateFormat("dd/MM/yyyy");
		Integer homeGoals = jsonFixture.getInt("homeGoals");
		Integer awayGoals = jsonFixture.getInt("awayGoals");
		
		if (fixtureDateString != null) {
			Calendar fixtureDate = Calendar.getInstance();
			try {
				fixtureDate.setTime(niceSdf.parse(fixtureDateString));
				fixtureObject.setFixtureDate(fixtureDate);
			} catch (ParseException e) {
				e.printStackTrace();
				throw new RuntimeException (e);
			}
		}
		
		if (homeGoals != null) {
			fixtureObject.setHomeGoals(homeGoals);
		}

		if (awayGoals != null) {
			fixtureObject.setAwayGoals(awayGoals);
		}

		return fixtureObject;
	}

	private String generateCouchbaseFixtureKey (String fixtureId) {
		return "fix_" + fixtureId;
	}
	
	@Override
	public Fixture addFixture(Season season, Calendar fixtureDate, Division division, Team homeTeam, Team awayTeam, Integer homeGoals,
			Integer awayGoals) {
		
		if (season == null) throw new IllegalArgumentException("Please supply a season when creating a fixture");
		if (division == null) throw new IllegalArgumentException("Please supply a division when creating a fixture");
		if (homeTeam == null) throw new IllegalArgumentException("Please supply a home team when creating a fixture");
		if (awayTeam == null) throw new IllegalArgumentException("Please supply an away team when creating a fixture");
		if (homeGoals != null && fixtureDate == null) throw new IllegalArgumentException("Please supply a fixture date team when creating a played fixture");
		
		Fixture fixtureObject = getUnqiueFixture (season, division, homeTeam, awayTeam);
		
		if (fixtureDate != null && fixtureObject != null && fixtureObject.getFixtureDate() != null) {
			if (fixtureObject.getFixtureDate().before(fixtureDate) &&
					fixtureObject.getHomeGoals() != null && homeGoals != null)
				throw new ChangeScoreException ("Can't save a playoff result over a regular game");
		}
		
		Long newId = null;
		
		if (fixtureObject != null) {
			newId = Long.valueOf(fixtureObject.getFixtureId());
		} else {
			JsonLongDocument newIdLongDoc = null;
			try {
				newIdLongDoc = bucket.counter("fixtureId", +1);
			} catch (Exception e) {
				newIdLongDoc = JsonLongDocument.create("fixtureId", 0L);
				bucket.insert(newIdLongDoc);
			}
			newId = newIdLongDoc.content();
			
			fixtureObject = domainObjectFactory.createFixture(season, homeTeam, awayTeam);
		}

		JsonObject fixture = JsonObject.empty()
				.put("type", "fixture")
				.put("fixtureId", newId)
				.put("seasonNumber", season.getSeasonNumber())
				.put("homeTeamId", homeTeam.getTeamId())
				.put("awayTeamId", awayTeam.getTeamId())
				.put("divisionId", division.getDivisionId())				
				;
		
		if (fixtureDate != null) {			
			SimpleDateFormat niceSdf = new SimpleDateFormat("dd/MM/yyyy");
			String fixtureDateAsString = niceSdf.format(fixtureDate.getTime());
			fixture.put("fixtureDate", fixtureDateAsString);
		} else {
			fixture.removeKey("fixtureDate");
		}
		
		if (homeGoals != null) {
			fixture.put("homeGoals", homeGoals);
		} else {
			fixture.removeKey("homeGoals");
		}
		
		if (awayGoals != null) {
			fixture.put("awayGoals", awayGoals);
		} else {
			fixture.removeKey("awayGoals");
		}
		
		JsonDocument doc = JsonDocument.create(generateCouchbaseFixtureKey(newId.toString()), fixture);
		bucket.upsert(doc);
		
		fixtureObject.setDivision(division);
		fixtureObject.setFixtureId(newId.toString());
		fixtureObject.setFixtureDate(fixtureDate);
		fixtureObject.setHomeGoals(homeGoals);
		fixtureObject.setAwayGoals(awayGoals);
		
		return fixtureObject;
	}

	@Override
	public Fixture getFixture(String fixtureId) {
		JsonDocument doc = bucket.get(generateCouchbaseFixtureKey(fixtureId));
		return (doc == null ? null : mapJsonToFixture(doc.content()));
	}

	@Override
	public List<Fixture> getFixturesWithNoFixtureDate() {
		List<Fixture> fixtures = new ArrayList<Fixture> ();
		
		ViewResult result = bucket.query(ViewQuery.from("fixture", "no_fixture_date").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject fixtureRow = doc.content();
			Fixture fixture = mapJsonToFixture (fixtureRow);
			fixtures.add(fixture);
		}
		
		return fixtures;
	}

	@Override
	public List<Fixture> getFixtures() {
		List<Fixture> fixtures = new ArrayList<Fixture> ();
		
		ViewResult result = bucket.query(ViewQuery.from("fixture", "by_id").stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject fixtureRow = doc.content();
			Fixture fixture = mapJsonToFixture (fixtureRow);
			fixtures.add(fixture);
		}
		
		return fixtures;
	}

	
	@Override
	public List<Fixture> getUnplayedFixturesBeforeToday() {
		List<Fixture> fixtures = new ArrayList<Fixture> ();
		
		Calendar now = Calendar.getInstance();
		String year = (new SimpleDateFormat("yyyy")).format(now.getTime());
		String month = (new SimpleDateFormat("MM")).format(now.getTime());
		String day = (new SimpleDateFormat("dd")).format(now.getTime());
		
		JsonArray endKey = JsonArray.from(year, month, day);
		
		ViewResult result = bucket.query(ViewQuery.from("fixture", "unplayed").stale(Stale.FALSE).endKey(endKey));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject fixtureRow = doc.content();
			Fixture fixture = mapJsonToFixture (fixtureRow);
			fixtures.add(fixture);
		}
		
		return fixtures;
	}

	@Override
	public List<Fixture> getFixturesForDivisionInSeason(SeasonDivision seasonDivision) {
		List<Fixture> fixtures = new ArrayList<Fixture> ();
		
		JsonArray jsonFromArrayKey = JsonArray.empty();
		jsonFromArrayKey.add(seasonDivision.getSeason().getSeasonNumber());
		jsonFromArrayKey.add(seasonDivision.getDivision().getDivisionId());

		JsonArray jsonToArrayKey = JsonArray.empty();
		jsonToArrayKey.add(seasonDivision.getSeason().getSeasonNumber());
		jsonToArrayKey.add(seasonDivision.getDivision().getDivisionId());
		jsonToArrayKey.add("\u0fff");

		ViewResult result = bucket.query(ViewQuery.from("fixture", "unique").startKey(jsonFromArrayKey)
				.endKey(jsonToArrayKey).stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject teamRow = doc.content();
			Fixture fixture = mapJsonToFixture (teamRow);
			fixtures.add(fixture);
		}
		
		return fixtures;
	}

	@Override
	public List<Fixture> getFixturesForTeamInDivisionInSeason(SeasonDivision seasonDivision, Team team) {
		List<Fixture> fixtures = new ArrayList<Fixture> ();
		
		JsonArray jsonFromArrayKey = JsonArray.empty();
		jsonFromArrayKey.add(seasonDivision.getSeason().getSeasonNumber());
		jsonFromArrayKey.add(seasonDivision.getDivision().getDivisionId());
		jsonFromArrayKey.add(team.getTeamId());

		JsonArray jsonToArrayKey = JsonArray.empty();
		jsonToArrayKey.add(seasonDivision.getSeason().getSeasonNumber());
		jsonToArrayKey.add(seasonDivision.getDivision().getDivisionId());
		jsonToArrayKey.add(team.getTeamId());
		jsonToArrayKey.add("\u0fff");

		ViewResult result = bucket.query(ViewQuery.from("fixture", "unique").startKey(jsonFromArrayKey)
				.endKey(jsonToArrayKey).stale(Stale.FALSE));
		
		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject teamRow = doc.content();
			Fixture fixture = mapJsonToFixture (teamRow);
			fixtures.add(fixture);
		}
		
		result = bucket.query(ViewQuery.from("fixture", "unique_away_home").startKey(jsonFromArrayKey)
				.endKey(jsonToArrayKey).stale(Stale.FALSE));

		for (ViewRow row : result.allRows()) {
			String key = (String) row.id();
			JsonDocument doc = bucket.get(key);
			JsonObject teamRow = doc.content();
			Fixture fixture = mapJsonToFixture (teamRow);
			fixtures.add(fixture);
		}

		return fixtures;
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
