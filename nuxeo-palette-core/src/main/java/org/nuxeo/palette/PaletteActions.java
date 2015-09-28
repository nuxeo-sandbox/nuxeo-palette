package org.nuxeo.palette;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.util.ComplexTypeJSONDecoder;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailAdapter;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.runtime.api.Framework;

/**
 * @author fvadon
 *
 */
public abstract class PaletteActions {

    public static final String THUMB_THUMBNAIL = "thumb:thumbnail";

    public static final String DOWNLOAD_THUMBNAIL = "downloadThumbnail";
    public static final String PALETTE_FACET = "palette";
    public static final String PALETTE_XPATH = "palette:items";


    protected String getPaletteItemsForDocument(DocumentModel document) throws JSONException{
        CoreSession session = document.getCoreSession();
        DocumentModelList children = session.getChildren(document.getRef());
        JSONArray array = new JSONArray();

        for (DocumentModel child : children) {
            JSONObject object = new JSONObject();
            object.put("id", child.getId());
            object.put("order", "0");
            object.put("thumburl", getThumbnailUrl(child));
            array.put(object);
        }
        return array.toString();
    }

    protected DocumentModel setPaletteItemsForDocument(DocumentModel document, String paletteJSON) throws IOException {
        document.addFacet(PALETTE_FACET);
        Property complexMeta = document.getProperty(PALETTE_XPATH);
        ListType ltype = (ListType) complexMeta.getField().getType();

        List<Object> newVals = ComplexTypeJSONDecoder.decodeList(ltype,
                paletteJSON);
       complexMeta.setValue(newVals);


        document = document.getCoreSession().saveDocument(document);
        return document;

    }

    private String getThumbnailUrl(DocumentModel document) {
        ThumbnailAdapter thumbnailAdapter = document.getAdapter(ThumbnailAdapter.class);
        String url="";
        if (thumbnailAdapter != null) {
                Blob thumbnail = null;
                //BlobHolder blobHolder = document.getAdapter(BlobHolder.class);
                //thumbnail = blobHolder.getBlob();
                thumbnail=        thumbnailAdapter.getThumbnail(document.getCoreSession());

                if (thumbnail != null) {
                    url = DocumentModelFunctions.fileUrl(Framework.getProperty("nuxeo.url"),DOWNLOAD_THUMBNAIL, document, THUMB_THUMBNAIL, thumbnail.getFilename());
                }
        }
        return url;
    }

}
