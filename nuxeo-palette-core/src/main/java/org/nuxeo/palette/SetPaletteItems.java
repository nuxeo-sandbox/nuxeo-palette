/**
 *
 */

package org.nuxeo.palette;

import java.io.IOException;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author fvadon
 */
@Operation(id=SetPaletteItems.ID, category=Constants.CAT_SERVICES, label="Set Palette Items", description="")
public class SetPaletteItems extends PaletteActions{

    public static final String ID = "Services.SetPaletteItems";

    @Param(name = "paletteJSONString", required = true)
    protected String paletteJSONString;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) throws IOException {
      return setPaletteItemsForDocument(input, paletteJSONString);
    }

}
