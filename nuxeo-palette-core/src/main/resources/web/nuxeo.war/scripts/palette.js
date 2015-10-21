function displayItems(gridItemJSONString){
	var gridItemJSON  = jQuery.parseJSON(gridItemJSONString);
	var paletteItem;
	var i;
	//We first add the items we have an order first to make sure the news ones are after the first ones.
	for(i=0;i<gridItemJSON.length;i++)
	{	
		paletteItem = gridItemJSON[i];
		if(paletteItem.order>0){
			gridster.add_widget('<li class="gridsterElement" id="'+parentWidgetId+'_palette_'+paletteItem.id+'"> <img src="'+paletteItem.thumburl+'" class="paletteThumb"></li>',
					paletteItem.size_x,paletteItem.size_y,paletteItem.col,paletteItem.row);
		}
	}
	
	for(i=0;i<gridItemJSON.length;i++)
	{	
		paletteItem = gridItemJSON[i];
		if(paletteItem.order==0){
			gridster.add_widget('<li class="gridsterElement" id="'+parentWidgetId+'_palette_'+paletteItem.id+'"> <img src="'+paletteItem.thumburl+'" class="paletteThumb"></li>');
		}
	}
}

function notifyServerPaletteChanges(){
	nxClient.operation("Services.SetPaletteItems").input(currentDocumentId).params({
			paletteJSONString: JSON.stringify(Gridster.sort_by_row_and_col_asc(gridster.serialize()))
		 })
	.execute(function(error,data){
		if (error) {
			throw error;
		}else{
			//console.log(JSON.stringify(gridster.serialize()));
		}
	});
}

jQuery(function(){ //DOM Ready
	
	gridster= jQuery(".gridster ul").gridster({
	        widget_margins: [palette_margins_horizontal, palette_margins_vertical],
	        widget_base_dimensions: [palette_base_dimensions_width, palette_base_dimensions_heigth],
	        max_cols:palette_max_cols,
	        min_cols:palette_min_cols,
	        serialize_params: function($w, wgd) {
	            return {
	            	col: wgd.col,
	                row: wgd.row,
	                size_x:wgd.size_x,
	               	size_y:wgd.size_y,
	            	id: wgd.el[0].id,
	            	docId: wgd.el[0].id.replace(parentWidgetId+'_palette_', ''),
	                order: 1 + (wgd.row - 1) * gridster.cols + (wgd.col - 1)
	            };
	        },
	        draggable: {
	        	stop: notifyServerPaletteChanges
	        }
	    }).data('gridster');

	nxClient = new nuxeo.Client();
	nxClient.connect(function(error, nxClient) {
	  if (error) {
	    // cannot connect
	    throw error;
	  }
	});
	
	var gritItemJSONString ;
	nxClient.schema(["dublincore", "file", "palette"]);
	nxClient.operation("Services.GetPaletteItems").input(currentDocumentId)
		.execute(function(error,data){
			if (error) {
				throw error;
			}
			else {
				gritItemJSONString=data.value;
				displayItems(gritItemJSONString);
				//console.log(JSON.stringify(Gridster.sort_by_row_and_col_asc(gridster.serialize())));
				
			}
		});
	
});


