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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
    "nuxeo-website-preview-core"
})
public class TestWebsitePreviewFolder {

    public static final String LOGO_FILE_NAME = "NUXEO-LOGO-1.png";

    public static final String PATH_TO_LOGO = "img/" + LOGO_FILE_NAME;

    protected DocumentModel testDocsFolder;

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

    protected DocumentModel buildWebsiteDocuments() {

        // Create main parent
        DocumentModel mainFolder = TestUtils.createFolder(coreSession, testDocsFolder, "Website");
        // Crete the html files
        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, mainFolder, "File", "wsp-folder/index.html");
        doc = TestUtils.createDocumentFromFile(coreSession, mainFolder, "File", "wsp-folder/test1.html");
        // Now the img folder and its logo
        DocumentModel imgFolder = TestUtils.createFolder(coreSession, mainFolder, "img");
        doc = TestUtils.createDocumentFromFile(coreSession, imgFolder, "File", "wsp-folder/img/NUXEO-LOGO-1.png");

        // Save for good
        coreSession.save();
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        eventService.waitForAsyncCompletion();

        return mainFolder;
    }

    @Test
    public void testTypeIsWebsiteFolder() {

        DocumentModel website = buildWebsiteDocuments();
        assertNotNull(website);

        assertEquals(WebsitePreviewUtils.TYPE.FOLDER, WebsitePreviewUtils.getType(coreSession, website));
    }

    @Test
    public void testGetHtmlIndex() {

        DocumentModel website = buildWebsiteDocuments();
        assertNotNull(website);

        Blob result = WebsitePreviewUtils.getMainHtmlBlob(coreSession, website);

        assertNotNull(result);
        assertNotNull(result.getFilename());
        assertEquals("index.html", result.getFilename().toLowerCase());
    }

    @Test
    public void testgetResource() {

        DocumentModel website = buildWebsiteDocuments();
        assertNotNull(website);

        Blob result = WebsitePreviewUtils.getResource(coreSession, website, PATH_TO_LOGO);
        assertNotNull(result);
        assertNotNull(result.getFilename());
        assertEquals(LOGO_FILE_NAME, result.getFilename());

    }
}
