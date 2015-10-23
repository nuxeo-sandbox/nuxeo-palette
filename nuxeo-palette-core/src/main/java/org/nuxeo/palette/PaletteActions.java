package org.nuxeo.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.runtime.api.Framework;

/**
 * @author fvadon
 */
public abstract class PaletteActions {

    public static final String THUMB_THUMBNAIL = "thumb:thumbnail";

    public static final String DOWNLOAD_THUMBNAIL = "downloadThumbnail";

    public static final String PALETTE_FACET = "palette";

    public static final String PALETTE_XPATH = "palette:items";

    public static final String DOC_ID_PALETTE_FIELD = "docId";

    public static final String PALETTE_ORDER_CHANGED_EVENT = "PaletteOrderChanged";

    protected String getPaletteItemsForDocument(DocumentModel document) throws JSONException {
        CoreSession session = document.getCoreSession();
        DocumentModelList children = session.query("Select * from Document where ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 "
                + "AND ecm:currentLifeCycleState != 'deleted' and (ecm:parentId= '"
                + document.getId()
                + "' OR collectionMember:collectionIds/* = '" + document.getId() + "') ORDER BY dc:title");
        JSONArray array = new JSONArray();
        boolean hasPreviousPalette = document.hasFacet(PALETTE_FACET);

        Property previousPaletteItems = null;
        Property previousPaletteItem = null;
        HashMap<String, Integer> itemPropertyPositions = null;
        if (hasPreviousPalette) {
            previousPaletteItems = document.getProperty(PALETTE_XPATH);
            itemPropertyPositions = getItemsPropertyPositions(previousPaletteItems);
        }

        for (DocumentModel child : children) {
            JSONObject object = new JSONObject();
            object.put("id", child.getId());
            if (hasPreviousPalette && itemPropertyPositions.containsKey(child.getId())) {
                previousPaletteItem = previousPaletteItems.get(itemPropertyPositions.get(child.getId()));
                object.put("col", previousPaletteItem.getValue("col"));
                object.put("row", previousPaletteItem.getValue("row"));
                object.put("size_x", previousPaletteItem.getValue("size_x"));
                object.put("size_y", previousPaletteItem.getValue("size_y"));
                object.put("order", previousPaletteItem.getValue("order"));
            } else {
                object.put("order", "0");
            }
            object.put("thumburl", getThumbnailUrl(child));
            array.put(object);
        }

        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            jsonValues.add(array.getJSONObject(i));
        }
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "order";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                int valA=0;
                int valB=0;

                try {
                    valA = Integer.parseInt((String) a.get(KEY_NAME));
                    valB = Integer.parseInt((String)  b.get(KEY_NAME));
                }
                catch (JSONException e) {
                    //do something
                }

                return (valA-valB);
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < array.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }

        return sortedJsonArray.toString();
    }

    protected DocumentModel setPaletteItemsForDocument(DocumentModel document, String paletteJSON) throws IOException {
        document.addFacet(PALETTE_FACET);
        Property complexMeta = document.getProperty(PALETTE_XPATH);
        ListType ltype = (ListType) complexMeta.getField().getType();

        List<Object> newVals = ComplexTypeJSONDecoder.decodeList(ltype, paletteJSON);
        complexMeta.setValue(newVals);
        document = document.getCoreSession().saveDocument(document);
        // send event
        CoreSession session = document.getCoreSession();
        EventContextImpl evctx = new DocumentEventContext(session, session.getPrincipal(), document);
        Event event = evctx.newEvent(PALETTE_ORDER_CHANGED_EVENT);
        EventService eventService = Framework.getLocalService(EventService.class);
        eventService.fireEvent(event);

        return document;

    }

    private String getThumbnailUrl(DocumentModel document) {
        ThumbnailAdapter thumbnailAdapter = document.getAdapter(ThumbnailAdapter.class);
        String url = "";
        if (thumbnailAdapter != null) {
            Blob thumbnail = null;
            thumbnail = thumbnailAdapter.getThumbnail(document.getCoreSession());

            if (thumbnail != null) {
                url = DocumentModelFunctions.fileUrl(Framework.getProperty("nuxeo.url"), DOWNLOAD_THUMBNAIL, document,
                        THUMB_THUMBNAIL, thumbnail.getFilename());
            }
        }
        return url;
    }

    protected HashMap<String, Integer> getItemsPropertyPositions(Property paletteItems) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        int i;
        for (i = 0; i < paletteItems.size(); i++) {
            map.put((String) paletteItems.get(i).getValue(DOC_ID_PALETTE_FIELD), i);
        }
        return map;
    }



}
