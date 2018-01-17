package com.example.firestore.snippets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.firestore.BaseIntegrationTest;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentChange.Type;
import com.google.cloud.firestore.DocumentReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ListenDataSnippetsIT extends BaseIntegrationTest {

  private static QueryDataSnippets queryDataSnippets;
  private static ListenDataSnippets listenDataSnippets;

  // TODO: tear down

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    queryDataSnippets = new QueryDataSnippets(db);
    queryDataSnippets.prepareExamples();

    listenDataSnippets = new ListenDataSnippets(db);
  }

  @Test
  public void testListenDocument() throws Exception {
    Map<String, Object> expectedData = listenDataSnippets.listenToDocument();
    DocumentReference docRef = db.collection("cities").document("SF");
    assertTrue(Objects.equals(expectedData, getDocumentDataAsMap(docRef)));
  }

  @Test
  public void testListenForMultiple() throws Exception {
    List<String> cities = listenDataSnippets.listenForMultiple();

    assertEquals("Two cities in CA", cities.size(), 2);
    assertTrue("SF in CA", cities.contains("San Francisco"));
    assertTrue("LA in CA", cities.contains("Los Angeles"));
  }

  @Test
  public void testListenForChanges() throws Exception {
    List<DocumentChange> changes = listenDataSnippets.listenForChanges();

    assertEquals("Two changes in snapshot.", changes.size(), 2);
    assertEquals("First change is ADDED", changes.get(0).getType(), Type.ADDED);
    assertEquals("Second change is ADDED", changes.get(1).getType(), Type.ADDED);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    deleteAllDocuments(db);
  }
}
