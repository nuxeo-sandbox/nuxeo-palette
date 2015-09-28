/**
 *
 */

package org.nuxeo.palette;

import org.json.JSONException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author fvadon
 */
@Operation(id=GetPaletteItems.ID, category=Constants.CAT_SERVICES, label="Get Palette Items", description="")
public class GetPaletteItems extends PaletteActions{

    @Context
    protected CoreSession coreSession;

    public static final String ID = "Services.GetPaletteItems";

    @OperationMethod
    public String run(DocumentModel input) throws JSONException {
      return getPaletteItemsForDocument(input);
    }

}
