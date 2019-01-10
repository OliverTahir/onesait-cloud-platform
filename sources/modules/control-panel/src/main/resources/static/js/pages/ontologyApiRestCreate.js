var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations responses.
	
var OntologyCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Sofia4Cities Control Panel'; 
	var LIB_TITLE = 'Ontology Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validTypes = ["object","string","number","integer","date","timestamp","array","geometry","file","boolean"]; // Valid property types	
	var mountableModel3 = $('#pathsParams').find('tr.mountable-model')[0].outerHTML;
	var mountableModel4 = $('#queriesParams').find('tr.mountable-model')[0].outerHTML;
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance
	var nextIndex=0;
	var jsonPathParams = [{'indexes': 0, 'namesPaths':''}];
	var jsonQueryParams = [];
	var operationsNames = [];
	var pathParamNames = [];
	var queryParamNames = [];
	var isExternal = false;
	var emptyBaseId='';
	var headersNames = [];

	
	// CONTROLLER PRIVATE FUNCTIONS	--------------------------------------------------------------------------------
	

	$('#pathsParams').mounTable(jsonPathParams,{
		model: '.mountable-model',
		noDebug: false,
		addLine:{				
			button: "#addPathParamOperation",					
			onClick: function (element){
				
				console.log('PathParam added!');
				return true;
			}
		}
			
	});
	
	$('#pathsParams tbody').on("DOMSubtreeModified", function(){
		var inputs = $("input[name='indexes\\[\\]']");
		$.each(inputs, function(i, item){
			inputs[i].value = i;
		});
	});
	
	$('#queriesParams').mounTable(jsonQueryParams,{
		model: '.mountable-model',
		noDebug: false,
		addLine:{				
			button: "#addQueryParamOperation",					
			onClick: function (element){
				console.log('PathParam added!');				
				return true;
			}
		}			
	});
	
	
	$('input[type=radio][name=auth]').change(function() {
        if (this.value == 'apiKey') {
        	$("#authMethod").val('apiKey');
           $("#header").removeAttr("disabled");
           $("#token").removeAttr("disabled");
           $("#oauthUser").attr("disabled","disabled");
           $("#oauthPass").attr("disabled","disabled");
           $("#basicUser").attr("disabled","disabled");
           $("#basicPass").attr("disabled","disabled");
           $("#oauthUser").val("");
           $("#oauthPass").val("");
           $("#basicUser").val("");
           $("#basicPass").val("");
        }else if (this.value == 'oauth') {
        	$("#authMethod").val('oauth');
        	$("#header").attr("disabled","disabled");;
            $("#token").attr("disabled","disabled");
            $("#oauthUser").removeAttr("disabled");
            $("#oauthPass").removeAttr("disabled");
            $("#basicUser").attr("disabled","disabled");
            $("#basicPass").attr("disabled","disabled");
            $("#header").val("");
            $("#token").val("");
            $("#basicUser").val("");
            $("#basicPass").val("");
        }else if (this.value == 'basic') {
        	$("#authMethod").val('basic');
        	$("#header").attr("disabled","disabled");
            $("#token").attr("disabled","disabled");
            $("#oauthUser").attr("disabled","disabled");
            $("#oauthPass").attr("disabled","disabled");
            $("#basicUser").removeAttr("disabled");
            $("#basicPass").removeAttr("disabled");
            $("#header").val("");
            $("#token").val("");
            $("#oauthPass").val("");
            $("#oauthUser").val("");
        }
    });
	
	$("#auth").on("click", function(){
		if($("#auth").is(":checked")){
			$("#authenticationOptions").show();
		}else{
			$("#authenticationOptions").hide();
		}
	});
	
	
	$('#addOperation').on('click', function(){
		event.preventDefault();
		logControl ? console.log('AddPathParamOperation() -> ') : '';
	
		var name = $("#nameOperation").val();
		var typeOperation = $("#typeOperation").val();
		var description = $("#descriptionOperation").val();
		
		if(name=="" || name==null || name==undefined){
			$.alert({title: 'ERROR!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.operation.name});
			return;
		}
		
		if(description=="" || description==null || description==undefined){
			$.alert({title: 'ERROR!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.operation.desc});
			return;
		}
		
		operationsNames.push(name);
		
		checkUnique = operationsNames.unique();
		if (operationsNames.length !== checkUnique.length)  { $.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.duplicates}); return false; } 
		
		var origin = ontologyCreateReg.manual;
		$("#operationsList tbody").append("<tr id='operation_"+name+"'></tr>");
		$("#operation_"+name).append("<td class='' value='" + name + "' id='" + name + "'>" + name +"</td>");
		$("#operation_"+name).append("<td class='' value='" + typeOperation + "' id='type_" + name + "'>" + typeOperation +"</td>");
		$("#operation_"+name).append("<td class='text-center' value='" + description + "' id='des_" + name + "'>" + description +"</td>");
		$("#operation_"+name).append("<td class='text-center' value='" + origin + "' id='" + origin + "'>" + origin +"</td>");
		
		var indexes = $("input[name='indexes\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();				
		var namesPath = $("input[name='namesPaths\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
		
		var json = [];
		if ( namesPath.length ){	
			$.each(namesPath, function( index, value ) {
				nameIndex = namesPath.indexOf(value);
				json.push({'indexes': indexes[nameIndex], 'namesPaths':value});
				
			});			
		}
		
		$("#operation_"+name).append("<td class='text-center hide' value=" + JSON.stringify(json)  +" id='pathParams_" + name +"'>" + JSON.stringify(json)  +"</td>");
		
		var namesQuery = $("input[name='namesQueries\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
		
		var json=[];
		var isFirst= true;
		
		if ( namesQuery.length ){	
			$.each(namesQuery, function( index, value ) {
				nameIndex = namesQuery.indexOf(value);
				json.push({'namesQueries':value});
				
			});			
		}
		
		$("#operation_"+name).append("<td class='text-center hide' value=" + JSON.stringify(json) +" id='queryParams_" + name +"'>" + JSON.stringify(json)  +"</td>");	
		$("#operation_"+name).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span  class='btn btn-sm blue-sharp sbold tooltips' data-container='body' data-placement='bottom' onclick='OntologyCreateController.showOperation(\""+name+"\")'><i class='fa fa-eye'></i></span><span  class='btn btn-sm btn-warning sbold tooltips' onclick='OntologyCreateController.editOperation(\""+name+"\")'><i class='fa fa-edit'></i></span> <span class='btn btn-sm btn-danger sbold tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteOperation(\""+name+"\")'><i class='fa fa-trash'></i></span></div></td>");
		$("#operations_div").show();
		
		$("#nameOperation").val("");
		$("#typeOperation").val("get");
		$("#descriptionOperation").val("");
		$("#pathsParams tbody tr").remove();
		$("#queriesParams tbody tr").remove();
		
	});
	
	$('#addHeaderBtn').on('click', function(){
		event.preventDefault();
		logControl ? console.log('addHeaderBtn() -> ') : '';
	
		var key = $("#headerKey").val();
		var value = $("#headerValue").val();
		
		if(key=="" || key==null || key==undefined){
			$.alert({title: 'ERROR!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.header.key});
			return;
		}
		
		if(value=="" || value==null || value==undefined){
			$.alert({title: 'ERROR!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.header.key});
			return;
		}
		
		headersNames.push(key);
		
		checkUnique = headersNames.unique();
		if (headersNames.length !== checkUnique.length)  { $.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.duplicates}); return false; } 
		
		$("#headersList tbody").append("<tr id='header_"+key+"'></tr>");
		$("#header_"+key).append("<td class='' value='" + key + "' id='" + key + "'>" + key +"</td>");
		$("#header_"+key).append("<td class='' value='" + value + "' id='value_" + key + "'>" + value +"</td>");
		
		$("#header_"+key).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span class='btn btn-sm btn-danger sbold tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteHeader(\""+key+"\")'><i class='fa fa-trash'></i></span></div></td>");
		$("#headers_div").show();
		
		$("#headerKey").val("");
		$("#headerValue").val("");
		
	});
	

	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#ontology_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
					
		// set current language
		currentLanguage = ontologyCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {},
			// validation rules
            rules: {
				ontologyId:		{ minlength: 5, required: true },
                identification:	{ minlength: 5, required: true },
				description:	{ minlength: 5, required: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
                success1.hide();
                error1.show();
                App.scrollTo(error1, -200);
            },
            errorPlacement: function(error, element) {				
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }				
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error'); 
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
               
                error1.hide();
				// VALIDATE JSON SCHEMA 
               
                var postOperations = [];
            	var json =[];
            	var operations = $("#operationsList tbody tr");
            	$.each(operations, function(i,item){
            		
            		if(i!=0){
            			var ops = $("#"+item.id + " td");
            			
            			json.push({'name': ops[0].innerHTML , 'type': ops[1].innerHTML, 'description' : ops[2].innerHTML, 'origin' : ops[3].innerHTML, 'pathParams' : JSON.parse(ops[4].innerHTML) , 'queryParams' : JSON.parse(ops[5].innerHTML)});
            			if(ops[1].innerHTML == "post"){
            				postOperations.push(ops[1].innerHTML);
            			}
            		}
 			       
       		        
       		     });
            	
            	 $("<input type='hidden' value='"+JSON.stringify(json)+"' />")
   		         	.attr("name", "operations")
   		         	.appendTo("#ontology_create_form");
            	 
            	var json =[];
             	var headers = $("#headersList tbody tr");
             	$.each(headers, function(i,item){
             		
             		if(i!=0){
             			var header = $("#"+item.id + " td");
             			
             			json.push({'key': header[0].innerHTML , 'value': header[1].innerHTML});
             			
             		}
  			       
       		        
       		     });
             	
             	 $("<input type='hidden' value='"+JSON.stringify(json)+"' />")
    		         	.attr("name", "headers")
    		         	.appendTo("#ontology_create_form");
             	
             	 if(postOperations.length>0 && editorRest.getText() == "{}"){
             		$.alert({title: 'ERROR!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.schema});
        			return;
             	 }else{
             		if(IsJsonString(editorRest.getText())){
                 		$("#schema").val(editorRest.getText());
                 	}
             	 }
             	
            	 
            	// VALIDATE TAGSINPUT
				validMetaInf = validateTagsInput();
				if (validMetaInf) {
					form.submit();					
				}
			}
        });
    }
	
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() ->  resetForm,  currentLanguage: ' + currentLanguage) : '';
		
		// tagsinput validate fix when handleValidation()
		$('#metainf').on('itemAdded', function(event) {
			
			if ($(this).val() !== ''){ $('#metainferror').addClass('hide');}
		});
		
		// authorization tab control 
		$(".nav-tabs a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!', type: 'blue' , theme: 'light', content: ontologyCreateReg.validations.authinsert});
			return false;
		  }
		});
				
		
		// 	INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('ontology_create_form');
		});

		// UPDATE TITLE AND DESCRIPTION IF CHANGED 
		$('#identification').on('change', function(){
			var jsonFromEditor = {};
			var datamodelLoaded = $('#datamodel_properties').attr('data-loaded');
			if (datamodelLoaded){			
				if (IsJsonString(editor.getText())){				
					jsonFromEditor = editor.get();
					jsonFromEditor["title"] = $(this).val();
					editor.set(jsonFromEditor);
				}			
			}		
		});
	
		$('#description').on('change', function(){
			var jsonFromEditor = {};
			var datamodelLoaded = $('#datamodel_properties').attr('data-loaded');
			if (datamodelLoaded){			
				if (IsJsonString(editor.getText())){				
					jsonFromEditor = editor.get();
					jsonFromEditor["description"] = $(this).val();
					editor.set(jsonFromEditor);
				}			
			}	
			
		});
		
		// INSERT MODE ACTIONS  (ontologyCreateReg.actionMode = NULL ) 
		if ( ontologyCreateReg.actionMode === null){
			logControl ? console.log('|---> Action-mode: INSERT') : '';
			
			// Set active 
			$('#active').trigger('click');
			
			// Set Public 
			$('#public').trigger('click');
		}
		// EDIT MODE ACTION 
		else {	
			logControl ? console.log('|---> Action-mode: UPDATE') : '';
			
			// if ontology has authorizations we load it!.
			authorizationsJson = ontologyCreateReg.authorizations;			
			if (authorizationsJson.length > 0 ){
				
				// MOUNTING AUTHORIZATIONS ARRAY
				var authid_update, accesstype_update , userid_update , authorizationUpdate , authorizationIdUpdate = '';
				$.each( authorizationsJson, function (key, object){			
					
					authid_update 		= object.id; 
					accesstype_update 	= object.typeName; 
					userid_update 		= object.userId;					
					
					logControl ? console.log('      |----- authorizations object on Update, ID: ' +  authid_update + ' TYPE: ' +  accesstype_update + ' USER: ' +  userid_update  ) : '';
					
					// AUTHs-table {"users":user,"accesstypes":accesstype,"id": response.id}
					authorizationUpdate = {"users": userid_update, "accesstypes": accesstype_update, "id": authid_update};					
					authorizationsArr.push(authorizationUpdate);
					
					// AUTH-Ids {[user_id]:auth_id}
					authorizationIdUpdate = {[userid_update]:authid_update};
					authorizationsIds.push(authorizationIdUpdate);
					
					// disable this users on users select
					$("#users option[value=" + userid_update + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');
					
				});

				// TO-HTML
				if ($('#authorizations').attr('data-loaded') === 'true'){
					$('#ontology_autthorizations > tbody').html("");
					$('#ontology_autthorizations > tbody').append(mountableModel2);
				}
				logControl ? console.log('authorizationsArr on UPDATE: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr)) : '';
				$('#ontology_autthorizations').mounTable(authorizationsArr,{
					model: '.authorization-model',
					noDebug: false							
				});
				
				// hide info , disable user and show table
				$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));					
				$('#authorizations').removeClass('hide');
				$('#authorizations').attr('data-loaded',true);// TO-HTML
				$("#users").selectpicker('deselectAll');
				
			}
			
			//OntologyRest
			var ontologyRest = ontologyCreateReg.ontologyRest;
			if(ontologyRest.baseUrl!=null || ontologyRest.baseUrl!=undefined || ontologyRest.baseUrl!=''){
				$("#urlBase").val(ontologyRest.baseUrl);
				var securityType = ontologyCreateReg.ontologyRest.securityType;
				if(securityType!='none'){
					$("#auth").trigger("click");
					var security = ontologyCreateReg.ontologyRest.security;
					if(securityType=='ApiKey'){
						var json = JSON.parse(security);
						$("#header").val(json.header);
						$("#token").val(json.token);
						$("#apiKey").trigger("click");
					}else if(securityType=='Basic'){
						var json = JSON.parse(security);
						$("#basicUser").val(json.user);
						$("#basicPass").val(json.password);
						$("#basic").trigger("click");
					}else if(securityType=='OAuth'){
						var json = JSON.parse(security);
						$("#oauthUser").val(json.user);
						$("#oauthPass").val(json.password);
						$("#oauth").trigger("click");
					}
				}
				
				var headers = ontologyCreateReg.ontologyRest.headers;
				var jsonHeaders = JSON.parse(headers);
				
				for(var i=0; i<jsonHeaders.length; i++){
					var json = jsonHeaders[i];
					
					$("#headersList tbody").append("<tr id='header_"+json.key+"'></tr>");
					$("#header_"+json.key).append("<td class='' value='" + json.key + "' id='" + json.key + "'>" + json.key +"</td>");
					$("#header_"+json.key).append("<td class='' value='" + json.value + "' id='value_" + json.key + "'>" + json.value +"</td>");
					
					$("#header_"+json.key).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span class='btn btn-sm btn-danger sbold tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteHeader(\""+json.key+"\")'><i class='fa fa-trash'></i></span></div></td>");
					$("#headers_div").show();
					
					headersNames.push(json.key);
				}
				
				if(ontologyCreateReg.ontologyRest.infer){
					$("#infer").trigger("click");
				}
				
				var operations = ontologyCreateReg.ontologyRest.LOperations;
				for(var i=0; i<operations.length;i++){
					var operation = operations[i];
					
					$("#operationsList tbody").append("<tr id='operation_"+operation.name+"'></tr>");
					$("#operation_"+operation.name).append("<td class='' value='" + operation.name + "' id='" + operation.name + "'>" + operation.name +"</td>");
					$("#operation_"+operation.name).append("<td class='' value='" + operation.type.toLowerCase() + "' id='type_" + operation.name + "'>" + operation.type.toLowerCase() +"</td>");
					$("#operation_"+operation.name).append("<td class='text-center' value='" + operation.description + "' id='des_" + operation.name + "'>" + operation.description +"</td>");
					$("#operation_"+operation.name).append("<td class='text-center' value='" + operation.origin + "' id='" + operation.origin + "'>" + operation.origin +"</td>");
					
					var params = operation.LParams;
					
					var jsonPath = [];
					var jsonQuery = [];
					for(var x=0; x<params.length;x++){
						var param = params[x];
						if(param.type=='PATH'){
							jsonPath.push({'indexes': param.index, 'namesPaths':param.name});
						}else if(param.type=='QUERY'){
							jsonQuery.push({'namesQueries':param.name});
						}
					}
					
					$("#operation_"+operation.name).append("<td class='text-center hide' value=" + JSON.stringify(jsonPath)  +" id='pathParams_" + operation.name +"'>" + JSON.stringify(jsonPath)  +"</td>");
					
					$("#operation_"+operation.name).append("<td class='text-center hide' value=" + JSON.stringify(jsonQuery) +" id='queryParams_" + operation.name +"'>" + JSON.stringify(jsonQuery)  +"</td>");	
					$("#operation_"+operation.name).append("<td class='icon' style='white-space: nowrap'><div class='grupo-iconos'><span  class='btn btn-sm blue-sharp sbold tooltips' data-container='body' data-placement='bottom' onclick='OntologyCreateController.showOperation(\""+operation.name+"\")'><i class='fa fa-eye'></i></span><span  class='btn btn-sm btn-warning sbold tooltips' onclick='OntologyCreateController.editOperation(\""+operation.name+"\")'><i class='fa fa-edit'></i></span> <span class='btn btn-sm btn-danger sbold tooltips' data-container='body' data-placement='bottom' th:title='#{gen.deleteBtn} ' th:data-original-title='#{gen.deleteBtn} ' onclick='OntologyCreateController.deleteOperation(\""+operation.name+"\")'><i class='fa fa-trash'></i></span></div></td>");
					$("#operations_div").show();
					
					operationsNames.push(operation.name);
					
				}
			}
			
			// take schema from ontology and load it
			schema = ontologyCreateReg.ontologyRest.jsonSchema;	
			
			editorRest.set(JSON.parse(schema));
			
			// overwrite datamodel schema with loaded ontology schema generated with this datamodel  template.
			var theSelectedModel = $("h3[data-model='"+ ontologyCreateReg.dataModelEditMode +"']");
			var theSelectedModelType = theSelectedModel.closest('div .panel-collapse').parent().find("a").trigger('click');			
			theSelectedModel.attr('data-schema',schema).trigger('click');
			
		}		
	}	
	
	
	
	// DELETE ONTOLOGY
	var deleteOntologyConfirmation = function(ontologyId){
		console.log('deleteOntologyConfirmation() -> formId: '+ ontologyId);
		
		// no Id no fun!
		if ( !ontologyId ) {$.alert({title: 'ERROR!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.validform}); return false; }
		
		logControl ? console.log('deleteOntologyConfirmation() -> formAction: ' + $('.delete-ontology').attr('action') + ' ID: ' + $('#delete-ontologyId').attr('ontologyId')) : '';
		
		// call ontology Confirm at header. 
		HeaderController.showConfirmDialogOntologia('delete_ontology_form');	
	}

	
	// CREATE EDITOR FOR JSON SCHEMA 
	var createEditor = function(){		
		logControl ? console.log('|--->   createEditor()') : '';
		var containerRest = document.getElementById('jsoneditorRest');	
		var options = {
			mode: 'code',
			theme: 'bootstrap3',
			required_by_default: true,
			modes: ['code', 'text', 'tree', 'view'], // allowed modes
			error: function (err) {
				$.alert({title: 'ERROR!', theme: 'light', style: 'red', content: err.toString()});
				return false;
			},
			onChange: function(){
				
				console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
			}
		};		
		editorRest = new jsoneditor.JSONEditor(containerRest, options, {});	
	}
	
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }
		
		return true;
	}
	
	
	// JSON SCHEMA VALIDATION PROCESS
	var validateJsonSchema = function(){
        logControl ? console.log('|--->   validateJsonSchema()') : ''; 
		
		if(IsJsonString(editor.getText())){
			
			var isValid = true;
		 
			// obtener esquemaOntologiaJson
			var ontologia = JSON.parse(editor.getText());
			
			if((ontologia.properties == undefined && ontologia.required == undefined)){
			
				$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.schemaprop});
				isValid = false;
				return isValid;
				
			}else if( ontologia.properties == undefined && (ontologia.additionalProperties == null || ontologia.additionalProperties == false)){
			
				$.alert({title: 'ERROR JSON SCHEMA!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.schemanoprop});
				isValid = false;
				return isValid;
					
			}else{  
			
				// Situarse en elemento raiz  ontologia.properties (ontologia) o ontologia.datos.properties (datos)
				var nodo;
				
				if(jQuery.isEmptyObject(ontologia.properties)){
					 //esquema sin raiz
					 nodo=ontologia;
				}else{
					for (var property in ontologia.properties){
						
						var data = "";
						//Se comprueba si dispone de un elemento raiz
						if (ontologia.properties[property] && ontologia.properties[property].$ref){
						
							// Se accede al elemento raiz que referencia el obj
							var ref = ontologia.properties[property].$ref;
							ref = ref.substring(ref.indexOf("/")+1, ref.length);
							nodo = ontologia[ref];
							
						} else {
							//esquema sin raiz
							nodo = ontologia;
						}
					}
				}				
				// Plantilla EmptyBase: se permite crear/modificar si se cumple alguna de estas condiciones:
				//a.     Hay al menos un campo (requerido o no requerido)
				//b.     No hay ningún campo (requerido o no requerido) pero tenemos el AditionalProperties = true
				// Resto de casos: Con que haya al menos un campo (da igual que sea requerido o no requerido) o el AditionalProperties = true, se permite crear/actualizar el esquema de la ontología.
				
				// Nodo no tiene valor
				if( (nodo == undefined)){
					   
					 $.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'light', content: 'NO NODE!'});
					  isValid = false;
					  return isValid;
					  
				// Propiedades no definida y additionarProperteis no esta informado a true     
				}else  if(  (nodo.properties ==undefined || jQuery.isEmptyObject(nodo.properties))  && (nodo.additionalProperties == null || nodo.additionalProperties == false)){
					
					$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.noproperties});
					isValid = false;
					return isValid;
				}				
				//Validaciones sobre propiedas y requeridos
				else if(nodo.required!=undefined && (nodo.additionalProperties == null || nodo.additionalProperties == false)) {

					var requiredData = nodo.required.length;
					
					// Si tiene elementos requeridos
					if (requiredData!=null && requiredData>0){
					
						   if(nodo.properties!=null){
								 var propertiesNumber=0;
								 for(var propertyName in nodo.properties) {
									 propertiesNumber++;
								  }
								 if(propertiesNumber==0){
									$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.schemanoprop});
									isValid = true;
								 }
						}
						else{
							$.alert({title: 'JSON SCHEMA !', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.noproperties});
							isValid = false;
							return isValid;
						}			
					}           
				}             
			}
		}
		else {
			// no schema no fun!
			isValid = false;
			$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.noschema});			
			return isValid;
			
		}	
		console.log('JSON SCHEMA VALIDATION: ' + isValid);
		return isValid;
	}	
	
	
	// VALIDATE TAGSINPUT
	var validateTagsInput = function(){		
		if ($('#metainf').val() === '') { $('#metainferror').removeClass('hide').addClass('help-block-error font-red'); return false;  } else { return true;} 
	}


	// AJAX AUTHORIZATION FUNCTIONS
	var authorization = function(action,ontology,user,accesstype,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';	
		var insertURL = '/controlpanel/ontologies/authorization';
		var updateURL = '/controlpanel/ontologies/authorization/update';
		var deleteURL = '/controlpanel/ontologies/authorization/delete';
		var response = {};
		
		if (action === 'insert'){
			console.log('    |---> Inserting... ' + insertURL);
						
			$.ajax({
				url:insertURL,
				type:"POST",
				async: true,
				data: {"accesstype": accesstype, "ontology": ontology,"user": user},			 
				dataType:"json",
				success: function(response,status){							
					
					var propAuth = {"users":user,"accesstypes":accesstype,"id": response.id};
					authorizationsArr.push(propAuth);
					console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
					// store ids for after actions.	inside callback 				
					var user_id = user;
					var auth_id = response.id;
					var AuthId = {[user_id]:auth_id};
					authorizationsIds.push(AuthId);
					console.log('     |---> Auths: ' + authorizationsIds.length + ' data: ' + JSON.stringify(authorizationsIds));
										
					// TO-HTML
					if ($('#authorizations').attr('data-loaded') === 'true'){
						$('#ontology_autthorizations > tbody').html("");
						$('#ontology_autthorizations > tbody').append(mountableModel2);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#ontology_autthorizations').mounTable(authorizationsArr,{
						model: '.authorization-model',
						noDebug: false							
					});
					
					// hide info , disable user and show table
					$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));			
					$("#users").selectpicker('deselectAll');
					$("#users option[value=" + $('#users').val() + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');
					$('#authorizations').removeClass('hide');
					$('#authorizations').attr('data-loaded',true);
					
				}
			});

	
		}
		if (action === 'update'){
			
			$.ajax({url:updateURL, type:"POST", async: true, 
				data: {"id": authorization, "accesstype": accesstype},			 
				dataType:"json",
				success: function(response,status){
							
					var updateIndex = foundIndex(user,'users',authorizationsArr);			
					authorizationsArr[updateIndex]["accesstypes"] = accesstype;
					console.log('ACTUALIZADO: ' + authorizationsArr[updateIndex]["accesstypes"]);
					
					// UPDATING STATUS...
					$(btn).find("i").removeClass('fa fa-spin fa-refresh').addClass('fa fa-edit');
					$(btn).find("span").text('Update');								
				}
			});
			
			
		}
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + user + ' with authId:' + authorization );
			
			$.ajax({url:deleteURL, type:"POST", async: true, 
				data: {"id": authorization},			 
				dataType:"json",
				success: function(response,status){									
					
					// remove object
					var removeIndex = authorizationsIds.map(function(item) { return item[user]; }).indexOf(authorization);			
					authorizationsIds.splice(removeIndex, 1);
					authorizationsArr.splice(removeIndex, 1);
					
					console.log('AuthorizationsIDs: ' + JSON.stringify(authorizationsIds));
					// refresh interface. TO-DO: EL this este fallará					
					if ( response  ){ 
						$(btn).closest('tr').remove();
						$("#users option[value=" + user + "]").prop('disabled', false);						
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));					
							$('#authorizations').addClass('hide');
							
						}
						
					}
					else{ 
						$.alert({title: 'ALERT!', theme: 'light', type: 'orange', content: 'NO RESPONSE!'}); 
					}
				}
			});			
		}	
	};
	
	
	// return position to find authId.
	var foundIndex = function(what,item,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( what === element[item]){ found = index;  console.log("a[" + index + "] = " + element[item] + ' Founded in position: ' + found ); } 
			
		});		
		return found;
	}
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{

		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return ontologyCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';				
			handleValidation();
			createEditor();
			initTemplateElements();
			
			$('#jsonschema').val(ontologyCreateJson.dataModels[0].jsonSchema);
			$('#datamodelid').val(ontologyCreateJson.dataModels[0].id);
			$("#rtdb").val("APIRest");
			
			
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
			
			
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		
		// DELETE ONTOLOGY 
		deleteOntology: function(ontologyId){
			logControl ? console.log(LIB_TITLE + ': deleteOntology()') : '';	
			deleteOntologyConfirmation(ontologyId);			
		},
			
		// JSON SCHEMA VALIDATION
		validateJson: function(){	
			validateJsonSchema();			
		},
		
		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){	
				// UPDATE MODE ONLY AND VALUES on user and accesstype
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){
					
					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
					authorization('insert',ontologyCreateReg.ontologyId,$('#users').val(),$('#accesstypes').val(),'');
								
				} else {  $.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.authuser}); }
			}
		},
		
		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				
				// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();				
				
				var removeIndex = foundIndex(selUser,'users',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[removeIndex][selUser];
				
				console.log('removeAuthorization:' + selAuthorizationId);
				
				authorization('delete',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj );				
			}
		},		
		// UPDATE authorization
		updateAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': updateAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				
				// AJAX UPDATE (ACTION,ONTOLOGYID,USER,ACCESSTYPE,ID) returns object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();
								
				var updateIndex = foundIndex(selUser,'users',authorizationsArr);				
				var selAuthorizationId = authorizationsIds[updateIndex][selUser];				
				
				console.log('updateAuthorization:' + selAuthorizationId);
				
				if (selAccessType !== authorizationsArr[updateIndex]["accesstypes"]){
					
					// UPDATING STATUS...
					$(obj).find("i").removeClass('fa fa-edit').addClass('fa fa-spin fa-refresh');
					$(obj).find("span").text('Updating...');
					
					authorization('update',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj);
				} 
				else { console.log('no hay cambios');}
			}
		},
		
		removePathParam: function(obj){
			logControl ? console.log(LIB_TITLE + ': removePathParam()') : '';
			
			var rempath = $(obj).closest('tr').find("input[name='namesPath\\[\\]']").val();		
			$(obj).closest('tr').remove();
			var inputs = $("input[name='indexes\\[\\]']");
			$.each(inputs, function(i, item){
				inputs[i].value = i;
			});
		},
		removeQueryParam: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeQueryParam()') : '';
			
			var rempath = $(obj).closest('tr').find("input[name='namesQueries\\[\\]']").val();		
			$(obj).closest('tr').remove();
		},
		// CHECK FOR NON DUPLICATE Path Params name
		checkNamePaths: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkNamePaths()') : '';
			var allProperties = $("input[name='namesPaths\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allProperties.unique();
			if (allProperties.length !== areUnique.length)  { 
				$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.duplicates});
				$(obj).val(''); return false;
			} 
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );   
			}
		},
		// CHECK FOR NON DUPLICATE Query Params name
		checkNameQueries: function(obj){
			logControl ? console.log(LIB_TITLE + ': checkNameQueries()') : '';
			var allProperties = $("input[name='namesQueries\\[\\]']").map(function(){return $(this).val();}).get();		
			areUnique = allProperties.unique();
			if (allProperties.length !== areUnique.length)  { 
				$.alert({title: 'ERROR!', theme: 'light', type: 'red', content: ontologyCreateReg.validations.duplicates});
				$(obj).val(''); return false;
			} 
			else {
				$(obj).closest('tr').find('.btn-mountable-remove').attr('data-property', $(obj).val() );   
			}
		},
		showOperation: function(operation){
			
			$("#updateoperationBtn").attr("disabled", "disabled");
			$("#nameOperation").removeAttr("disabled");
			
			var pathParams = $.parseJSON($("#pathParams_" + operation).text());
			var queryParams = $.parseJSON($("#queryParams_" + operation).text());
			$("#nameOperation").val($("#" + operation).text());
			$("#descriptionOperation").val($("#des_" + operation).text());
			$("#typeOperation").val($("#type_" + operation).text());
			
			$('#pathsParams > tbody').html("");
			$('#pathsParams > tbody').append(mountableModel3);
			
			$('#queriesParams > tbody').html("");
			$('#queriesParams > tbody').append(mountableModel4);
			
			$('#pathsParams').mounTable(pathParams,{
				model: '.mountable-model',
				noDebug: false,
				addLine:{				
					button: "#addPathParamOperation",					
					onClick: function (element){
						
						console.log('PathParam added!');
						return true;
					}
				}
					
			});
			
			$('#queriesParams').mounTable(queryParams,{
				model: '.mountable-model',
				noDebug: false,
				addLine:{				
					button: "#addQueryParamOperation",					
					onClick: function (element){
						
						console.log('PathParam added!');
						return true;
					}
				}
					
			});
		},
		editOperation: function(operation){
			
			OntologyCreateController.showOperation(operation);
			$("#nameOperation").attr("disabled", "disabled");
			$("#updateoperationBtn").removeAttr("disabled");
		},
		updateOperation : function(){
			
			var nameOperation = $("#nameOperation").val();
			$("#type_" + nameOperation).val($("#typeOperation").val());
			$("#des_" + nameOperation).val($("#descriptionOperation").val());
			$("#des_" + nameOperation).val($("#descriptionOperation").val());
			$("#type_" + nameOperation).text($("#typeOperation").val());
			$("#des_" + nameOperation).text($("#descriptionOperation").val());
			$("#des_" + nameOperation).text($("#descriptionOperation").val());
			
			if(nameOperation=="" || nameOperation==null || nameOperation==undefined){
				$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.operation.name});
				return;
			}
			
			if($("#descriptionOperation").val()=="" || $("#descriptionOperation").val()==null || $("#descriptionOperation").val()==undefined){
				$.alert({title: 'JSON SCHEMA!', type: 'red' , theme: 'light', content: ontologyCreateReg.validations.operation.desc});
				return;
			}
			
			var indexes = $("input[name='indexes\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();				
			var namesPath = $("input[name='namesPaths\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
			
			var json = [];
			if ( namesPath.length ){	
				$.each(namesPath, function( index, value ) {
					nameIndex = namesPath.indexOf(value);
					json.push({'indexes': indexes[nameIndex], 'namesPaths':value});
					
				});			
			}
			
			$("#pathParams_" + nameOperation).val(JSON.stringify(json))
			$("#pathParams_" + nameOperation).text(JSON.stringify(json))
			
			var namesQuery = $("input[name='namesQueries\\[\\]']").map(function(){ if ($(this).val() !== ''){ return $(this).val(); }}).get();		
			
			var json=[];
			var isFirst= true;
			
			if ( namesQuery.length ){	
				$.each(namesQuery, function( index, value ) {
					nameIndex = namesQuery.indexOf(value);
					json.push({'namesQueries':value});
					
				});			
			}
			
			$("#queryParams_" + nameOperation).val(JSON.stringify(json))
			$("#queryParams_" + nameOperation).text(JSON.stringify(json))
			
			$("#nameOperation").val("");
			$("#typeOperation").val("get");
			$("#descriptionOperation").val("");
			$("#pathsParams tbody tr").remove();
			$("#queriesParams tbody tr").remove();
			
			$("#updateoperationBtn").attr("disabled", "disabled");
			$("#nameOperation").removeAttr("disabled");
		},
		deleteOperation: function(operation){
			
			$("#operation_"+operation).remove();
		},
		editHeader: function(header){
			
			$("#headerKey").val(header);
			$("#headerValue").val($("#value_" + header).text());
			
		},
		deleteHeader: function(header){
			
			$("#header_"+header).remove();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editorRest;
	var aceEditor;
	var schema = ''; // current schema json string var
	
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	OntologyCreateController.load(ontologyCreateJson);	
		
	// AUTO INIT CONTROLLER.
	OntologyCreateController.init();
});
