/**
 * @projectDescription	View controller for the query status window (which is a GUI-only component of the CRC module).
 * @inherits 	i2b2.CRC.view
 * @namespace	i2b2.CRC.view.status
 * @author 		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik]
 */
console.group('Load & Execute component file: CRC > view > Status');
console.time('execute time');


// create and save the screen objects
i2b2.CRC.view.status = new i2b2Base_cellViewController(i2b2.CRC, 'status');
i2b2.CRC.view.status.visible = false;

i2b2.CRC.view.status.show = function() {
	i2b2.CRC.view.status.visible = true;
	$('crcStatusBox').show();
}
i2b2.CRC.view.status.hide = function() {
	i2b2.CRC.view.status.visible = false;
	$('crcStatusBox').hide();
}

i2b2.CRC.view.status.hideDisplay = function() {
	$('infoQueryStatusText').hide();
}
i2b2.CRC.view.status.showDisplay = function() {
	var targs = $('infoQueryStatusText').parentNode.parentNode.select('DIV.tabBox.active');
	// remove all active tabs
	targs.each(function(el) { el.removeClassName('active'); });
	// set us as active
	$('infoQueryStatusText').parentNode.parentNode.select('DIV.tabBox.tabQueryStatus')[0].addClassName('active');
	$('infoQueryStatusText').show();
}

// ================================================================================================== //
i2b2.CRC.view.status.Resize = function(e) {
	var viewObj = i2b2.CRC.view.status;
	if (viewObj.visible) {
		var ds = document.viewport.getDimensions();
		var w = ds.width;
		var h = ds.height;
		if (w < 840) {w = 840;}
		if (h < 517) {h = 517;}
		
		// resize our visual components
		var ve = $('crcStatusBox');
		ve.show();
		switch(viewObj.viewMode) {
			case "Patients":
				ve = ve.style;
				ve.left = w-550;
				ve.width = 524;
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					$('infoQueryStatusText').style.height = '100px';
					if (YAHOO.env.ua.ie > 0) {  
						ve.top = h-135; //196+44;
					} else {
						ve.top = h-152; //196+44;
					}
				} else {
					$('infoQueryStatusText').style.height = '144px';
					ve.top = h-196;
				}
				break;
			default:
				ve.hide();
		}
	}
}
// ================================================================================================== //
YAHOO.util.Event.addListener(window, "resize", i2b2.CRC.view.status.Resize, i2b2.CRC.view.status);

i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	newMode = newMode[0];
	this.viewMode = newMode;
	switch(newMode) {
		case "Patients":
			// check if other windows are zoomed and blocking us
			var zw = i2b2.hive.MasterView.getZoomWindows();
			if (zw.member("QT")) {
				this.visible = false;
			} else {
				this.visible = true;
			}
			break;
		default:
			this.visible = false;
			break;
	}
	if (this.visible) {
		$('crcStatusBox').show();
		this.Resize();
	} else {
		$('crcStatusBox').hide();		
	}
// -------------------------------------------------------
}),'',i2b2.CRC.view.status);


// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.action == "ADD") {
		switch (newMode.window) {
			case "QT":
				this.visible = false;
				this.isZoomed = false;
				i2b2.CRC.view.status.hide();
		}
	} else {
		switch (newMode.window) {
			case "QT":
				this.isZoomed = false;
				this.visible = true;
				i2b2.CRC.view.status.show();
		}
	}
	this.Resize();
}),'',i2b2.CRC.view.status);


console.timeEnd('execute time');
console.groupEnd();