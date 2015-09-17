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
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.webengine.WebEngine;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;

/**
 * @since TODO
 */
@Path("/WSP")
@Produces("text/html;charset=UTF-8")
@WebObject(type = "WebsitePreviewWE")
public class WebsitePreviewWE extends ModuleRoot {

    private static final Log log = LogFactory.getLog(WebsitePreviewWE.class);

    @Path("{docId}/")
    @GET
    public Response getMainHtml(@PathParam("docId") String docId) {
        
        
        Response r;
        CoreSession session = ctx.getCoreSession();
        
        log.warn(ctx.getUrlPath());

        try {            
            DocumentModel doc = session.getDocument(new IdRef(docId));
            Blob b = (Blob) doc.getPropertyValue("file:content");
            
            ResponseBuilder resp = Response.ok(b.getFile());
            
            resp.type("text/html");
            r = resp.build();
            
        } catch (Exception e) {
            r = Response.status(Status.NOT_FOUND).build();
        }
        
        return r;
    }

    @Path("{docId}/{theRest:.*}")
    @GET
    public Object getMore(@PathParam("docId") String docId, @PathParam("other") String other, @PathParam("theRest") String theRest) {
        
        log.warn(theRest);

        return theRest;
    }
    
    protected DocumentModel getDocForId(String id) {
        
        DocumentModel doc = null;
        
        CoreSession session = WebEngine.getActiveContext().getCoreSession();
        doc = session.getDocument(new IdRef(id));
        
        return doc;
    }
    
    protected DocumentModel getDoc(String mainHtmlDocId, String subPath) {
        
        DocumentModel doc = null;
        
        DocumentModel mainHtml = getDocForId(mainHtmlDocId);
        if(mainHtml != null) {
            if(subPath.charAt(0) != '/') {
                subPath = "/" + subPath;
            }
            String path = mainHtml.getPathAsString() + subPath;
            CoreSession session = WebEngine.getActiveContext().getCoreSession();
            doc = session.getDocument(new PathRef(path));
            
        }
        
        return doc;
    }
    
    protected Blob getMainBlob(DocumentModel doc) {
        
        return (Blob) doc.getPropertyValue("file:content");
    }

}
