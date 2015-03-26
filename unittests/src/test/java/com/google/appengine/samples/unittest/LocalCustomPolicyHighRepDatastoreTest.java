package com.google.appengine.samples.unittest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.*;

// [START HRD_example_2]
// ...
import com.google.appengine.api.datastore.dev.HighRepJobPolicy;

public class LocalCustomPolicyHighRepDatastoreTest {
    private static final class CustomHighRepJobPolicy implements HighRepJobPolicy {
        static int newJobCounter = 0;
        static int existingJobCounter = 0;
        @Override
        public boolean shouldApplyNewJob(Key entityGroup) {
            // every other new job fails to apply
            return newJobCounter++ % 2 == 0;
        }

        @Override
        public boolean shouldRollForwardExistingJob(Key entityGroup) {
            // every other existing job fails to apply
            return existingJobCounter++ % 2 == 0;
        }
    }

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setAlternateHighRepJobPolicyClass(CustomHighRepJobPolicy.class));

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testEventuallyConsistentGlobalQueryResult() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(new Entity("yam")); // applies
        ds.put(new Entity("yam")); // does not apply
        // first global query only sees the first Entity
        assertEquals(1, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
        // second global query sees both Entities because we "groom" (attempt to
        // apply unapplied jobs) after every query
        assertEquals(2, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
    }
}
// [END HRD_example_2]
