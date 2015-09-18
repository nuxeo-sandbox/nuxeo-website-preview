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
 *     thibaud
 */
package org.nuxeo.website.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * @since TODO
 */
public class WebsitePreviewUtils {

    /**
     * Receives a <i>Folderish</i> document, which contains at least one Document whose file:content field is an html
     * file.
     * <p>
     * Returns null is the corresponding Document is not found.
     * <p>
     * The method assumes:
     * <ul>
     * <li>There is one single HTML file in the folder</li>
     * <li>Or (if ore than one), assumes there is one named "index.html".</li>
     * <li>If there are several and none is named "index.html", returns the first one.</li>
     * </ul>
     * <b>WARNING</b> "Not found" could be actually:
     * <ul>
     * <li>Really not found</li>
     * <li><code>id</code> was found but it not <i>Folderish</i></li>
     * <li>Current user has not enough right to read (either read the parent or the child)</li>
     * </ul>
     * 
     * @param session
     * @param parent, the root parent
     * @return
     * @since 7.3
     */
    public static DocumentModel getMainHtmlDocument(CoreSession session, DocumentModel parent) {

        DocumentModel mainHtml = null;

        if (parent != null && parent.hasFacet("Folderish")) {

            // Must find a .html file in first children
            String nxql = "SELECT * FROM Document WHERE ecm:parentId = '"
                    + parent.getId()
                    + "'"
                    + " AND (content/mime-type ILIKE '%html' OR content/name ILIKE '%html%' OR content/name ILIKE '%hml')"
                    + " AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'";
            DocumentModelList children = session.query(nxql);

            String fileName;
            Blob mainBlob;
            for (DocumentModel child : children) {

                mainHtml = child;
                
                // Because of the nxql we did, we know the document has a blob, and the html mime type, no need to
                // try-catch or check things
                // (we could be hysterical: Maybe in the microseconds, the document has been removed or it's permissions
                // changed... The error will then be thrown and that's all.
                mainBlob = (Blob) child.getPropertyValue("file:content");
                fileName = mainBlob.getFilename();
                if (mainHtml != null && fileName.toLowerCase().indexOf("index.html") == 0) {
                    break;
                }
            }
        }

        return mainHtml;
    }
}
