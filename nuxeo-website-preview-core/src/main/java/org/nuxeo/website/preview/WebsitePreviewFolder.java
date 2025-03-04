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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 9.10
 */
public class WebsitePreviewFolder implements WebsitePreview {

    protected static final Logger log = LogManager.getLogger(WebsitePreviewFolder.class);

    public static int MAX_ELEMENTS_IN_CACHE = 500;

    // When cache is full, we remove 20% by default
    public static double CACHE_CLEAN_RATIO = 0.2;

    // Caching, to avoid doing too many NXQL.
    protected static LinkedHashMap<String, DocumentModel> parentIdAndMainHtml = new LinkedHashMap<>();
    
    // But caching stays optional
    // WARNING: cach is static => not using it for one WebsitePreviewFolder => not using it for all
    protected static boolean useCache = true;

    protected CoreSession session;

    protected DocumentModel mainParent;

    public WebsitePreviewFolder(CoreSession session, DocumentModel mainDoc) {
        this.session = session;
        mainParent = mainDoc;
    }
    
    public static void useCache(boolean value) {
        if(useCache != value) {
            useCache = value;
            
            if(!useCache) {
                synchronized (parentIdAndMainHtml) {
                    parentIdAndMainHtml = new LinkedHashMap<>();
                }
            }
        }
    }
    
    public static boolean getUseCache() {
        return useCache;
    }

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
     * <li>If there are several and none is named "index.html", returns the "first" one.</li>
     * </ul>
     * <b>WARNING</b> "Not found" could be actually:
     * <ul>
     * <li>Really not found</li>
     * <li><code>id</code> was found but it not <i>Folderish</i></li>
     * <li>Current user has not enough right to read (either read the parent or the child)</li>
     * </ul>
     *
     * @return
     * @since 9.10
     */
    @Override
    public Blob getMainHtmlBlob(String customDocId) throws DocumentNotFoundException {

        DocumentModel mainHtmlDoc = null;

        // Don't loose time processing
        if (mainParent == null || !mainParent.hasFacet("Folderish")) {
            return null;
        }

        // Cleanup cache if needed
        synchronized (parentIdAndMainHtml) {
            if (parentIdAndMainHtml.size() > MAX_ELEMENTS_IN_CACHE) {
                int howMany = (int) (MAX_ELEMENTS_IN_CACHE * CACHE_CLEAN_RATIO);
                String[] keys = new String[howMany];
                Iterator<Map.Entry<String, DocumentModel>> it = parentIdAndMainHtml.entrySet().iterator();
                for (int i = 0; i < howMany; ++i) {
                    keys[i] = it.next().getKey();
                }
                for (String oneKey : keys) {
                    parentIdAndMainHtml.remove(oneKey);
                }
            }
        }

        // Get the DocumentModel
        mainHtmlDoc = parentIdAndMainHtml.get(mainParent.getId());

        if (mainHtmlDoc != null) {
            // We need to reload the document. Cannot use mainHtml.refresh(), because
            // the CoreSession may have change (has most likely changed) since "last" call
            // and we will get an error.
            mainHtmlDoc = session.getDocument(mainHtmlDoc.getRef());
        } else {
            // Must protect writing in shared array, to save cpu when getting the same html from different threads
            synchronized (parentIdAndMainHtml) {
                // Another thread could have done the exact same thing right when setting the MutEx => we must check
                // again
                mainHtmlDoc = parentIdAndMainHtml.get(mainParent.getId());
                if (mainHtmlDoc == null) {
                    
                    if(StringUtils.isBlank(customDocId)) {
                    
                        // Must find a .html file in first children
                        String nxql = "SELECT * FROM Document WHERE ecm:parentId = '" + mainParent.getId() + "'"
                                + " AND (content/mime-type ILIKE '%html' OR content/name ILIKE '%html' OR content/name ILIKE '%htm'"
                                + " OR note:mime_type = 'text/html')"
                                + " AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isVersion = 0 AND ecm:isTrashed = 0";
                        DocumentModelList children = session.query(nxql);
    
                        if (children.size() == 1) {
                            mainHtmlDoc = children.get(0);
                        } else {
                            String fileName;
                            Blob mainBlob;
                            for (DocumentModel child : children) {
    
                                mainHtmlDoc = child;
                                /*
                                 * Because of the NXQL we did, we know we have:
                                 * - An html blob
                                 * - Or an html note. In this case, the document title must be index.html
                                 */
                                if (child.hasSchema("file")) {
                                    mainBlob = (Blob) child.getPropertyValue("file:content");
                                    fileName = mainBlob.getFilename();
                                } else if (child.hasSchema("note")) {
                                    fileName = mainHtmlDoc.getTitle();
                                } else {
                                    fileName = "";
                                }
                                if (fileName.toLowerCase().indexOf("index.html") == 0) {
                                    break;
                                }
                            }
                        }
                    } else {
                        mainHtmlDoc = session.getDocument(new IdRef(customDocId));
                    }
                } else {
                    // We need to reload the document. Cannot use mainHtml.refresh(), because
                    // the CoreSession may have changed (has most likely changed) since "last" call
                    // and we will get an error.
                    mainHtmlDoc = session.getDocument(mainHtmlDoc.getRef());
                }
                if (mainHtmlDoc != null) {
                    if(useCache) {
                        parentIdAndMainHtml.put(mainParent.getId(), mainHtmlDoc);
                    }
                }
            }
        }

        if (mainHtmlDoc != null) {
            return getBlob(mainHtmlDoc);
        }

        return null;
    }

