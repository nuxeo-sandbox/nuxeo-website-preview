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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;

/**
 * @since 7.3
 */
@Path("/WSP")
@Produces("text/html;charset=UTF-8")
@WebObject(type = "WebsitePreviewWE")
public class WebsitePreviewWE extends ModuleRoot {

    public static final String PREFIX_PATH = "/WSP";
    public static final String MAIN_URL_SUFFIX = "/index.html";
    @SuppressWarnings("unused")
    protected static final Logger log = LogManager.getLogger(WebsitePreviewWE.class);
    protected static String BASE_URL = null;

    // Main call. This is the ID of the Folderish containing the html files, not the id of a subfile
    @Path("{docId}")
    public Object getMainHtml(@PathParam("docId") String docId) {

        return ctx.newObject("Website", docId);
    }
}
