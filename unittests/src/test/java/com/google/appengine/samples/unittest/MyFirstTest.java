package com.google.appengine.samples.unittest;

// [START framework]
import org.junit.Test;

import static org.junit.Assert.*;

public class MyFirstTest {
    @Test
    public void testAddition() {
        assertEquals(4, 2 + 2);
    }
}
// [END framework]