# nuxeo-website-preview


This plug-in allows to display a website stored in Nuxeo either as:

* A `Folderish` Document containing the site, with relative paths. So for example, an index.html file with an `img` tag whose `src` attribute is `"img/logo.png"`,  and at first level of the Document, there is an `img` folder containing the logo.png Picture document

* Or a non `Folderish` document whose `file:content` field contains a blob that can be unzipped as a website (typically a .zip file). Please read below, "WARNING About the Zip Format"

* _Notice_: It will also display the minisite, if any, when the document is a `Folderish` whose content does not have any html file to display at first level and if this `Folderish` also have zip file in it's `file` schema.
  
* Also, this is about _previewing_ The preview is displayed in the context of the Nuxeo application. This means, some JavaScript or access to other website, if any, may fail (typically because of CORS)

**WARNING**: Please, see the _Security Warning_ below

## How to Use

The plug-in creates a _WebEngine_ module allowing to access the embedded website _via_ a URL. The name of the module (in the URLs) is `WSP` (WebSitePreview).

* In order tp be as easy possible to use, the main url _must_ end with `index.html`, _whatever the real name of your main html file_.

* So, to access the preview(*), the URL to use is:
    `{server:port}/nuxeo/site/WSP/main-parent-doc-id/index.html`
   
    This URL will typically be used in the UI using an `iframe`

* For example, say you have a _Folderish_ document, named "My Site", whose `id` is `1234-5678-9ABC-DEF0`, and you are testing on your localhost, you can display the preview using this URL: `http://localhost:8080/nuxeo/site/WSP/1234-5678-9ABC-DEF0/index.html`.

  The exact same URL is used if instead of a `Folderish` containing all the website as Nuxeo documents, it is a `File` whose `file:content` holds a zip containing the website

* The plugin contributes the `WebsitePreviewAvailable` facet:
  * Because not every `Folderish` and not every .zip host a website 
  * So you can dynamically add/remove (using the native `Document.AddFacet` and `Document.RemoveFacet` operaitons) this facet from your documents for quick test to display a "preview" button in the UI for example
  * **IMPORTANT**: The plugin does not add/remove this facet at anytime, it is for you to use.
    * A typical example would be a button in the UI, like "This is a website". The user clicks it and you run the `Document.Addfacet` operation to add the `WebsitePreviewAvailable` facet.
    * This could also be done automatically in a listener depending on some metadata and rules, and if the `Document.HasMinisite` operation returns `true` (see below).

(*) Assuming current user is logged in and has enough rights to at least _read_ the blobs.

## The "Main" HTML File

Whatever the source (a `Folderish` or a .zip), the plugin searches for a min html file using this algorithm:

* Whatever the name, it must be at first level, the plugin does not search in nested folders
* If, at first level, one file is `index.html`, the document is considered being a mini website and this file will be returned at the first peview
* If there is no `index.html` file, the plugin searches for any other .html and returns the first it finds, to be used at first display
* If there is no .html file at all at first level, the plug-in returns a 404 error.

**WARNING**

This means that if the source document contains at least one html document at first level, will be seen as a mini website. The preview will display the HTML, but the result is unpredictable if it is not website with relative paths for sub-elements.

This is why you should use the `WebsitePreviewAvailable ` facet and add your preview only when it is relevant



## Utilities - Operation(s)
The plugin provides the following operation(s):

####    `Document.HasMinisite`
* `input` is a document
*  `output` is the document, unchanged
*  The operation sets the `WSP_hasMinisite` boolean Context Variable with the result:
  *  If the Document has the `WebsitePreviewAvailable ` facet, `WSP_hasMinisite` is `true`
  *  Else, if it is a `Folderish` document with an html child at first level, `WSP_hasMinisite` is `true`
  *  Else, it the document has the `file` schema and `file:content` can be unzipped and contains an html file at first level, `WSP_hasMinisite` is set to ` true`
  *  Else, `WSP_hasMinisite` is set to `false.
  

## Usage in WebUI
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

Create an `xhtml` widget with an iframe, setting up the correct URL for the `src` attribut.


## WARNING About the Zip Format
When using a zip file, it must not be built with a tool that prefixes the paths of every item. For example, if you zip the `mysite` folder containing...

```
img
  logo.png
index.html
otherpage.html
```

... the zip file **must** not have a TOC prefixing every path with `mysite`. The list of files in the zip must be...

```
img/
img/logo.png
index.html
otherpage.html
```

...and not:

```
mysite/img/
mysite/img/logo.png
mysite/index.html
mysite/otherpage.html
```

## Security Warning
The plugin sends the files as they are stored (either as single Nuxeo Document or inside the .zip file). This means no sanitizing is done, the JavaScript is not filtered and is sent as is.

If there is any risk of a user uploading a website that contains some malicious JavaScript (like getting the cookies to steel the session), then we recommend to add configuration to approve/reject the document before making it available.

The plugin could also be forked and sanitizing the JavaScript can be done (Nuxeo has APIs for this purpose). This means the plugin would then send html files that contain no `<script>` tag at all for example.


## Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained in this repository.


## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Contributors:
Thibaud Arguillere (https://github.com/ThibArg)

## About Nuxeo

Nuxeo, developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia. Learn more at [www.nuxeo.com](http://www.nuxeo.com).
