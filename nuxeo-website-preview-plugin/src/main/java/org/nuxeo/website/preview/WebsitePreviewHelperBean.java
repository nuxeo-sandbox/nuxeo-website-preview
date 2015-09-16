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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.io.fsexporter.FSExporterService;

/**
 * Assume there is one single html. If more than one, loks for index.html
 * 
 * @since 7.3
 */
@Name("websitePreview")
@Scope(ScopeType.EVENT)
public class WebsitePreviewHelperBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(WebsitePreviewHelperBean.class);

    protected static boolean alreadyReportFSENotHere = false;

    public static final String MAIN_FOLDER_NAME = "WebsitePreviewTemp";

    protected static String MAIN_FOLDER_PATH = null;

    protected static String BASE_URL = null;

    protected DocumentModel currentDocument = null;

    protected String mainHtmlFilePath = null;

    protected ArrayList<String> htmlFiles = new ArrayList<String>();

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true)
    protected FSExporterService fsExporterService;

    @Create
    public void initialize() throws ClientException {

        try {
            setup();
        } catch (Exception e) {
            log.error("Error initializing the bean", e);
        }

    }

    protected void setup() {

        if (fsExporterService == null) {
            if (!alreadyReportFSENotHere) {
                log.error("The FSExporterService is not available. Did you instal nuxeo-fsexporter?");
                alreadyReportFSENotHere = true;
            }
            return;
        }

        if (BASE_URL == null) {
            BASE_URL = org.nuxeo.ecm.platform.ui.web.util.BaseURL.getBaseURL();
        }

        if (MAIN_FOLDER_PATH == null) {
            File nuxeoWar = FileUtils.getResourceFileFromContext("nuxeo.war");
            File mainFolder = new File(nuxeoWar, MAIN_FOLDER_NAME);
            if (!mainFolder.exists()) {
                mainFolder.mkdir();
            }

            MAIN_FOLDER_PATH = mainFolder.getAbsolutePath();
        }

        currentDocument = navigationContext.getCurrentDocument();

        if (mainHtmlFilePath == null) {
            if (currentDocument.hasFacet("Folderish")) {
                // Must find a .html file in first children
                DocumentModelList children = documentManager.getChildren(currentDocument.getRef());
                String fileName;
                Blob b;
                for (DocumentModel child : children) {
                    if (isAcceptableDocument(child)) {
                        try {
                            b = (Blob) child.getPropertyValue("file:content");
                            if (b != null) {
                                fileName = b.getFilename().toLowerCase();
                                if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                                    // We can't use the lowercase version for the final url, it is case sensitive
                                    htmlFiles.add(b.getFilename());
                                }
                            }
                        } catch (Exception e) {
                            // No "file:content" is not an error
                        }
                    }
                }
            }
        }

    }

    protected boolean isAcceptableDocument(DocumentModel doc) {
        return doc != null && !doc.hasFacet("Folderish") && !doc.hasFacet("HiddenInNavigation")
                && doc.getCurrentLifeCycleState() != "deleted" && !doc.isVersion() && !doc.isProxy();
    }

    public boolean hasMiniSite() {

        return htmlFiles.size() > 0;
    }

    protected String getMainHtmlFile() {

        String result = "";

        switch (htmlFiles.size()) {
        case 0:
            break;

        case 1:
            result = htmlFiles.get(0);
            break;

        default:
            for (String oneName : htmlFiles) {
                if (oneName.toLowerCase().indexOf("index.htm") == 0) {
                    result = oneName;
                    break;
                }
            }
            if (result.isEmpty()) {
                // Give up, use the first one...
                result = htmlFiles.get(0);
            }
            break;
        }

        return result;
    }

    public String getMainHtmlFilePath() throws IOException, Exception {

        if (hasMiniSite() && StringUtils.isBlank(mainHtmlFilePath)) {

            fsExporterService.export(documentManager, currentDocument.getPathAsString(), MAIN_FOLDER_PATH, null);

            mainHtmlFilePath = BASE_URL; // CONTAINS THE FINAL "/"
            mainHtmlFilePath += MAIN_FOLDER_NAME + "/" + currentDocument.getName();
            mainHtmlFilePath += "/" + getMainHtmlFile();
        }

        return mainHtmlFilePath;

    }

}
