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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;

import jakarta.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.nuxeo.website.preview.operations.HasWebsite;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-website-preview-core" })
public class TestOperations {

    protected DocumentModel testDocsFolder;

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldDetectEmbeddedWebsiteInZip() throws Exception {
        
        DocumentModel doc = session.createDocumentModel("/", "test", "File");
        doc.setPropertyValue("dc:title", "test");
        File f = FileUtils.getResourceFileFromContext("WSP-html-several-and-index.zip");
        Blob b = new FileBlob(f);
        b.setMimeType("application/zip");
        doc.setPropertyValue("file:content", (Serializable) b);
        doc = session.createDocument(doc);
        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        doc = (DocumentModel) automationService.run(ctx, HasWebsite.ID);
        
        boolean hasWebsite = (boolean) ctx.get(HasWebsite.CONTEXT_VAR_NAME);
        assertTrue(hasWebsite);
        
    }
    


    @Test
    public void shouldNotDetectEmbeddedWebsiteInZip() throws Exception {
        
        DocumentModel doc = session.createDocumentModel("/", "test", "File");
        doc.setPropertyValue("dc:title", "test");
        File f = FileUtils.getResourceFileFromContext("WSP-no-html.zip");
        Blob b = new FileBlob(f);
        b.setMimeType("application/zip");
        doc.setPropertyValue("file:content", (Serializable) b);
        doc = session.createDocument(doc);
        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        doc = (DocumentModel) automationService.run(ctx, HasWebsite.ID);
        
        boolean hasWebsite = (boolean) ctx.get(HasWebsite.CONTEXT_VAR_NAME);
        assertFalse(hasWebsite);
        
    }
}
