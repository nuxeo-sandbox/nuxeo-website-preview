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
import org.nuxeo.ecm.core.api.DocumentNotFoundException;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;

/**
 * @since 7.3
 */
@WebObject(type = "Website")
@Produces("text/html;charset=UTF-8")
public class WebsiteObject extends DefaultObject {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(WebsiteObject.class);

    // The main fodler, containing the files
    DocumentModel mainDocument = null;

    @Override
    protected void initialize(Object... args) {
        super.initialize(args);

        // URL is .../WSP/1234-5678-90ab/index.html => first argument is the doc id
        String docId = (String) args[0];
        try {
            mainDocument = ctx.getCoreSession().getDocument(new IdRef(docId));
        } catch (Exception e) {
            mainDocument = null;
        }
    }

    @Path("/index.html")
    @GET
    @Produces("text/html")
    public Response getMainHtml() {

        Response response = null;

        if (mainDocument != null) {
            try {

                Blob blob = WebsitePreviewUtils.getMainHtmlBlob(ctx.getCoreSession(), mainDocument);
                if (blob == null) {
                    response = Response.status(Status.NOT_FOUND).build();
                } else {
                    ResponseBuilder builder = Response.ok(blob.getFile());
                    response = builder.build();
                }

            } catch (DocumentNotFoundException e) {
                log.error("mainDocument not found", e);
            }
        }

        if (response == null) {
            response = Response.status(Status.NOT_FOUND).build();
        }

        return response;

    }

    /*
     * Here, we receive "img01.jpg", or "/img/img01.jpg", etc.
     */
    /*
     * WARNING WARNING: We assume the path received is the same as the path in nuxeo or in the zip. We can't handle file
     * renamed etc.
     */
    @Path("{thePath:.*}")
    @GET
    public Response getResource(@PathParam("thePath") String thePath) {

        Response response = null;

        try {

            Blob blob = WebsitePreviewUtils.getResource(ctx.getCoreSession(), mainDocument, thePath);
            if (blob != null) {
                ResponseBuilder builder = Response.ok(blob.getFile());
                builder.type(blob.getMimeType());
                response = builder.build();
            }

        } catch (DocumentNotFoundException e) {
            log.error("Document " + path + " not found", e);
        }

        if (response == null) {
            response = Response.status(Status.NOT_FOUND).build();
        }

        return response;

    }

}
