/**
 *
 */
package org.nuxeo.palette.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.palette.PaletteActions;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author fvadon
 *
 */
@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy({ "org.nuxeo.palette", "org.nuxeo.ecm.platform.thumbnail",
        "org.nuxeo.ecm.platform.types.api",
        "org.nuxeo.ecm.platform.types.core",
        "org.nuxeo.ecm.platform.collections.core" })
// @LocalDeploy({ "nuxeo-thumbnail:thumbnail-default-factories-contrib.xml"})
public class PaletteTest extends PaletteActions {
    @Inject
    CoreSession session;

    @Inject
    RepositoryManager rm;

    protected DocumentModel folder;

    protected DocumentModel document;

    @Before
    public void initRepo() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        folder = session.createDocumentModel("/", "Folder", "Folder");
        folder.setPropertyValue("dc:title", "Folder");
        folder = session.createDocument(folder);
        session.save();
        folder = session.getDocument(folder.getRef());

        document = session.createDocumentModel("/Folder/", "file1", "File");
        document.setPropertyValue("dc:title", "File1");
        document = session.createDocument(document);
        session.save();

        document = session.createDocumentModel("/Folder/", "file2", "File");
        document.setPropertyValue("dc:title", "File2");
        document = session.createDocument(document);
        session.save();

        document = session.createDocumentModel("/Folder/", "file3", "File");
        document.setPropertyValue("dc:title", "File3");
        document = session.createDocument(document);
        session.save();

        document = session.createDocumentModel("/Folder/", "file4", "File");
        document.setPropertyValue("dc:title", "File4");
        document = session.createDocument(document);
        session.save();

    }

    @Test
    public void paletteTest() throws JSONException, IOException {

        assertNotNull(folder);
        DocumentModelList children = session.getChildren(folder.getRef());
        assertEquals(4, children.size());
        String itemJsonString = getPaletteItemsForDocument(folder);
        JSONArray itemJson = new JSONArray(itemJsonString);
        assertEquals(4, itemJson.length());
        assertFalse(folder.hasFacet("palette"));
        String paletteJSONSample = "[{\"id\": \"nxw_sub0_palette_1\", \"docId\": \"uid1\", \"col\": 2, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 2}"
                + ", {\"id\": \"nxw_sub0_palette_2\", \"docId\": \"uid2\", \"col\": 3, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 3}"
                + ", {\"id\": \"nxw_sub0_palette_3\", \"docId\": \"uid3\", \"col\": 1, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 1}"
                + ", {\"id\": \"nxw_sub0_palette_4\", \"docId\": \"uid4\", \"col\": 4, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 4}"
                + ", {\"id\": \"nxw_sub0_palette_5\", \"docId\": \"uid5\", \"col\": 5, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 5}"
                + ", {\"id\": \"nxw_sub0_palette_6\", \"docId\": \"uid6\", \"col\": 6, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 6}]";
        folder = setPaletteItemsForDocument(folder, paletteJSONSample);
        assertTrue(folder.hasFacet("palette"));
        Property storedPaletteItems = folder.getProperty(PALETTE_XPATH);
        assertTrue(storedPaletteItems.size() == 6);

        // Test removePaletteItemForDocument: Remove the second one
        folder = removePaletteItemForDocument(folder, "uid2", true);
        @SuppressWarnings("unchecked")
        ArrayList<Map<String, Serializable>> complexValues = (ArrayList<Map<String, Serializable>>) folder.getPropertyValue(PALETTE_XPATH);
        assertEquals(5, complexValues.size());

        // Test getPaletteItemsDocumentIDs
        String docIds = getPaletteItemsDocumentIDs(folder);
        JSONArray array = new JSONArray(docIds);
        int length = array.length();
        assertEquals(5, length);
        assertEquals("uid1", array.getString(0));
        assertEquals("uid3", array.getString(1));
        assertEquals("uid4", array.getString(2));
        assertEquals("uid5", array.getString(3));
        assertEquals("uid6", array.getString(4));

    }
}
