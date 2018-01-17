package com.example.firestore;

import com.example.firestore.snippets.ManageDataSnippetsIT;
import com.example.firestore.snippets.model.City;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.Map;
import org.junit.BeforeClass;

/**
 * Base class for tests like {@link ManageDataSnippetsIT}.
 */
public class BaseIntegrationTest {

  protected static String projectId = "java-docs-samples-firestore";
  protected static Firestore db;

  @BeforeClass
  public static void baseSetup() throws Exception {
    FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId(projectId)
        .build();
    db = firestoreOptions.getService();
    deleteAllDocuments(db);
  }

  protected DocumentSnapshot getDocumentData(DocumentReference docRef) throws Exception {
    return docRef.get().get();
  }

  protected Map<String, Object> getDocumentDataAsMap(DocumentReference docRef) throws Exception {
    DocumentSnapshot snapshot = docRef.get().get();
    if (!snapshot.exists()) {
      throw new RuntimeException("Document does not exist: " + docRef.getPath());
    }

    return snapshot.getData();
  }

  protected City getDocumentDataAsCity(DocumentReference docRef) throws Exception {
    return docRef.get().get().toObject(City.class);
  }

  protected  static void deleteAllDocuments(Firestore db) throws Exception {
    ApiFuture<QuerySnapshot> future = db.collection("cities").get();
    QuerySnapshot querySnapshot = future.get();
    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
      // block on delete operation
      db.collection("cities").document(doc.getId()).delete().get();
    }
  }

}
