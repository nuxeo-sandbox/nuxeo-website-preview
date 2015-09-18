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
