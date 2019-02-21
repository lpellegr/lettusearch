package com.redislabs.lettusearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redislabs.lettusearch.search.AddOptions;
import com.redislabs.lettusearch.search.Schema;
import com.redislabs.lettusearch.search.SearchOptions;
import com.redislabs.lettusearch.search.SearchResults;
import com.redislabs.lettusearch.search.api.sync.SearchCommands;
import com.redislabs.lettusearch.search.field.TextField;
import com.redislabs.lettusearch.search.field.TextField.Matcher;
import com.redislabs.lettusearch.suggest.SuggestAddOptions;
import com.redislabs.lettusearch.suggest.SuggestGetOptions;
import com.redislabs.lettusearch.suggest.SuggestResult;
import com.redislabs.lettusearch.suggest.api.sync.SuggestCommands;

public class SearchTest {

	private final static String INDEX = "testIndex";
	private static final String FIELD1 = "field1";
	private static final String FIELD2 = "field2";
	private static final String DOC1 = "doc1";
	private static final String DOC2 = "doc2";
	private RediSearchClient client;

	@Before
	public void setup() {
		client = RediSearchClient.create("redis://localhost");
		client.connect().sync().flushall();
	}

	@After
	public void teardown() {
		client.shutdown();
	}

	@Test
	public void testAdd() {
		StatefulRediSearchConnection<String, String> connection = client.connect();
		SearchCommands<String, String> commands = connection.sync();
		connection.sync().create(INDEX, Schema.builder().field(TextField.builder().name(FIELD1).build())
				.field(TextField.builder().name(FIELD2).sortable(true).build()).build());
		Map<String, String> fields1 = new HashMap<>();
		fields1.put(FIELD1, "this is doc 1 value 1");
		fields1.put(FIELD2, "this is doc 1 value 2");
		Map<String, String> fields2 = new HashMap<>();
		fields2.put(FIELD1, "this is doc 2 value 1");
		fields2.put(FIELD2, "this is doc 2 value 2");
		commands.add(INDEX, DOC1, 1, fields1, AddOptions.builder().build());
		commands.add(INDEX, DOC2, 1, fields2, AddOptions.builder().build());
		connection.close();
	}

	@Test
	public void testPhoneticFields() {
		String index = "traps";
		StatefulRediSearchConnection<String, String> connection = client.connect();
		SearchCommands<String, String> commands = connection.sync();
		commands.create(index,
				Schema.builder().field(TextField.builder().name("word").matcher(Matcher.English).build()).build());
		Map<String, String> fields1 = new HashMap<>();
		fields1.put("word", "kar");
		Map<String, String> fields2 = new HashMap<>();
		fields2.put("word", "car");
		commands.add(index, DOC1, 1, fields1, AddOptions.builder().build());
		commands.add(index, DOC2, 1, fields2, AddOptions.builder().build());
		SearchResults<String, String> results = commands.search(index, "qar",
				SearchOptions.builder().withScores(true).build());
		Assert.assertEquals(2, results.getCount());
		connection.close();
	}

	@Test
	public void testSuggestions() {
		String key = "artists";
		StatefulRediSearchConnection<String, String> connection = client.connect();
		SuggestCommands<String, String> commands = connection.sync();
		String hancock = "Herbie Hancock";
		String mann = "Herbie Mann";
		commands.sugadd(key, hancock, 1, SuggestAddOptions.builder().build());
		commands.sugadd(key, mann, 1, SuggestAddOptions.builder().build());
		commands.sugadd(key, "DJ Herbie", 1, SuggestAddOptions.builder().build());
		List<SuggestResult<String>> results = commands.sugget(key, "Herb",
				SuggestGetOptions.builder().withScores(true).withPayloads(true).build());
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(result -> hancock.equals(result.getString())));
		Assert.assertTrue(results.stream().anyMatch(result -> mann.equals(result.getString())));
	}

	@Test
	public void testSearch() {
		testAdd();
		StatefulRediSearchConnection<String, String> connection = client.connect();
		SearchResults<String, String> results = connection.sync().search(INDEX, "value",
				SearchOptions.builder().withScores(true).build());
		Assert.assertEquals(2, results.getCount());
		Assert.assertEquals(2, results.getResults().size());
		Assert.assertEquals(DOC2, results.getResults().get(0).getDocumentId());
		Assert.assertEquals(DOC1, results.getResults().get(1).getDocumentId());
		Assert.assertEquals("this is doc 1 value 1", results.getResults().get(1).getFields().get(FIELD1));
		Assert.assertEquals("this is doc 2 value 2", results.getResults().get(0).getFields().get(FIELD2));
		Assert.assertEquals(0.1333333, results.getResults().get(0).getScore(), 0.000001);
		connection.close();
	}

	@Test
	public void testSearchNoContent() {
		testAdd();
		StatefulRediSearchConnection<String, String> connection = client.connect();
		SearchResults<String, String> results = connection.sync().search(INDEX, "value",
				SearchOptions.builder().withScores(true).noContent(true).build());
		Assert.assertEquals(2, results.getCount());
		Assert.assertEquals(2, results.getResults().size());
		Assert.assertEquals(DOC2, results.getResults().get(0).getDocumentId());
		Assert.assertEquals(DOC1, results.getResults().get(1).getDocumentId());
		Assert.assertEquals(0.1333333, results.getResults().get(0).getScore(), 0.000001);
	}

}