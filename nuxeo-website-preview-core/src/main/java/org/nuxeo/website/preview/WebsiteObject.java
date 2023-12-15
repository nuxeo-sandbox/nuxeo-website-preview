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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    protected static final Logger log = LogManager.getLogger(WebsiteObject.class);

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
        } else {
            
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
