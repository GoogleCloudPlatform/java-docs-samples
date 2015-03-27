package com.google.appengine.samples.unittest;

// [START local_capabilities]
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalCapabilitiesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.api.ApiProxy;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class CapabilitiesTest {

    private LocalServiceTestHelper helper;

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test(expected = ApiProxy.CapabilityDisabledException.class)
    public void testDisabledDatastore() {
        Capability testOne = new Capability("datastore_v3");
        CapabilityStatus testStatus = CapabilityStatus.DISABLED;
        //Initialize
        LocalCapabilitiesServiceTestConfig config =
                new LocalCapabilitiesServiceTestConfig().setCapabilityStatus(testOne, testStatus);
        helper = new LocalServiceTestHelper(config);
        helper.setUp();
        FetchOptions fo = FetchOptions.Builder.withLimit(10);
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        assertEquals(0, ds.prepare(new Query("yam")).countEntities(fo));
    }
}
// [END local_capabilities]
