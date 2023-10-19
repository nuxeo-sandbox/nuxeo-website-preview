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
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-website-preview-core" })
public class TestWebsitePreviewZip {

    public static final String LOGO_FILE_NAME = "NUXEO-LOGO-1.png";

    public static final String PATH_TO_LOGO = "img/" + LOGO_FILE_NAME;

    protected DocumentModel testDocsFolder;

    @Inject
    protected CoreSession coreSession;

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
    public void testTypeIsWebsiteFolder() {

        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, testDocsFolder, "File",
                "WSP-html-just-index.zip");
        assertNotNull(doc);

        assertEquals(WebsitePreviewUtils.TYPE.ZIP, WebsitePreviewUtils.getType(coreSession, doc));
    }

    @Test
    public void testWithSingleHtmlIndex() {

        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, testDocsFolder, "File",
                "WSP-html-just-index.zip");
        assertNotNull(doc);

        Blob result = WebsitePreviewUtils.getMainHtmlBlob(coreSession, doc);
        assertNotNull(result);
        assertNotNull(result.getFilename());
        assertEquals("index.html", result.getFilename().toLowerCase());
    }

    @Test
    public void testWithHtmlButNoIndexFile() {

        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, testDocsFolder, "File",
                "WSP-html-no-index-file.zip");
        assertNotNull(doc);

        Blob result = WebsitePreviewUtils.getMainHtmlBlob(coreSession, doc);
        assertNotNull(result);

    }

    @Test
    public void testWithSeveralHtmlAndIndexFile() {

        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, testDocsFolder, "File",
                "WSP-html-several-and-index.zip");
        assertNotNull(doc);

        Blob result = WebsitePreviewUtils.getMainHtmlBlob(coreSession, doc);
        assertNotNull(result);
        assertNotNull(result.getFilename());
        assertEquals("index.html", result.getFilename().toLowerCase());

    }

    @Test
    public void testWithNoHtml() {

        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, testDocsFolder, "File", "WSP-no-html.zip");
        assertNotNull(doc);

        Blob result = WebsitePreviewUtils.getMainHtmlBlob(coreSession, doc);
        assertNull(result);

    }

    @Test
    public void testgetResource() {

        DocumentModel doc = TestUtils.createDocumentFromFile(coreSession, testDocsFolder, "File",
                "WSP-html-several-and-index.zip");
        assertNotNull(doc);

        Blob result = WebsitePreviewUtils.getResource(coreSession, doc, PATH_TO_LOGO);
        assertNotNull(result);
        assertNotNull(result.getFilename());
        assertEquals(LOGO_FILE_NAME, result.getFilename());

    }
}
