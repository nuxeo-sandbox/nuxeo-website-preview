## nuxeo-website-preview


The plug-in provides a widget which displays the preview of a mini-site, where an .html file is displayed, referencing images and resources with _relative_ paths.

It assumes the current document:

* Is a `Folderish`
* And it contains _at its first level_:
  * One single .html file
  * Or, if several, one whose name is `index.html`

If the folder contains several Document whose blobs are html files and none of them has a file named "index.html", it returns one of them (arbitrary), so it is likely the site will not work correctly in this case.

**WARNING**

This means that a folder contain html document(s) will be seen as a mini website. The preview will display the HTML, but the result is unpredictable.

#### WARNING AND TODO
* **Separate JSF** from WebUI. As of today, the plugin requires both UIs installed
* Add **unit tests**
* Make it a service:
  * For example: Contribute the NXQL used to detect the html file
  * Add listeners to automatically check if it can be website and add the `WebsitePreviewAvailable` facet
  * . . .
 


### Good to Know
* it is a _preview_ of the html and its related resources: Related links will all request a resource from the nuxeo server. It is ok for any resource stored in the correct folder/subfolder, but links will not work with custom and dynamic URLS.

* The plug-in uses a _WebEngine_ module whose name (in the URLs) is `WSP` (**W**eb**S**ite**P**review).

* Also, to be a bit more user friendly, the main url _must_ end with `index.html`, _whatever the real name of your main html file_.

* So, to access the preview(*), the URL to use is:

    `{server:port}/nuxeo/site/WSP/main-parent-doc-id/index.html`

For example, say you have a _Folderish_ document, named "My Site", whose `id` is `1234-5678-9ABC-DEF0`, and you are testing on your localhost, you can display the preview using this URL: `http://localhost:8080/nuxeo/site/WSP/1234-5678-9ABC-DEF0/index.html`

* The plugin contributes the `WebsitePreviewAvailable` facet:
  * so you can dynamically add/remove this facet from your documents for quick test to display a "preview" button in the UI for example
  * But notice the plugin does not add/remove it at anytime,; it just tests it in the hasMinisite operation.

(*) Assuming current user is logged in and has enough rights to at least _read_ the blobs, or is anonymous and you allowed anonymous users and setup the permissions, etc. etc.

### Utilities - Operation(s)
The plugin provides the following operation(s):

####    `Document.HasMinisite`
* `input` is a document
*  `output` is the document, unchanged
*  The operation looks for an html file inside the document and it it finds it, it sets the `WSP_hasMinisite` Context Variable to `true`
*  If the input document...
  * Does not have the "" facet
  * _or_is not a `Folderish` that contains at elast an HTML document at its first level
  
  ...`WSP_hasMinisite` Context Variable is set to `false`

### Usage in WebUI
* First, create an element (in Studio Designer > Resources) with an iframe and set the src of the iframe to the correct url. For example:


```
<dom-module id="my-website-preview">
  <template>
    <style>
      *[role=widget] {
        padding: 5px;
      }

      iframe {
        position: absolute;
        width: 95%;
        height: 90%;
        border: 2 2 2 2;
      }

    </style>

    <iframe src="/nuxeo/site/WSP/[[document.uid]]/index.html"></iframe>

  </template>

  <script>
    Polymer({
      is: 'my-website-preview',
      behaviors: [Nuxeo.LayoutBehavior],
      properties: {
        
        document: {
          type: Object
        }
      }

    });
  </script>
</dom-module>
```

* Create a Document Page _pill_ (In Studio Designer > UI)  displaying this element, with the correct filter (typically, if the document has the `WebsitePreviewAvailable` facet)

## Usage in JSF UI

After installing the plug-ins in the server (see below) you must use the specific preview widget deployed by the plug-in. Typically, we recommend doing the following in Nuxeo Studio:

* Create a new Tab, title "Preview" for example
* In the "Activation" sub tab:
  * Make it available only for document with the "Folderish" facet
  * We also recommend you add the following expression in the "Custom EL expression" part: `#{websitePreview.hasMiniSite()}`. This will make sure the "Preview" tab is not displayed when a folder does not contain any html file.
* Drop a "Template" widget
  * In "Custom Properties Configuration", add a new property
  * Name:  `template`
  * Value: `/widgets/website-preview-widget.xhtml`

Nothing more to do.

### License
(C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.

All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Lesser General Public License
(LGPL) version 2.1 which accompanies this distribution, and is available at
http://www.gnu.org/licenses/lgpl-2.1.html

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

Contributors:
Thibaud Arguillere (https://github.com/ThibArg)

### About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com) and packaged applications for Document Management, Digital Asset Management and Case Management. Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.
