
var ViewerCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Onesait Platform Control Panel'; 
	var LIB_TITLE = 'Viewer Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var hasId = false; // instance
	var fieldsArray = [];
	var baseLayers = [];
	var htmlEditor;
	var initMap= '';
	var setLayers = [];
	var isBaseLayerLoad= false;
	var base_tpl = null;

	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------

	var propertyTypeOntologyIndex=-1;
	
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// DELETE VIEWER
	var deleteViewer = function(viewerId){
		console.log('deleteViewerConfirmation() -> formId: '+ viewerId);

		// no Id no fun!
		if ( !viewerId ) {$.alert({title: 'ERROR!',  theme: 'light', content: ontologyCreateReg.validations.validform}); return false; }

		logControl ? console.log('deleteViewerConfirmation() -> formAction: ' + $('.delete-viewer').attr('action') + ' ID: ' + $('#delete-viewerId').attr('viewerId')) : '';

		// call ontology Confirm at header.
		HeaderController.showConfirmDialogViewer('delete_viewer_form');
	}
	
		
	var handleValidation =  function() {
        // for more info visit the official plugin documentation:
        // http://docs.jquery.com/Plugins/Validation

        var form1 = $('#viewer_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');


        form1.validate({
            errorElement: 'span', // default input error message container
            errorClass: 'help-block help-block-error', // default input error
														// message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .formcolorpicker, .hidden-validation')", // validate
																		// all
																		// fields
																		// including
																		// form
																		// hidden
																		// input
																		// but
																		// not
																		// selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {
//				jsonschema: { required:"El esquema no se ha guardado correctamente"},
//				datamodelid: { required: "Por favor seleccione una plantilla de ontología, aunque sea la vacia."}
			},
			// validation rules
            rules: {
            	ontology:		{ required: true },
                identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true },
				techonology:	{ required: true },
				baseLayer:		{ required: true },
            },
            invalidHandler: function(event, validator) { // display error
															// alert on form
															// submit
                success1.hide();
                error1.show();
                App.scrollTo(error1, -200);
            },
            errorPlacement: function(error, element) {
            	if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
				else if ( element.is(':hidden'))	{
					if ($('#datamodelid').val() === '') { $('#datamodelError').removeClass('hide');}
				}
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error');
            },
            unhighlight: function(element) { // revert the change done by
												// hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
            	
                error1.hide();
    			var infoBox=[];
    			
    			var js = htmlEditor.getValue();
    			src = base_tpl;
    			
    			// Javascript
    			js = '<script>' + js + '</script>';
    			src = src.replace('</body>', js + '</body>');
    			src = src.replace(/"/g, "'");
    			
    			$("<input type='hidden' name='rollback' value='"+$("#rollback").is(':checked')+"' />")
 		         .appendTo("#viewer_create_form");
    			
    			$("<input type='hidden' name='baseLayer' th:field='*{baseLayer}' value='"+$("#baseLayers").val()+"' />")
  		         .appendTo("#viewer_create_form");
    			
    			$('<input type="hidden" name="jsViewer" value="'+src+'" />')
 		         .appendTo("#viewer_create_form");
    			
    			if($("#layersSelect").val() != null){
    				$('<input type="hidden" name="layersSelectedHidden" value="'+$("#layersSelect").val()+'" />')
   		         .appendTo("#viewer_create_form");
    			}
    			
				form1.ajaxSubmit({type: 'post', success : function(data){
					
					navigateUrl(data.redirect);
					
					}, error: function(data){
						HeaderController.showErrorDialog(data.responseJSON.cause)
					}
				})
				

			}
        });
    }
	
	
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return viewerCreateJson = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			
			$(".panel-left").resizable({
			   handleSelector: ".splitter",
			   resizeHeight: false,
			   onDragStart: function(){
				 console.log('start...');
				 
			   },
			   onDragEnd: function(){
				 console.log('stop...');
				 
			   }
			 });
	
			handleValidation();
			// PROTOTYPEs
			// ARRAY PROTOTYPE FOR CHECK UNIQUE PROPERTIES.
			Array.prototype.unique = function() {
				return this.filter(function (value, index, self) { 
					return self.indexOf(value) === index;
				});
			};
			
			// ARRAY PROTROTYPE FOR REMOVE ELEMENT (not object) BY VALUE
			Array.prototype.remove = function() {
				var what, a = arguments, L = a.length, ax;				
				while (L && this.length) {
					what = a[--L];				
					while ((ax = this.indexOf(what)) !== -1) {
						console.log('AX: ' + ax);
						this.splice(ax, 1);
					}
				}
				return this;
			};
			
			$.ajax({
				url : "/controlpanel/static/js/htmlviewer.txt",
				type : 'GET',
				dataType: 'text', 
				contentType: 'text/plain',
				mimeType: 'text/plain',
				async : false,
				success : function(data) {
					
					base_tpl = data;
					
				},
				error : function(data, status, er) {
					$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
				}
			});
			
			// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
			if ( viewerCreateJson.actionMode === null){
				logControl ? console.log('|---> Action-mode: INSERT') : '';

				htmlEditor = CodeMirror.fromTextArea(document.getElementById("jsBody"), {
	        	  	mode: "text/javascript",
					lineNumbers: true,
		            foldGutter: true,
		            matchBrackets: true,
		            styleActiveLine: true,
		            theme:"default"
				});
				
			}
			// EDIT MODE ACTION 
			else {	
				logControl ? console.log('|---> Action-mode: UPDATE') : '';
					
				htmlEditor = CodeMirror.fromTextArea(document.getElementById("jsBody"), {
	        	  	mode: "text/javascript",
					lineNumbers: true,
		            foldGutter: true,
		            matchBrackets: true,
		            styleActiveLine: true,
		            theme:"default"
				});
				
				if(viewerCreateJson.isPublic){
					$("#public").attr("checked", "checked");
				}
				
				$("#technology").val(viewerCreateJson.tecnology);
				$('#technology').selectpicker('refresh');
				
				$.ajax({
					url : "/controlpanel/viewers/getBaseLayers/" + viewerCreateJson.tecnology,
					type : 'GET',
					dataType: 'text', 
					contentType: 'text/plain',
					mimeType: 'text/plain',
					async : false,
					success : function(data) {
						var options = [];
						$.each(JSON.parse(data), function(k,v){
							options.push('<option value="'+v.identification+'">'+v.name+'</option>');
							baseLayers.push(v);
						});
						
						$('#baseLayers').html(options);
						$('#baseLayers').removeAttr("disabled");
						$('#baseLayers').selectpicker('refresh');
						
						$("#baseLayers").val(viewerCreateJson.baseLayer);
						$('#baseLayers').selectpicker('refresh');
						
						
						var js = viewerCreateJson.js;
						
						var begin = js.indexOf("<script>");
						var end = js.lastIndexOf("script>");
						
						js = js.substring(end -2,begin + 8);
						
						htmlEditor.setValue(js);
						
						$("#container").css('visibility', 'visible');
						
						$.ajax({
							url : "/controlpanel/viewers/getLayers/",
							type : 'GET',
							dataType: 'text', 
							contentType: 'text/plain',
							mimeType: 'text/plain',
							async : false,
							success : function(data) {
								
								var options = [];
								
								$.each(JSON.parse(data), function(k,v){
									options.push('<option value="'+v+'">'+v+'</option>');
								});
								
								$('#layersSelect').html(options);
								$('#layersSelect').removeAttr("disabled");
								$('#layersSelect').selectpicker('refresh');
								
								$("#layersSelect").val(viewerCreateJson.layersInUse);
								$('#layersSelect').selectpicker('refresh');
								
							},
							error : function(data, status, er) {
								$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
							}
						});
						
						ViewerCreateController.run();
						
					},
					error : function(data, status, er) {
						$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
					}
				});
			}
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		deleteViewer: function(viewerId){
			deleteViewer(viewerId);
		},
		rollbackViewer: function(viewerId){
			$.ajax({
				url : "/controlpanel/viewers/doRollback/" + viewerCreateJson.actionMode,
				type : 'GET',
				dataType: 'text', 
				contentType: 'text/plain',
				mimeType: 'text/plain',
				success : function(data) {
					navigateUrl(data);
				},
				error : function(data, status, er) {
					$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
				}
			});
		},
		run : function() {
			event.preventDefault();
			
			var iframe = document.querySelector('#viewerIframe');
			iframe_doc = iframe.contentDocument;
			
			iframe_doc.open();
			iframe_doc.write('');
			iframe_doc.close();
			
			var js =htmlEditor.getValue();
			src = base_tpl;
			
			// Javascript
			js = '<script>' + js + '<\/script>';
			src = src.replace('</body>', js + '</body>');
			
			iframe_doc.open();
			iframe_doc.write(src);
			iframe_doc.close();
			
		},
		changeTechology : function(){
			$.ajax({
				url : "/controlpanel/viewers/getBaseLayers/" + $("#technology").val(),
				type : 'GET',
				dataType: 'text', 
				contentType: 'text/plain',
				mimeType: 'text/plain',
				success : function(data) {
					var options = [];
					$.each(JSON.parse(data), function(k,v){
						options.push('<option value="'+v.identification+'">'+v.name+'</option>');
						baseLayers.push(v);
					});
					
					$('#baseLayers').html(options);
					$('#baseLayers').removeAttr("disabled");
					$('#baseLayers').selectpicker('refresh');
					
				},
				error : function(data, status, er) {
					$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
				}
			});
		},
		changeBaseLayer : function(){
			
			var url='';
			$.each(baseLayers, function(k,v){
				if(v.identification == $("#baseLayers").val()){
					url = v.url;
				}
			});
			
			initMap= "initialBaseMap('"+$("#baseLayers").val()+"',accessTokenMapbox, '"+url+"')\n";
			var layersSelected = $("#layersSelect").val();
			var layersTypes = viewerCreateJson.layersTypes;
			if(!isBaseLayerLoad){
				$.get('/controlpanel/static/js/baseviewer.txt',  function(data){
					
					  data += initMap;
					  htmlEditor.setValue(data);
			          ViewerCreateController.run();
			          isBaseLayerLoad = true;
			          $("#container").css('visibility', 'visible');
						
						$.ajax({
							url : "/controlpanel/viewers/getLayers/",
							type : 'GET',
							dataType: 'text', 
							contentType: 'text/plain',
							mimeType: 'text/plain',
							success : function(data) {
								
								var options = [];
								
								$.each(JSON.parse(data), function(k,v){
									options.push('<option value="'+v+'">'+v+'</option>');
								});
								
								$('#layersSelect').html(options);
								$('#layersSelect').removeAttr("disabled");
								$('#layersSelect').selectpicker('refresh');
								
							},
							error : function(data, status, er) {
								$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
							}
						});
			    });
			}else{
				$.get('/controlpanel/static/js/baseviewer.txt',  function(data){
					
					  data += initMap;
					  if(layersSelected!=null){
						  setLayers = [];
						  setHeatLayers = [];
						  $.each(layersSelected, function(k,layerAux){
							  if(layersTypes[layerAux] == 'iot' ){
									$.ajax({
										url : "/controlpanel/viewers/getLayerData/" + layerAux,
										type : 'GET',
										dataType: 'text', 
										contentType: 'text/plain',
										mimeType: 'text/plain',
										async : false,
										success : function(data) {
											
											setLayers.push(data);
											
										},
										error : function(data, status, er) {
											$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
										}
									});
										
							  }else if(layersTypes[layerAux] == 'heat' ){
									$.ajax({
										url : "/controlpanel/viewers/getLayerData/" + layerAux,
										type : 'GET',
										dataType: 'text', 
										contentType: 'text/plain',
										mimeType: 'text/plain',
										async : false,
										success : function(data) {
											
											setHeatLayers.push(data);
											
										},
										error : function(data, status, er) {
											$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
										}
									});
										
							  }else if(layersTypes[layerAux] == 'wms' ){
								  $.ajax({
										url : "/controlpanel/viewers/getLayerWms/" + layerAux,
										type : 'GET',
										dataType: 'text', 
										contentType: 'text/plain',
										mimeType: 'text/plain',
										async : false,
										success : function(result) {
											result = JSON.parse(result);
											var urlLayer = result['url'];
											var layerWms = result['layerWms'];
											 data += "loadWms('"+urlLayer+"', '"+layerWms+"', '"+ layerAux +"')\n";
											
										},
										error : function(data, status, er) {
											$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
										}
									});
								  
								 
							  }else if(layersTypes[layerAux] == 'kml' ){
								  $.ajax({
										url : "/controlpanel/viewers/getLayerKml/" + layerAux,
										type : 'GET',
										dataType: 'text', 
										contentType: 'text/plain',
										mimeType: 'text/plain',
										async : false,
										success : function(result) {
											result = JSON.parse(result);
											var urlLayer = result['url'];
											 data += "loadKml('"+urlLayer+"', '"+ layerAux +"')\n";
											
										},
										error : function(data, status, er) {
											$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
										}
									});
								  
								 
							  }
							 
							}) 
							$.each(setLayers, function(k,v){
									data += "createLayer("+v+")\n";
							})
							
							$.each(setHeatLayers, function(k,v){
									data += "heatmapGenerator("+v+")\n";
							})
					  }
					 
					  htmlEditor.setValue(data);
			          ViewerCreateController.run();
			          
			    });
				
			}
			
		
			
		},
		changeLayer : function(){
			var layersSelected = $("#layersSelect").val();
			if(layersSelected==null){
				layersSelected=[];
			}
			
			var url='';
			
			$.each(baseLayers, function(k,v){
				if(v.identification == $("#baseLayers").val()){
					url = v.url;
				}
			});
			
			initMap= "initialBaseMap('"+$("#baseLayers").val()+"',accessTokenMapbox, '"+url+"')\n";
			var layersTypes = viewerCreateJson.layersTypes;
			$.get('/controlpanel/static/js/baseviewer.txt',  function(data){
				
				  data += initMap;
				  setLayers = [];
				  setHeatLayers = [];
				  $.each(layersSelected, function(index, layerAux){
					  if(layersTypes[layerAux] == 'iot' ){
							$.ajax({
								url : "/controlpanel/viewers/getLayerData/" + layerAux,
								type : 'GET',
								dataType: 'text', 
								contentType: 'text/plain',
								mimeType: 'text/plain',
								async : false,
								success : function(data) {
									
									setLayers.push(data);
									
								},
								error : function(data, status, er) {
									$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
								}
							});
								
					  }else if(layersTypes[layerAux] == 'heat' ){
							$.ajax({
								url : "/controlpanel/viewers/getLayerData/" + layerAux,
								type : 'GET',
								dataType: 'text', 
								contentType: 'text/plain',
								mimeType: 'text/plain',
								async : false,
								success : function(data) {
									
									setHeatLayers.push(data);
									
								},
								error : function(data, status, er) {
									$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
								}
							});
								
					  }else if(layersTypes[layerAux] == 'wms' ){
						  $.ajax({
								url : "/controlpanel/viewers/getLayerWms/" + layerAux,
								type : 'GET',
								dataType: 'text', 
								contentType: 'text/plain',
								mimeType: 'text/plain',
								async : false,
								success : function(result) {
									result = JSON.parse(result);
									var urlLayer = result['url'];
									var layerWms = result['layerWms'];
									 data += "loadWms('"+urlLayer+"', '"+layerWms+"', '"+ layerAux +"')\n";
									
								},
								error : function(data, status, er) {
									$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
								}
							});
						  
						 
					  }else if(layersTypes[layerAux] == 'kml' ){
						  $.ajax({
								url : "/controlpanel/viewers/getLayerKml/" + layerAux,
								type : 'GET',
								dataType: 'text', 
								contentType: 'text/plain',
								mimeType: 'text/plain',
								async : false,
								success : function(result) {
									result = JSON.parse(result);
									var urlLayer = result['url'];
									 data += "loadKml('"+urlLayer+"', '"+ layerAux +"')\n";
									
								},
								error : function(data, status, er) {
									$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
								}
							});
						  
						 
					  }
				  })
				  
				  	$.each(setLayers, function(k,v){
						data += "createLayer("+v+")\n";
					})
					
					$.each(setHeatLayers, function(k,v){
						data += "heatmapGenerator("+v+")\n";
					})
					htmlEditor.setValue(data);
					ViewerCreateController.run();
		          
		    });
			
			
		}

	}
}();


// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	ViewerCreateController.load(viewerCreateJson);
	
	ViewerCreateController.init();
	
});


