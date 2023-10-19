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

import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;

/**
 * @since 7.3
 */
public class WebsitePreviewUtils {

    @SuppressWarnings("unused")
    protected static final Logger log = LogManager.getLogger(WebsitePreviewUtils.class);

    // Caching, to avoid doing too many NXQL.
    protected static LinkedHashMap<String, DocumentModel> parentIdAndMainHtml = new LinkedHashMap<>();

    /**
     * Looks for the "main" html file in the input document. If it is a folder, looks at first level, if it is a not,
     * looks inside the blob, expected to be a zip (or unzippable) blob, and looks at first level.
     * <p>
     *
     * @param session
     * @param doc
     * @return
     * @throws DocumentNotFoundException
     * @since 9.10
     */
    public static Blob getMainHtmlBlob(CoreSession session, DocumentModel doc) throws DocumentNotFoundException {

        Blob mainBlob = null;

        if (doc.isFolder()) {
            WebsitePreviewFolder wspFolder = new WebsitePreviewFolder(session, doc);
            mainBlob = wspFolder.getMainHtmlBlob();
        }

        if (mainBlob == null && doc.hasSchema("file")) {
            WebsitePreviewZip wspZip = new WebsitePreviewZip(session, doc);
            mainBlob = wspZip.getMainHtmlBlob();
        }

        return mainBlob;
    }

    /**
     * Finds the document inside the mainDoc based on the relative path, returns the blob.
     * <p>
     * In DocumentModel base implementaiton, DocumentNotFoundException should be raised only if the Nuxeo Document is
     * not found, not if does not have a valid blob (in this case, return false).
     *
     * @param session
     * @param mainDoc
     * @return the blob of the corresponding document
     * @throws DocumentNotFoundException
     * @since 9.10
     */
    public static Blob getResource(CoreSession session, DocumentModel mainDoc, String relativePath)
            throws DocumentNotFoundException {

        Blob blob = null;

        if (mainDoc.isFolder()) {
            WebsitePreviewFolder wspFolder = new WebsitePreviewFolder(session, mainDoc);
            blob = wspFolder.getResource(relativePath);
        }

        if (blob == null && mainDoc.hasSchema("file")) {
            WebsitePreviewZip wspZip = new WebsitePreviewZip(session, mainDoc);
            blob = wspZip.getResource(relativePath);
        }

        return blob;
    }

    /**
     * Checks if the can be previewed as a miniwebsite
     *
     * @param session
     * @param doc
     * @return
     * @since 9.10
     */
    public static boolean hasMiniSite(CoreSession session, DocumentModel doc) {
        return doc.hasFacet(WebsitePreview.FACET) || WebsitePreviewUtils.getMainHtmlBlob(session, doc) != null;
    }

    /**
     * the type of the container, folder or zip. WARNING: This is done by checking misc. properties AND getting the
     * "main html file". Not always optimized.
     *
     * @param session
     * @param doc
     * @return the type of the container, folder or zip
     * @since 9.10
     */
    public static TYPE getType(CoreSession session, DocumentModel doc) {

        Blob mainBlob = null;

        if (doc.isFolder()) {
            WebsitePreviewFolder wspFolder = new WebsitePreviewFolder(session, doc);
            mainBlob = wspFolder.getMainHtmlBlob();
            if (mainBlob != null) {
                return TYPE.FOLDER;
            }
        }

        if (doc.hasSchema("file")) {
            WebsitePreviewZip wspZip = new WebsitePreviewZip(session, doc);
            mainBlob = wspZip.getMainHtmlBlob();
            if (mainBlob != null) {
                return TYPE.ZIP;
            }
        }

        return TYPE.NOT_AND_EMBEDDED_WEBSITE;
    }

    public enum TYPE {
        ZIP, FOLDER, NOT_AND_EMBEDDED_WEBSITE
    }

}
