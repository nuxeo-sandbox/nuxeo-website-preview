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
package org.nuxeo.website.preview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;
import org.nuxeo.runtime.transaction.TransactionHelper;

@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class })
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-website-preview-core" })
public class TestWebsitePreviewWebEngine extends BaseTest {

    public static final String BASE_URL = "http://localhost";

    public static final String LOGO_FILE_NAME = "NUXEO-LOGO-1.png";

    public static final String PATH_TO_LOGO = "img/" + LOGO_FILE_NAME;

    protected DocumentModel testDocsFolder;

    @Inject
    protected ServletContainerFeature servletContainerFeature;

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
        DocumentModel doc = TestUtils.createDocumentFromFile(session, testDocsFolder, "File",
                "WSP-html-just-index.zip");
        assertNotNull(doc);

        // Save for good
        coreSession.save();
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        eventService.waitForAsyncCompletion();

        // Get the main HTML page
        int port = servletContainerFeature.getPort();
        String url = BASE_URL + ":" + port + "/WSP/" + doc.getId() + "/index.html";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            // request.setHeader(HttpHeaders.CONTENT_TYPE, "text/html" /*"application/json"*/);
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y");
            try (CloseableHttpResponse response = client.execute(request)) {

                assertEquals(200, response.getStatusLine().getStatusCode());

                HttpEntity entity = response.getEntity();
                assertEquals("text/html", entity.getContentType().getValue());
                // See src/test/resources, the content of the zip
                String resultHtml = entity != null ? EntityUtils.toString(entity) : null;
                assertNotNull(resultHtml);
                assertTrue(resultHtml.indexOf("<title>Website Preview Test</title>") > 0);
            }
        }

        // Get the logo
        url = BASE_URL + ":" + port + "/WSP/" + doc.getId() + "/" + PATH_TO_LOGO;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            // request.setHeader(HttpHeaders.CONTENT_TYPE, "text/html" /*"application/json"*/);
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y");
            try (CloseableHttpResponse response = client.execute(request)) {

                assertEquals(200, response.getStatusLine().getStatusCode());

                HttpEntity entity = response.getEntity();
                assertEquals("image/png", entity.getContentType().getValue());
            }
        }

    }

}
