/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.website.preview;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

/**
 * Assume there is one single html. If more than one, looks for index.html, or return any of them
 * 
 * @since 7.3
 */
@Name("websitePreview")
@Scope(ScopeType.EVENT)
public class WebsitePreviewHelperBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(WebsitePreviewHelperBean.class);

    protected String mainUrl = null;

    protected boolean hasMiniSite = false;

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @Create
    public void initialize() throws ClientException {

        try {
            setup();
        } catch (Exception e) {
            log.error("Error initializing the bean", e);
        }

    }

    protected void setup() {

        if (mainUrl == null) {
            DocumentModel currentDoc = navigationContext.getCurrentDocument();
            hasMiniSite = WebsitePreviewUtils.getMainHtmlDocument(documentManager, currentDoc) != null;
            if (hasMiniSite) {
                mainUrl = WebsitePreviewWE.buildMainUrl(currentDoc.getId());
            } else {
                mainUrl = "";
            }
        }

    }

    public boolean hasMiniSite() {

        return this.hasMiniSite;
    }

    public String getMainUrl() {

        return mainUrl;

    }

}
