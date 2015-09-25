package org.nuxeo.palette;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * @author fvadon
 *
 */
public abstract class PaletteActions {

    protected String getPaletteItemsForDocument(DocumentModel document) throws JSONException{
        CoreSession session = document.getCoreSession();
        DocumentModelList children = session.getChildren(document.getRef());

        JSONArray array = new JSONArray();

        for (DocumentModel child : children) {
            JSONObject object = new JSONObject();
            object.put("id", child.getId());
            object.put("order", "0");
            array.put(object);
        }

        return array.toString();

    }
}
