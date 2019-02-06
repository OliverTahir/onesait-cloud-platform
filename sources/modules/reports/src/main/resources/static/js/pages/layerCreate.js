
var LayerCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Onesait Platform Control Panel'; 
	var LIB_TITLE = 'Layer Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var hasId = false; // instance
	var attributesArray = [];
	var fieldsArray = [];
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
	
	$("#isHeatMap").on("click", function(){
		if($("#isHeatMap").is(":checked")){
			$("#tab-symbology").addClass('disabledTab');
			$("#tab-infobox").addClass('disabledTab');
			$("#heatMapDiv").show();
			$.ajax({
				url: '/controlpanel/layers/getOntologyFields',
				type:"POST",
				async: true,
				data: { 'ontologyIdentification': $("#ontology").val()},
				dataType:"json",
				success: function(response,status){
					$("#weightField").empty();
					var fields = response;
					$("#weightField").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
					for (var key in fields){
				        var field = key;
				        var type = fields[key];
				        
				        $("#weightField").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
				    }
				}
			});
		}else{
			$("#tab-symbology").removeClass('disabledTab')
			$("#tab-infobox").removeClass('disabledTab');
			$("#heatMapDiv").hide();
		}
	});
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// DELETE LAYER
	var deleteLayer = function(layerId){
		console.log('deleteLayerConfirmation() -> formId: '+ layerId);

		// no Id no fun!
		if ( !layerId ) {$.alert({title: 'ERROR!',  theme: 'light', content: ontologyCreateReg.validations.validform}); return false; }
		
		$.ajax({
			url : "/controlpanel/layers/isLayerInUse/" + layerId,
			type : 'GET',
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			async : false,
			success : function(isLayerInUse) {
				
				if(isLayerInUse){
					logControl ? console.log('deleteLayerConfirmation() -> formAction: ' + $('.delete-layer').attr('action') + ' ID: ' + $('#delete-layerId').attr('layerId')) : '';

					// call ontology Confirm at header.
					HeaderController.showConfirmDialogLayer('delete_layer_form');
				}else{
					$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: layerCreateJson.deleteError});
				}
				
			},
			error : function(data, status, er) {
				$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: er}); 
			}
		});

		
	}
	
	var handleValidation =  function() {
        // for more info visit the official plugin documentation:
        // http://docs.jquery.com/Plugins/Validation

        var form1 = $('#layer_create_form');
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
				fields:		{ required: true },
				types:		{ required: true },
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
	   			 $.each($("#attributes tbody tr"), function(k,v){
	   			     var field=$(v).find('td')[0].innerHTML;
	   				 var attribute=$(v).find('td')[1].innerHTML;
	   				 infoBox.push({"field":field, "attribute": attribute});
	   		        
	   		     });
	   			
	   			 $("<input type='hidden' name='infoBox' value='"+JSON.stringify(infoBox)+"' />")
   		         .appendTo("#layer_create_form");
	   			 
	   			$("<input type='hidden' name='isPublic' value='"+$("#public").is(":checked")+"' />")
  		         .appendTo("#layer_create_form");
	   			
	   			$("<input type='hidden' name='isHeatMap' value='"+$("#isHeatMap").is(":checked")+"' />")
 		         .appendTo("#layer_create_form");
	   			 
				// form.submit();
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
			return layerCreateJson = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
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
			
			$("#addAttribute").on("click", function(){
				LayerCreateController.showAttributeDialog();
			});
			
			// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
			if ( layerCreateJson.actionMode === null){
				logControl ? console.log('|---> Action-mode: INSERT') : '';
				$('.formcolorpicker').each(function () {
				    $(this).colorpicker({
			            color: null
			        });
				});
				
			
			}
			// EDIT MODE ACTION 
			else {	
				logControl ? console.log('|---> Action-mode: UPDATE') : '';
				
				if(layerCreateJson.isPublic){
					$("#public").attr("checked", "checked");
				}
				
				if(layerCreateJson.isHeatMap){
					$("#tab-symbology").addClass('disabledTab')
					$("#tab-infobox").addClass('disabledTab')
					$("#heatMapDiv").show();
					$("#checkHeatMap").show();
					$("#isHeatMap").attr("checked", "checked");
					spinnerEachFrom = $("#min").TouchSpin({
						min: 0,
						max: 999.0,
						stepinterval: 0.2,
						maxboostedstep: 999.0,
						verticalbuttons: true
					});			
					
					($("#min").val() == "") ? $("#min").val(parseInt(layerCreateJson.heatMapMin)) : null;		
					spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
					
					spinnerEachFrom = $("#max").TouchSpin({
						min: 0.0,
						max: 999.0,
						stepinterval: 0.2,
						maxboostedstep: 999,
						verticalbuttons: true
					});			
					
					($("#max").val() == "") ? $("#max").val(parseInt(layerCreateJson.heatMapMax)) : null;		
					spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
					
					spinnerEachFrom = $("#radius").TouchSpin({
						min: 0.0,
						max: 9999.0,
						stepinterval: 0.2,
						maxboostedstep: 9999,
						verticalbuttons: true,
						postfix: 'px'
					});			
					
					($("#radius").val() == "") ? $("#radius").val(parseInt(layerCreateJson.heatMapRadius)) : null;		
					spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
					
					$.ajax({
						url: '/controlpanel/layers/getOntologyFields',
						type:"POST",
						async: true,
						data: { 'ontologyIdentification': $("#ontology").val()},
						dataType:"json",
						success: function(response,status){
							$("#weightField").empty();
							var fields = response;
							$("#weightField").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
							for (var key in fields){
						        var field = key;
						        var type = fields[key];
						        
						        $("#weightField").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
						    }
							$("#weightField").val(layerCreateJson.weightField);
						}
					});
					
				}
				
				spinnerEachFrom = $("#inner_thinckness").TouchSpin({
					min: 0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999.0,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#inner_thinckness").val() == "") ? $("#inner_thinckness").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#outer_thinckness").TouchSpin({
					min: 0.0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#outer_thinckness").val() == "") ? $("#outer_thinckness").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#size").TouchSpin({
					min: 0.0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#size").val() == "") ? $("#size").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				$("#geometryTypes").css('visibility', 'visible');
				$("#identification").attr("disabled", "disabled");
				
			    $("#innerColor").colorpicker({
		            color: layerCreateJson.innerColor
		        });
			    
			    $("#outerColor").colorpicker({
		            color: layerCreateJson.outerColor
		        });
			
				
				$.ajax({
					url: '/controlpanel/layers/getOntologyGeometryFields',
					type:"POST",
					async: true,
					data: { 'ontologyIdentification': $("#ontology").val()},
					dataType:"json",
					success: function(response,status){
						$("#fields").empty();
						var fields = response;
						$("#fields").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
						for (var key in fields){
					        var field = key;
					        var type = fields[key];
					        $("#fields").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
					    }
						
						$("#fields").val(layerCreateJson.geometryField);

					}
				});
				
				
				$("#types").removeAttr("disabled");
				$("#lon_lat").removeAttr("disabled");
				
				var infoB = JSON.parse(layerCreateJson.infobox);
				
				$.each(infoB, function(k,v){
					var field = v.field;
	            	var attribute = v.attribute;
	            	
	            	attributesArray.push(attribute);
	            	fieldsArray.push(field);
	            	
	            	LayerCreateController.checkAttribute(attribute, field);
				});
			}
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		deleteAttribute: function(obj){
			
			var field = $(obj).closest('tr').find(".field").text();
			var attribute = $(obj).closest('tr').find(".attribute").text();
			var index = attributesArray.indexOf(attribute);
			if (index > -1) {
				attributesArray.splice(index, 1);
			}
			var index = fieldsArray.indexOf(field);
			if (index > -1) {
				fieldsArray.splice(index, 1);
			}
			$(obj).closest('tr').remove();
		},
		editAttribute: function(obj){
			var field_edit = $(obj).closest('tr').find(".field").text();
			var attribute_edit = $(obj).closest('tr').find(".attribute").text();
			$.confirm({
				async: false,
			    title: layerCreateJson.newattribute,
			    content: '' +
			    '<form action="" class="formName">' +
			    '<div class="form-group col-md-12" id="parameter_info">' +
			    '<label>' + layerCreateJson.field + '</label> <select id="fields_pop" class="form-control" data-width="100%">'+
				'</select>'+
				'<label>'+ layerCreateJson.attribute +'</label> <input type="text" name="field[]" id="attribute" value="" class="form-control"/></div>' +
			    '</form>',
			    buttons: {
			        formSubmit: {
			            text: 'OK',
			            btnClass: 'btn-blue',
			            action: function () {
			            	var field = $("#fields_pop").val();
			            	var attribute = $("#attribute").val();
			            	
			            	if(field_edit!=field){
			            		var index = fieldsArray.indexOf(field_edit);
			            		if (index > -1) {
			            			fieldsArray.splice(index, 1);
			            		}
			            		fieldsArray.push(field);
			            		
			            	}
			            	if(attribute_edit!=attribute){
			            		var index = attributesArray.indexOf(attribute_edit);
			            		if (index > -1) {
			            			attributesArray.splice(index, 1);
			            		}
			            		attributesArray.push(attribute);
			            		
			            	}
			            	
			            	$(obj).closest('tr').remove();
			            	LayerCreateController.checkAttribute(attribute, field);
			            }
			            	
			        },
			        cancel: function () {
			        },
			    },
			    onContentReady: function () {
			    	$.ajax({
						url: '/controlpanel/layers/getOntologyFields',
						type:"POST",
						async: true,
						data: { 'ontologyIdentification': $("#ontology").val()},
						dataType:"json",
						success: function(response,status){
							$("#fields_pop").empty();
							var fields = response;
							$("#fields_pop").append('<option value="select">'+layerCreateJson.layerselect+'</option>');
							for (var key in fields){
						        var field = key;
						        var type = fields[key];
						        
						        $("#fields_pop").append('<option value="'+ field +'">' + field +'</option>');
						    }
							$("#fields_pop").val(field_edit);
							$("#attribute").val(attribute_edit);
						}
					});
			        
			    }
			});
		},
		
		changeOntology: function(){
			
			spinnerEachFrom = $("#inner_thinckness").TouchSpin({
				min: 0,
				max: 999.0,
				stepinterval: 0.2,
				maxboostedstep: 999.0,
				verticalbuttons: true,
				postfix: 'px'
			});			
			
			($("#inner_thinckness").val() == "") ? $("#inner_thinckness").val(0.0) : null;		
			spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			
			spinnerEachFrom = $("#outer_thinckness").TouchSpin({
				min: 0.0,
				max: 999.0,
				stepinterval: 0.2,
				maxboostedstep: 999,
				verticalbuttons: true,
				postfix: 'px'
			});			
			
			($("#outer_thinckness").val() == "") ? $("#outer_thinckness").val(0.0) : null;		
			spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			
			spinnerEachFrom = $("#size").TouchSpin({
				min: 0.0,
				max: 999.0,
				stepinterval: 0.2,
				maxboostedstep: 999,
				verticalbuttons: true,
				postfix: 'px'
			});			
			
			($("#size").val() == "") ? $("#size").val(0.0) : null;		
			spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			
			$.ajax({
				url: '/controlpanel/layers/getOntologyGeometryFields',
				type:"POST",
				async: true,
				data: { 'ontologyIdentification': $("#ontology").val()},
				dataType:"json",
				success: function(response,status){
					$("#fields").empty();
					
					var fields = response;
					$("#fields").append('<option value="select">'+layerCreateJson.layerselect+'</option>');
					for (var key in fields){
				        var field = key;
				        var type = fields[key];
				        
				        if(type=='geometry'){
				        	$("#fields").append('<option name="'+type+'" id="'+field+'" value="'+ field +'">' + field +'</option>');
				        	$("#fields").val(field);
				        }
				        
				        $("#fields").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
				    }
					
					$("#geometryTypes").css('visibility', 'visible');

				}
			});
		},
		deleteLayer: function(layerId){
			deleteLayer(layerId);
		},
		
		checkAttribute: function (attribute, field){
			areUniqueAttribute = attributesArray.unique();
			areUniqueField = fieldsArray.unique();
			if (attributesArray.length !== areUniqueAttribute.length)  { 
				var index = attributesArray.indexOf(attribute);
	    		if (index > -1) {
	    			attributesArray.splice(index, 1);
	    		}
				$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: layerCreateJson.validations.duplicates});
				
				return;
			}
			if (fieldsArray.length !== areUniqueField.length)  { 
				var index = fieldsArray.indexOf(field);
	    		if (index > -1) {
	    			fieldsArray.splice(index, 1);
	    		}
				$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: layerCreateJson.validations.duplicates});
				
				return;
			}
			var add= "<tr id='"+attribute+"'><td class='text-left no-wrap field'>"+field+"</td><td class='text-left no-wrap attribute'>"+attribute+"</td><td class='icon text-center' style='white-space: nowrap'><div class='grupo-iconos'><span class='btn btn-xs btn-no-border btn-circle btn-outline blue tooltips' onclick='LayerCreateController.editAttribute(this)' data-container='body' data-placement='bottom' id='edit_"+ attribute +" th:text='#{gen.edit}'><i class='la la-edit font-hg'></i></span><span class='btn btn-xs btn-no-border btn-circle btn-outline blue tooltips' onclick='LayerCreateController.deleteAttribute(this)' th:text='#{gen.deleteBtn}'><i class='la la-trash font-hg'></i></span></div></div></div></td></tr>";
	    	$("#attributes tbody").append(add);
			return true;
		},
		changeField: function (){
			var field = $("#fields").val();
			var type = $("#"+field).attr("name");
			$("#geometryType").val(type);
			if(type=="Point"){
				$("#checkHeatMap").show();
				spinnerEachFrom = $("#min").TouchSpin({
					min: 0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999.0,
					verticalbuttons: true
				});			
				
				($("#min").val() == "") ? $("#min").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#max").TouchSpin({
					min: 0.0,
					max: 999.0,
					stepinterval: 0.2,
					maxboostedstep: 999,
					verticalbuttons: true
				});			
				
				($("#max").val() == "") ? $("#max").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
				
				spinnerEachFrom = $("#radius").TouchSpin({
					min: 0.0,
					max: 9999.0,
					stepinterval: 0.2,
					maxboostedstep: 9999,
					verticalbuttons: true,
					postfix: 'px'
				});			
				
				($("#radius").val() == "") ? $("#radius").val(0.0) : null;		
				spinnerEachFrom.bind("keydown", function (event) { event.preventDefault(); });
			}else{
				$("#checkHeatMap").hide();
			}
		},
		

		showAttributeDialog: function (editMode){
			$.confirm({
				async: false,
			    title: layerCreateJson.newattribute,
			    content: '' +
			    '<form action="" class="formName">' +
			    '<div class="form-group col-md-12" id="parameter_info">' +
			    '<label>' + layerCreateJson.field + '</label> <select id="fields_pop" class="form-control" data-width="100%">'+
				'</select>'+
				'<label>'+ layerCreateJson.attribute +'</label> <input type="text" name="field[]" id="attribute" value="" class="form-control"/></div>' +
			    '</form>',
			    buttons: {
			        formSubmit: {
			            text: 'OK',
			            btnClass: 'btn-blue',
			            action: function () {
			            	var field = $("#fields_pop").val();
			            	var attribute = $("#attribute").val();
			            	
			            	attributesArray.push(attribute);
			            	fieldsArray.push(field);
			            	
			            	LayerCreateController.checkAttribute(attribute, field);
			            		

			            }
			            	
			        },
			        cancel: function () {
			        },
			    },
			    onContentReady: function () {
			    	$.ajax({
						url: '/controlpanel/layers/getOntologyFields',
						type:"POST",
						async: true,
						data: { 'ontologyIdentification': $("#ontology").val()},
						dataType:"json",
						success: function(response,status){
							$("#fields_pop").empty();
							var fields = response;
							$("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="select">'+layerCreateJson.layerselect+'</option>');
							for (var key in fields){
						        var field = key;
						        var type = fields[key];
						        
						        $("#fields_pop").append('<option id="'+field+'" name="'+type+'" value="'+ field +'">' + field +'</option>');
						    }
						}
					});
			        
			    }
			});
		}
	}
}();


// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	LayerCreateController.load(layerCreateJson);
	
	LayerCreateController.init();
});
