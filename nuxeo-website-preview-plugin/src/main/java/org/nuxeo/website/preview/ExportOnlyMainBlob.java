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

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.io.fsexporter.DefaultExporterPlugin;

/**
 * Exports only file:content and overrides existing files, if any. Depends on nuxeo-fsexporter
 * 
 * @since 7.3
 */
public class ExportOnlyMainBlob extends DefaultExporterPlugin {

    public static final String DEFAULT_BLOB_XPATH = "file:content";

    private static final Log log = LogFactory.getLog(ExportOnlyMainBlob.class);
    
    /*
     if (MAIN_FOLDER == null) {
            File nuxeoWar = FileUtils.getResourceFileFromContext("nuxeo.war");
            MAIN_FOLDER = new File(nuxeoWar, MAIN_FOLDER_NAME);
            if (!MAIN_FOLDER.exists()) {
                MAIN_FOLDER.mkdir();
            }
        }
     */

    public static boolean done = false;
    @Override
    public File serialize(CoreSession session, DocumentModel docfrom, String fsPath) throws Exception {
        
        File folder = null;
        File newFolder = null;
        folder = new File(fsPath);

        // if target directory doesn't exist, create it
        if (!folder.exists()) {
            folder.mkdir();
        }

        if (docfrom.hasFacet("Folderish")) {
            newFolder = new File(fsPath + "/" + docfrom.getName());
            newFolder.mkdir();
        }

        Blob blob = null;
        try {
            blob = (Blob) docfrom.getPropertyValue(DEFAULT_BLOB_XPATH);
        } catch (Exception e) {
            // Ignore, we just have no blob here. Maybe we are handling a Folder with no "file" schema, it is not an
            // error
            blob = null;
        }

        if (blob != null) {
            String fileName = blob.getFilename();
            File target = new File(folder, fileName);
            if (target.exists()) {
                target.delete();
            }
            blob.transferTo(target);
        }

        if (newFolder != null) {
            folder = newFolder;
        }
        return folder;
    }
}
