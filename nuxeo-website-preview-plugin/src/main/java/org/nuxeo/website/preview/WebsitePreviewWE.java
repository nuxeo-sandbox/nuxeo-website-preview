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

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;

/**
 * @since 7.3
 */
@Path("/WSP")
@Produces("text/html;charset=UTF-8")
@WebObject(type = "WebsitePreviewWE")
public class WebsitePreviewWE extends ModuleRoot {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(WebsitePreviewWE.class);

    public static final String PREFIX_PATH = "/WSP";

    public static final String MAIN_URL_SUFFIX = "/index.html";
    
    protected static String BASE_URL = null;

    // Main call. This is the ID of the Folderish containing the html files, not the id of a subfile
    @Path("{docId}")
    public Object getMainHtml(@PathParam("docId") String docId) {

        return ctx.newObject("Website", docId);
    }

    /**
     * Returns the full URL to use to display the website. <code>mainParentId</code> is, well, the ID of the main
     * folder, the one containing the main html file.
     * 
     * @param docId
     * @return
     * @since 7.3
     */
    public static String buildMainUrl(String mainParentId) {
        
        if(BASE_URL == null) {
            BASE_URL = org.nuxeo.ecm.platform.ui.web.util.BaseURL.getBaseURL();;
        }
        
        return BASE_URL // CONTAINS THE FINAL "/"
                + "site" + PREFIX_PATH + "/" + mainParentId + MAIN_URL_SUFFIX;

    }

}
