function displayItems(gridItemJSONString){
	var gridItemJSON  = jQuery.parseJSON(gridItemJSONString);
	var i;
	//We first add the items we have an order first to make sure the news ones are after the first ones.
	for(i=0;i<gridItemJSON.length;i++)
	{
		if(gridItemJSON[i].order>0){
			gridster.add_widget('<li class="gridsterElement" id="'+parentWidgetId+'_palette_'+gridItemJSON[i].id+'"> <img src="'+gridItemJSON[i].thumburl+'" class="paletteThumb"></li>',
					gridItemJSON[i].size_x,gridItemJSON[i].size_y,gridItemJSON[i].col,gridItemJSON[i].row);
		}
	}
	
	for(i=0;i<gridItemJSON.length;i++)
	{
		if(gridItemJSON[i].order==0){
			gridster.add_widget('<li class="gridsterElement" id="'+parentWidgetId+'_palette_'+gridItemJSON[i].id+'"> <img src="'+gridItemJSON[i].thumburl+'" class="paletteThumb"></li>');
		}
	}
}

function notifyServerPaletteChanges(){
	console.log(gridster.serialize().toJSON());
	/*nxClient.operation("Services.SetPaletteItems").input(currentDocumentId).params({
			paletteJSONString: gridster.serialize().toJSON()
		 })
	.execute(function(error,data){
		if (error) {
			throw error;
		}
		else {
			console.log("New Value Saved");
		}
	});*/
}

$(function(){ //DOM Ready
	gridster= $(".gridster ul").gridster({
	        widget_margins: [10, 10],
	        widget_base_dimensions: [100, 100],
	        serialize_params: function($w, wgd) {
	            return {
	            	id: wgd.el[0].id,
	            	docId: wgd.el[0].id.replace(parentWidgetId+'_palette_', ''),
	                col: wgd.col,
	                row: wgd.row,
	                sizeX:wgd.size_x,
	               	sizeY:wgd.size_y,
	                order: 1 + (wgd.row - 1) * gridster.cols + (wgd.col - 1)
	            };
	        },
	        max_cols:10,
	        draggable: {
	        	stop: notifyServerPaletteChanges
	        }
	    }).data('gridster');;

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
			}
		});
	
});


