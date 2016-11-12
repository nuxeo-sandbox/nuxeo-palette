function displayItems(gridItemJSONString) {
    var gridItemJSON = jQuery.parseJSON(gridItemJSONString);
    var paletteItem, i, lastRow, colNumber;

    gridster.remove_all_widgets();

    // Realign column numbers (can have holes when items are removed)
    lastRow = ""; // Saved as string
    colNumber = 0;
    for (i = 0; i < gridItemJSON.length; i++) {
        paletteItem = gridItemJSON[i];
        if (paletteItem.order > 0) {
            if (lastRow === "") {
                lastRow = paletteItem.row;
                colNumber = 1;
            } else if (lastRow !== paletteItem.row) {
                lastRow = paletteItem.row;
                colNumber = 1;
            } else {
                colNumber += 1;
            }

            paletteItem.col = "" + colNumber;
        }
    }

    // We first add the items we have an order first to make sure the news ones are after the first ones.
    for (i = 0; i < gridItemJSON.length; i++) {
        paletteItem = gridItemJSON[i];
        if (paletteItem.order > 0) {
            gridster.add_widget('<li class="gridsterElement" id="' + parentWidgetId + '_palette_' + paletteItem.id
                    + '"> <img src="' + paletteItem.thumburl + '" class="paletteThumb"></li>', paletteItem.size_x,
                    paletteItem.size_y, paletteItem.col, paletteItem.row);
        }
    }

    for (i = 0; i < gridItemJSON.length; i++) {
        paletteItem = gridItemJSON[i];
        if (paletteItem.order == 0) {
            gridster.add_widget('<li class="gridsterElement" id="' + parentWidgetId + '_palette_' + paletteItem.id
                    + '"> <img src="' + paletteItem.thumburl + '" class="paletteThumb"></li>');
        }
    }
}

function savePalette() {
    nxClient.operation("Services.SetPaletteItems").input(currentDocumentId).params({
        paletteJSONString : JSON.stringify(Gridster.sort_by_row_and_col_asc(gridster.serialize()))
    }).execute(function(error, data) {
        if (error) {
            throw error;
        } else {
            // console.log(JSON.stringify(gridster.serialize()));
        }
    });
}

function resetPalette() {
    // Should be done when clicking some "Cancel" button, and at initialization
    var gritItemJSONString;

    if (typeof nxClient === "undefined" || nxClient === null) {
        console.error("nxClient is invalid");
        return;
    }

    nxClient.schema([ "dublincore", "file", "palette" ]);
    nxClient.operation("Services.GetPaletteItems").input(currentDocumentId).params({
        "xpath" : customFieldXPath
    }).execute(function(error, data) {
        if (error) {
            throw error;
        } else {
            gritItemJSONString = data.value;
            displayItems(gritItemJSONString);
            /*
             * console.log(gritItemJSONString); console.log("--"); console.log(JSON.stringify(gridster.serialize()));
             */
            // console.log(JSON.stringify(Gridster.sort_by_row_and_col_asc(gridster.serialize())));
        }
    });
}

function notifyServerPaletteChanges() {

    // Should be done when clicking some "Save" button
    savePalette();
}

jQuery(function() { // DOM Ready

    gridster = jQuery(".gridster ul").gridster({
        widget_margins : [ palette_margins_horizontal, palette_margins_vertical ],
        widget_base_dimensions : [ palette_base_dimensions_width, palette_base_dimensions_heigth ],
        max_cols : palette_max_cols,
        min_cols : palette_min_cols,
        serialize_params : function($w, wgd) {
            return {
                col : wgd.col,
                row : wgd.row,
                size_x : wgd.size_x,
                size_y : wgd.size_y,
                id : wgd.el[0].id,
                docId : wgd.el[0].id.replace(parentWidgetId + '_palette_', ''),
                order : 1 + (wgd.row - 1) * gridster.cols + (wgd.col - 1)
            };
        },
        draggable : {
            stop : notifyServerPaletteChanges
        }
    }).data('gridster');

    nxClient = new nuxeo.Client();
    nxClient.connect(function(error, nxClient) {
        if (error) {
            nxClient = null;
            // cannot connect
            throw error;
        }
    });

    resetPalette();

});
