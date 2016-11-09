/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Frédéric Vadon
 *     Thibaud Arguillere
 */
package org.nuxeo.palette;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.util.ComplexTypeJSONDecoder;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
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

    public static final String ORDER_PALETTE_FIELD = "order";

    public static final String PALETTE_ORDER_CHANGED_EVENT = "PaletteOrderChanged";

    /*
     * This is the main method that builds the JSON string with the children.
     */
    protected String buildPaletteItemsForDocument(DocumentModel mainDoc,
            DocumentModelList documents) throws JSONException {

        JSONArray array = new JSONArray();
        boolean hasPreviousPalette = mainDoc.hasFacet(PALETTE_FACET);

        Property previousPaletteItems = null;
        Property previousPaletteItem = null;
        HashMap<String, Integer> itemPropertyPositions = null;
        if (hasPreviousPalette) {
            previousPaletteItems = mainDoc.getProperty(PALETTE_XPATH);
            itemPropertyPositions = getItemsPropertyPositions(previousPaletteItems);
        }

        for (DocumentModel child : documents) {
            JSONObject object = new JSONObject();
            object.put("id", child.getId());
            if (hasPreviousPalette
                    && itemPropertyPositions.containsKey(child.getId())) {
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
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            // You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "order";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                int valA = 0;
                int valB = 0;

                try {
                    valA = Integer.parseInt((String) a.get(KEY_NAME));
                    valB = Integer.parseInt((String) b.get(KEY_NAME));
                } catch (JSONException e) {
                    // do something
                }

                return (valA - valB);
                // if you want to change the sort order, simply use the
                // following:
                // return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < array.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }

        return sortedJsonArray.toString();

    }

    protected String getPaletteItemsForDocument(DocumentModel document)
            throws JSONException {
        return getPaletteItemsForDocument(document, null);
    }

    /*
     * Handling compatibility with version< <= 1.0.4, where there were no xpath
     * parameter, we used only a Folderish or a collection. New version accepts
     * a String field (multivalued, storing document IDs) as parameter
     */
    protected String getPaletteItemsForDocument(DocumentModel document,
            String xpath) throws JSONException {

        CoreSession session = document.getCoreSession();
        DocumentModelList children;

        if (StringUtils.isBlank(xpath)) {
            children = session.query("SELECT * FROM Document WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 "
                    + "AND ecm:currentLifeCycleState != 'deleted' AND (ecm:parentId= '"
                    + document.getId()
                    + "' OR collectionMember:collectionIds/* = '"
                    + document.getId() + "') ORDER BY dc:title");
        } else {
            String[] uuids = (String[]) document.getPropertyValue(xpath);
            if (uuids == null || uuids.length == 0) {
                children = new DocumentModelListImpl();
            } else {
                StringBuffer sb = new StringBuffer(
                        "SELECT * FROM Document WHERE ecm:uuid IN (");
                boolean first = true;
                for (String uid : uuids) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append("'" + uid + "'");
                }
                sb.append(") ORDER BY dc:title");
                children = session.query(sb.toString());

            }
        }

        return buildPaletteItemsForDocument(document, children);
    }

    protected DocumentModel setPaletteItemsForDocument(DocumentModel document,
            String paletteJSON) throws IOException {
        document.addFacet(PALETTE_FACET);
        Property complexMeta = document.getProperty(PALETTE_XPATH);
        ListType ltype = (ListType) complexMeta.getField().getType();

        List<Object> newVals = ComplexTypeJSONDecoder.decodeList(ltype,
                paletteJSON);
        complexMeta.setValue(newVals);
        document = document.getCoreSession().saveDocument(document);
        // send event
        CoreSession session = document.getCoreSession();
        EventContextImpl evctx = new DocumentEventContext(session,
                session.getPrincipal(), document);
        Event event = evctx.newEvent(PALETTE_ORDER_CHANGED_EVENT);
        EventService eventService = Framework.getLocalService(EventService.class);
        eventService.fireEvent(event);

        return document;

    }

    protected DocumentModel removePaletteItemForDocument(
            DocumentModel document, String docId, boolean saveDoc) {

        if (StringUtils.isNotBlank(docId)) {
            @SuppressWarnings("unchecked")
            ArrayList<Map<String, Serializable>> complexValues = (ArrayList<Map<String, Serializable>>) document.getPropertyValue(PALETTE_XPATH);
            if (complexValues != null && complexValues.size() > 0) {
                boolean found = false;
                for (Map<String, Serializable> oneEntry : complexValues) {
                    String id = (String) oneEntry.get(DOC_ID_PALETTE_FIELD);
                    if (StringUtils.isNotBlank(id) && id.equals(docId)) {
                        complexValues.remove(oneEntry);
                        found = true;
                        break;
                    }
                }

                // Reset order numbers
                if(found) {
                    int index = 0;
                    int orderValue;
                    String orderStr;

                    for (Map<String, Serializable> oneEntry : complexValues) {
                        try {
                            orderStr = (String) oneEntry.get(ORDER_PALETTE_FIELD);
                            orderValue = Integer.parseInt(orderStr);
                            if(orderValue > 0) {
                                index += 1;
                                orderValue = index;
                            } else {
                                orderValue = 0;
                            }
                        } catch (NumberFormatException e) {
                            orderValue = 0;
                        }
                        oneEntry.put(ORDER_PALETTE_FIELD, "" + orderValue);
                    }

                    document.setPropertyValue(PALETTE_XPATH, complexValues);
                    if (saveDoc) {
                        document = document.getCoreSession().saveDocument(
                                document);
                    }
                }
            }
        }

        return document;
    }

    private String getThumbnailUrl(DocumentModel document) {
        ThumbnailAdapter thumbnailAdapter = document.getAdapter(ThumbnailAdapter.class);
        String url = "";
        if (thumbnailAdapter != null) {
            Blob thumbnail = null;
            thumbnail = thumbnailAdapter.getThumbnail(document.getCoreSession());

            if (thumbnail != null) {
                url = DocumentModelFunctions.fileUrl(
                        Framework.getProperty("nuxeo.url"), DOWNLOAD_THUMBNAIL,
                        document, THUMB_THUMBNAIL, thumbnail.getFilename());
            }
        }
        return url;
    }

    protected HashMap<String, Integer> getItemsPropertyPositions(
            Property paletteItems) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        int i;
        for (i = 0; i < paletteItems.size(); i++) {
            map.put((String) paletteItems.get(i).getValue(DOC_ID_PALETTE_FIELD),
                    i);
        }
        return map;
    }

}
