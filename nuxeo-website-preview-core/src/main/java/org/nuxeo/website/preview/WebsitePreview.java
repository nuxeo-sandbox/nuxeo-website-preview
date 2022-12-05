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

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;

/**
 * @since 9.10
 */
public interface WebsitePreview {

    public static final String FACET = "WebsitePreviewAvailable";

    /**
     * Returns the main html file corresponding to the mainDoc. If mainDoc itself is not found, raised
     * <code>DocumentNotFoundException</code>. If it is found but the implepentaiton cannot find an html file, it
     * returns null.
     * <p>
     * Each implementaiton should describe how they decide what is the main HTML document. See
     * {@link WebsitePreviewFolder} for an example.
     * <p>
     *
     * @param session
     * @param parent, the root parent
     * @return
     * @since 9.10
     */
    public Blob getMainHtmlBlob() throws DocumentNotFoundException;

    /**
     * Retrieves a blob based on its relative path from the main document, returns null if not found.
     * <p>
     * DocumentNotFoundException is returned if mainDoc does not exist, or, for implementaiton based on DocumentModel,
     * no such document is found at the relative path. For file based implementaitons (like searching in a zip), the
     * method should return null when there is no such entry on disk.
     *
     * @param session
     * @param mainDoc
     * @return the blob of the corresponding document
     * @throws DocumentNotFoundException
     * @since 9.10
     */
    public Blob getResource(String relativePath) throws DocumentNotFoundException;
}
