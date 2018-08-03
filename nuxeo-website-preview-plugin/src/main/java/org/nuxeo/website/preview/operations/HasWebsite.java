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
package org.nuxeo.website.preview.operations;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.website.preview.WebsitePreviewUtils;

/**
 *
 * @since 9.10
 */
@Operation(id = HasWebsite.ID, category = Constants.CAT_DOCUMENT, label = "Has Website", description = "Checks if the input can be previewed as a mini-website. Currently, this means it is a Folderish with an html file at first level. Returns the input unchanged. Sets the WSP_hasWebsite context variable (boolean) to true or false")
public class HasWebsite {

    public static final String ID = "Document.HasWebsite";

    public static final String CONTEXT_VAR_NAME = "WSP_hasWebsite";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @OperationMethod
    public DocumentModel run(DocumentModel input) {

        boolean hasMiniSite = WebsitePreviewUtils.hasMiniSite(session, input);

        ctx.put(CONTEXT_VAR_NAME, hasMiniSite);

        return input;
    }

}
