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
 *     Thibaud Arguillere
 */
package org.nuxeo.palette;

import org.json.JSONException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

@Operation(id = RemovePaletteItem.ID, category = Constants.CAT_SERVICES, label = "Remove Palette Item", description = "Remove the item given its document ID.")
public class RemovePaletteItem extends PaletteActions {

    @Context
    protected CoreSession coreSession;

    public static final String ID = "Services.RemovePaletteItem";

    @Param(name = "docId")
    protected String docId = null;

    @Param(name = "save", required = false, values = { "true" })
    protected boolean save = true;

    @OperationMethod
    public DocumentModel run(DocumentModel input) throws JSONException {

        if(input.hasFacet(PALETTE_FACET)) {
            input = removePaletteItemForDocument(input, docId, save);
        }
        return input;
    }

}
