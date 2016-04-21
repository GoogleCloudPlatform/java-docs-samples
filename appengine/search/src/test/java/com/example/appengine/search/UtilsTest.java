package com.example.appengine.search;

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class UtilsTest {
  private static final String INDEX = "UtilsTestIndex";
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper();

  @Before
  public void setUp() throws Exception {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void indexADocument_successfullyInvoked() throws Exception {
    String id = "test";
    Document doc = Document.newBuilder()
        .setId(id)
        .addField(Field.newBuilder().setName("f").setText("v"))
        .build();
    Utils.indexADocument(INDEX, doc);
    // get the document by id
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX).build();
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    Document fetched = index.get(id);
    assertThat(fetched.getOnlyField("f").getText())
        .named("A value of the fetched document")
        .isEqualTo("v");
  }
}