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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.Environment;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.mimetype.service.MimetypeRegistryService;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 9.10
 */
public class WebsitePreviewZip implements WebsitePreview {

    private static final Log log = LogFactory.getLog(WebsitePreviewZip.class);

    public static final String MAIN_TEMP_FOLDER_NAME = "WebsitePreviewZip";

    public static File mainTempFolderFile = null;

    protected CoreSession session;

    protected DocumentModel mainParent;

    public WebsitePreviewZip(CoreSession session, DocumentModel mainDoc) {
        this.session = session;
        mainParent = mainDoc;
    }

    @Override
    public Blob getMainHtmlBlob() throws DocumentNotFoundException {

        // throw new UnsupportedOperationException();
        Blob mainHtmlBlob = null;
        DocumentModel mainHtmlDoc = null;

        // Don't loose time processing
        if (mainParent == null ) {
            return null;
        }

        // Get the DocumentModel
        mainHtmlDoc = session.getDocument(mainParent.getRef());
        // Assume it has the "file" schema
        Blob blob = (Blob) mainHtmlDoc.getPropertyValue("file:content");
        if (blob != null) {
            ZipFile zipFile = null;
            byte[] buffer = new byte[4096 * 2];
            int len = 0;

            // Try to explore it as a zip, if it's not a zip it will fail
            try {
                File zipBlobFile = blob.getFile();
                zipFile = new ZipFile(zipBlobFile);
                Map<String, ZipEntry> htmlEntries = new HashMap();
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String nameLC = entry.getName().toLowerCase();
                    if (!entry.isDirectory() && nameLC.indexOf("/") < 0) {
                        if (nameLC.endsWith(".html") || nameLC.endsWith(".htm")) {
                            htmlEntries.put(nameLC, entry);
                            if(nameLC.equals("index.html")) {
                                break;
                            }
                        }
                    }
                }
                // Find index.html
                ZipEntry entry = htmlEntries.get("index.html");
                if(entry == null && htmlEntries.size() > 0) {
                    // Get any file, first one will do it
                    entry = htmlEntries.entrySet().iterator().next().getValue();
                }
                if(entry != null) {
                    mainHtmlBlob = Blobs.createBlobWithExtension(".html");
                    FileOutputStream fos = new FileOutputStream(mainHtmlBlob.getFile());
                    InputStream zipEntryStream = zipFile.getInputStream(entry);
                    while ((len = zipEntryStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    mainHtmlBlob.setFilename(entry.getName());
                }


            } catch (java.io.IOException e) {
                log.error("Error parsing the file, expecting a zip file", e);
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        return mainHtmlBlob;
    }

    @Override
    public Blob getResource(String relativePath) throws DocumentNotFoundException {

        // throw new UnsupportedOperationException();
        // Not optimized. We walk the whole zip everytime we access a ressource, and if the same resource is asked 10
        // times in a minute we reparse the azip 10 times; We should better expand the zip

        Blob resourceBlob = null;
        DocumentModel mainHtmlDoc = null;

        // Don't loose time processing
        if (mainParent == null) {
            return null;
        }

        // Get the DocumentModel
        mainHtmlDoc = session.getDocument(mainParent.getRef());
        // Assume it has the "file" schema
        Blob blob = (Blob) mainHtmlDoc.getPropertyValue("file:content");
        if (blob != null) {
            ZipFile zipFile = null;
            byte[] buffer = new byte[4096];
            int len = 0;
            String fileName = null;
            String fileExtension = null;

            // Try to explore it as a zip, if it's not a zip it will fail
            try {
                File zipBlobFile = blob.getFile();
                zipFile = new ZipFile(zipBlobFile);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if(entry.getName().equals(relativePath)) {
                        fileName = entry.getName();
                        int idx = fileName.lastIndexOf('/');
                        if (idx > -1) {
                            fileName = fileName.substring(idx + 1);
                        }
                        fileExtension = FilenameUtils.getExtension(fileName);
                        resourceBlob = Blobs.createBlobWithExtension("." + fileExtension);

                        FileOutputStream fos = new FileOutputStream(resourceBlob.getFile());
                        InputStream zipEntryStream = zipFile.getInputStream(entry);
                        while ((len = zipEntryStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                        break;
                    }
                }

                // Now handle the mimetype
                // Unfortunately, mimetype for some files is problematic...
                // (getMimeTyeFromBlob() for example returns text/plain for a .js file,
                // getMimeTypeFromFileName throws a mimeTypeNotFound exception for ".js", etc.
                // Let's work aroudn this quickly
                // BUT:
                // TODO: Update mimetype registry...
                resourceBlob.setFilename(fileName);
                
                String mimeType;
                MimetypeRegistryService service = (MimetypeRegistryService) Framework.getService(
                         MimetypeRegistry.class);
                if(fileExtension == null) {
                    mimeType = service.getMimetypeFromBlob(resourceBlob);
                } else {
                    switch (fileExtension.toLowerCase()) {
                    case "js":
                        mimeType = "application/javascript";
                        break;

                      default:
                        mimeType = service.getMimetypeFromFilename(fileName);
                        break;
                    }
                }
                resourceBlob.setMimeType(mimeType);

            } catch (java.io.IOException e) {
                log.error("Error parsing the file, expecting a zip file", e);
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        return resourceBlob;

    }

    protected void cleanupIfNeeded() throws IOException {
        synchronized (MAIN_TEMP_FOLDER_NAME) {
            if (mainTempFolderFile == null) {
                // First call, cleanup previous expanded zip if any
                String tmpDirStr = Environment.getDefault().getTemp().getPath();
                String mainTempFolderPath;
                if (tmpDirStr.endsWith("/")) {
                    mainTempFolderPath = tmpDirStr + MAIN_TEMP_FOLDER_NAME;
                } else {
                    mainTempFolderPath = tmpDirStr + "/" + MAIN_TEMP_FOLDER_NAME;
                }
                mainTempFolderFile = new File(mainTempFolderPath);
                if (mainTempFolderFile.exists()) {
                    FileUtils.deleteDirectory(mainTempFolderFile);
                }
                mainTempFolderFile.mkdir();
            }
            // Delete old directories
        }
    }

}
