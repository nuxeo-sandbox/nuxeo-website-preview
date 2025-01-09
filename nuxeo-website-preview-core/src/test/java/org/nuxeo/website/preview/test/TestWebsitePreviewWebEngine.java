/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.website.preview.test;

import java.io.IOException;

import jakarta.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.webengine.test.WebEngineFeature;
import org.nuxeo.http.test.CloseableHttpResponse;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.nuxeo.http.test.HttpClientTestRule;

import static org.junit.Assert.*;

@RunWith(FeaturesRunner.class)
@Features({ WebEngineFeature.class })
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-website-preview-core" })
public class TestWebsitePreviewWebEngine {

    public static final String LOGO_FILE_NAME = "NUXEO-LOGO-1.png";

    public static final String PATH_TO_LOGO = "img/" + LOGO_FILE_NAME;

    protected DocumentModel testDocsFolder;

    @Inject
    protected ServletContainerFeature servletContainerFeature;

    @Rule
    public final HttpClientTestRule httpClient = HttpClientTestRule.builder()
            .url(() -> servletContainerFeature.getHttpUrl()+"/WSP/")
            .adminCredentials()
            .build();

    @Inject
    protected CoreSession coreSession;

    @Inject
    protected EventService eventService;

    @Before
    public void setup() {
        testDocsFolder = coreSession.createDocumentModel("/", "testWSP", "Folder");
        testDocsFolder.setPropertyValue("dc:title", "testWSP");
        testDocsFolder = coreSession.createDocument(testDocsFolder);
        testDocsFolder = coreSession.saveDocument(testDocsFolder);
        coreSession.save();
    }

    @After
    public void cleanup() {
        coreSession.removeDocument(testDocsFolder.getRef());
        coreSession.save();
    }

    @Test
    public void testTypeIsWebsiteFolder() throws IOException {
        // Testing with a zip
        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, testDocsFolder, "File",
                "WSP-html-just-index.zip");
        assertNotNull(doc);

        // Save for good
        coreSession.save();
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        eventService.waitForAsyncCompletion();

        // Get the main HTML page
        try (CloseableHttpResponse response = httpClient.buildGetRequest(doc.getId() + "/index.html").execute()) {
            assertEquals(200, response.getStatus());
            assertEquals("text/html", response.getType());
            // See src/test/resources, the content of the zip
            String resultHtml = response.getEntityString();
            assertNotNull(resultHtml);
            assertTrue(resultHtml.indexOf("<title>Website Preview Test</title>") > 0);
        }

        // Get the logo
        try (CloseableHttpResponse response = httpClient.buildGetRequest(doc.getId() + "/" + PATH_TO_LOGO).execute()) {
            assertEquals(200, response.getStatus());
            assertEquals("image/png", response.getType());
        }
    }

}
