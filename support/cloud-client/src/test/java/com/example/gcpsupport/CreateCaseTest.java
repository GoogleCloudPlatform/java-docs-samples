/**
 * Copyright (C) 2022 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.gcpsupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.io.IOException;
import com.google.api.services.cloudsupport.v2beta.model.CloudSupportCase;

public class CreateCaseTest 
{
    
    private static CloudSupportCase csc; 

    private void createCase() {
        if (csc != null) return;

        String newCasePath = System.getenv("NEW_CASE_PATH");
        String parentResource = System.getenv("PARENT_RESOURCE");

        try {
            csc = CreateCase.createCase(parentResource, newCasePath);
        } catch (IOException e) {
            System.out.println("IOException caught! \n" + e);
        }
    }

    @Test
    public void createsNewCase()
    {
        createCase();
     
        boolean b = csc instanceof CloudSupportCase;
        assertTrue(b);
       
    }

    @Test
    public void createsCase_WithSameDescription()
    {
        createCase();

        String caseDisplayName = csc.getDisplayName().toLowerCase();
        String expectedDisplayName = "test case";
        assertEquals(expectedDisplayName, caseDisplayName);
    }

}