    /*
     * Returns the "file:content" if any. If the doc is a NOte, returns a temp. blob.
     * <br>
     * If doc is a Note, the filename of the blob is set to the dc:title of the Note.
     */
    protected Blob getBlob(DocumentModel doc) {

        if (doc.hasSchema("file")) {
            return (Blob) doc.getPropertyValue("file:content");
        }

        if (doc.hasSchema("note")) {
            String content = (String) doc.getPropertyValue("note:note");
            String mimeType = (String) doc.getPropertyValue("note:mime_type");

            String fileName = doc.getTitle();
            String baseName = FilenameUtils.getBaseName(fileName);
            String ext = FilenameUtils.getExtension(fileName);
            try {
                File tempFile = Framework.createTempFile(baseName, "." + ext);

                org.apache.commons.io.FileUtils.writeStringToFile(tempFile, content, StandardCharsets.UTF_8.name());
                Blob tempBlob = Blobs.createBlob(tempFile, mimeType);
                tempBlob.setFilename(fileName);
                
                return tempBlob;
                
            } catch (IOException e) {
                throw new NuxeoException(e);
            }
        }

        return null;
    }

    /**
     * Finds the document inside the mainfolder based on the relative path, returns the blob
     *
     * @param relativePath
     * @return the blob of the corresponding document
     * @throws DocumentNotFoundException
     * @since 9.10
     */
    @Override
    public Blob getResource(String relativePath) throws DocumentNotFoundException {

        Blob blob = null;

        String path = mainParent.getPathAsString() + "/" + relativePath;

        DocumentModel doc = null;
        try {
            doc = session.getDocument(new PathRef(path));
        } catch (DocumentNotFoundException e) {
            // Workaround in case the path has been truncated/recalculated by Nuxeo because too long
            int lastIndex = path.lastIndexOf("/");
            String parentPath = path.substring(0, lastIndex);
            String docTitle = path.substring(lastIndex + 1);
            // Get the parent (if error, we give up)
            DocumentModel parent = session.getDocument(new PathRef(parentPath));
            String nxql = "SELECT * FROM Document WHERE ecm:parentId = '" + parent.getId() + "'"
                    + " AND dc:title = '" + docTitle + "'"
                    + " AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isVersion = 0 AND ecm:isTrashed = 0";
            DocumentModelList children = session.query(nxql);
            if(children.size() > 0) {
                log.info("Not found at " + path + ", but found by title at " + parentPath);
                doc = children.get(0);
            }
            if(doc == null) {
                log.warn("No document found at " + path + ", nor at " + parentPath + " with a title '" + docTitle + "'.");
                return null;
            }
        }

        blob = getBlob(doc);
        if (blob == null) {
            log.warn("Document " + path + " has no blob");
        } else {
            String fileName = blob.getFilename();
            String mimeType = blob.getMimeType();

            // Assume there is a file name. If null, we'll fail miserably with a NPE
            // Pb in some browsers. Returning text/plain as mimetype instead of text/css makes
            // the browser to ignore the file, or even log a 404. It is mainly when there is a
            // <link> that explicitely asks for text/css and we return text/plain
            // 2019-06-13: We do have issues with mimetype. a .js file is returned as text/plain
            // => adding for css and for js
            String ext = FilenameUtils.getExtension(fileName);
            if ("css".equalsIgnoreCase(ext) && !"text/css".equalsIgnoreCase(mimeType)) {
                log.warn("Adjusting mimeType for css for " + path);
                blob.setMimeType("text/css");
            }
            if ("js".equalsIgnoreCase(ext) && !"application/javascript".equalsIgnoreCase(mimeType)) {
                log.warn("Adjusting mimeType for js for " + path);
                blob.setMimeType("application/javascript");
            }
        }

        return blob;

    }
}
