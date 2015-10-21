/**
 *
 */
package org.nuxeo.palette.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
@Deploy({ "org.nuxeo.palette","org.nuxeo.ecm.platform.thumbnail","org.nuxeo.ecm.platform.types.api","org.nuxeo.ecm.platform.types.core","org.nuxeo.ecm.platform.collections.core"})
//@LocalDeploy({ "nuxeo-thumbnail:thumbnail-default-factories-contrib.xml"})
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

        document=session.createDocumentModel("/Folder/", "file1", "File");
        document.setPropertyValue("dc:title", "File1");
        document = session.createDocument(document);
        session.save();

        document=session.createDocumentModel("/Folder/", "file2", "File");
        document.setPropertyValue("dc:title", "File2");
        document = session.createDocument(document);
        session.save();

        document=session.createDocumentModel("/Folder/", "file3", "File");
        document.setPropertyValue("dc:title", "File3");
        document = session.createDocument(document);
        session.save();

        document=session.createDocumentModel("/Folder/", "file4", "File");
        document.setPropertyValue("dc:title", "File4");
        document = session.createDocument(document);
        session.save();

    }

    @Test
    public void paletteTest() throws JSONException, IOException{

        assertNotNull(folder);
        DocumentModelList children = session.getChildren(folder.getRef());
        assertEquals(4,children.size());
        String itemJsonString = getPaletteItemsForDocument(folder);
        JSONArray itemJson = new JSONArray(itemJsonString);
        assertEquals(4,itemJson.length());
        assertFalse(folder.hasFacet("palette"));
        String paletteJSONSample = "[{\"id\": \"nxw_sub0_palette_faaa64ee-5148-47b0-9237-3d8f825420f8\", \"docId\": \"faaa64ee-5148-47b0-9237-3d8f825420f8\", \"col\": 2, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 2}, {\"id\": \"nxw_sub0_palette_184de7f7-40a7-4414-b1e0-3cc808abfb5d\", \"docId\": \"184de7f7-40a7-4414-b1e0-3cc808abfb5d\", \"col\": 3, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 3}, {\"id\": \"nxw_sub0_palette_49b10daf-0cbe-493a-9fac-114f04d18947\", \"docId\": \"49b10daf-0cbe-493a-9fac-114f04d18947\", \"col\": 1, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 1}, {\"id\": \"nxw_sub0_palette_88cb6c08-f536-4bdf-a571-0059d4d49b66\", \"docId\": \"88cb6c08-f536-4bdf-a571-0059d4d49b66\", \"col\": 4, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 4}, {\"id\": \"nxw_sub0_palette_73a00873-cc82-4add-a50c-afc63d7188da\", \"docId\": \"73a00873-cc82-4add-a50c-afc63d7188da\", \"col\": 5, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 5}, {\"id\": \"nxw_sub0_palette_4d939e07-3fdb-495b-8bf7-bbef5a65b695\", \"docId\": \"4d939e07-3fdb-495b-8bf7-bbef5a65b695\", \"col\": 6, \"row\": 1, \"size_x\": 1, \"size_y\": 1, \"order\": 6}]";
        folder = setPaletteItemsForDocument(folder, paletteJSONSample);
        assertTrue(folder.hasFacet("palette"));
        Property storedPaletteItems = folder.getProperty(PALETTE_XPATH);
        assertTrue(storedPaletteItems.size()==6);


    }
}
