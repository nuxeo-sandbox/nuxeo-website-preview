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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;

/**
 *
 * @since 7.3
 */
@WebObject(type = "Website")
@Produces("text/html;charset=UTF-8")
public class WebsiteObject extends DefaultObject {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(WebsiteObject.class);

    // The main fodler, containing the files
    DocumentModel mainFolder = null;

    @Override
    protected void initialize(Object... args) {
        super.initialize(args);

        String docId = (String) args[0];
        try {
            mainFolder = ctx.getCoreSession().getDocument(new IdRef(docId));
        } catch (Exception e) {
            mainFolder = null;
        }
    }

    @Path("/index.html")
    @GET
    @Produces("text/html")
    public Response getMainHtml() {

        Response r = null;

        if (mainFolder != null) {
            try {
                DocumentModel doc = WebsitePreviewUtils.getMainHtmlDocument(ctx.getCoreSession(), mainFolder);
                if (doc == null) {
                    r = Response.status(Status.NOT_FOUND).build();
                } else {
                    try {
                        if(doc.hasSchema("file")) {
                            Blob b = (Blob) doc.getPropertyValue("file:content");
                            ResponseBuilder resp = Response.ok(b.getFile());
                            r = resp.build();
                        } else {
                            r = Response.status(Status.NOT_FOUND).build();
                        }

                    } catch (Exception e) {
                        r = Response.status(Status.NOT_FOUND).build();
                    }
                }

            } catch (Exception e) {
                // Should be more granular...
                r = Response.status(Status.NOT_FOUND).build();
            }
        }

        if (r == null) {
            r = Response.status(Status.NOT_FOUND).build();
        }

        return r;

    }

    /*
     * Here, we receive "img01.jpg", or "/img/img01.jpg", etc.
     */
    /*
     * WARNING WARNING: We assume the path received is the same as the path in nuxeo. We can't handle file renamed etc.
     */
    @Path("{thePath:.*}")
    @GET
    public Response getResource(@PathParam("thePath") String thePath) {

        Response r = null;
        if (mainFolder != null) {
            String path = mainFolder.getPathAsString() + "/" + thePath;

            DocumentModel doc = null;
            try {
                doc = ctx.getCoreSession().getDocument(new PathRef(path));

                Blob b = (Blob) doc.getPropertyValue("file:content");
                ResponseBuilder resp = Response.ok(b.getFile());

                resp.type(b.getMimeType());
                r = resp.build();

            } catch (Exception e) {
                // This is where we try to rebuild a nuxeo path?
            }

        }

        if (r == null) {
            r = Response.status(Status.NOT_FOUND).build();
        }

        return r;

    }

}
