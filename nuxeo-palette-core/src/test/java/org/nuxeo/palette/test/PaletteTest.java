/**
 *
 */
package org.nuxeo.palette.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
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
@Deploy({ "org.nuxeo.palette" })
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
    public void paletteTest() throws JSONException{

        assertNotNull(folder);
        DocumentModelList children = session.getChildren(folder.getRef());
        assertEquals(4,children.size());
        String itemJsonString = getPaletteItemsForDocument(folder);
        JSONArray itemJson = new JSONArray(itemJsonString);
        assertEquals(4,itemJson.length());

    }
}
