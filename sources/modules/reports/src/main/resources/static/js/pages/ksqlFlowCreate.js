var KsqlFlowCreateController = function() {
	
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Sofia4Cities Control Panel'; 
	var LIB_TITLE = 'KSQL Flow Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';	
	var validMetaInf = false;
	var hasId = false; // instance
	var ksqlFlowIsAvailable = false;
	var waitingForKsqlFlowValidation = false;
	var updateKsqlResourceId = null;
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		//CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
		
	}
	
	var checkKsqlFlowAvailable = function (identification) {
			
		return $.ajax({ 
			url: '/controlpanel/ksqlflows/check/'+identification,
			type: 'GET',
			async: false,
			dataType: 'text', 
			contentType: 'text/plain',
			mimeType: 'text/plain',
			success: function(exists) { 
			  if(exists=="false") ksqlFlowExist=false;
	      else ksqlFlowExist=true;
			},
			error: function (xhr, textStatus, errorThrown) {
                console.log('a' + textStatus);
                return false;
            }
		});
	}
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#ksql_flow_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
					
		// set current language
		currentLanguage = ksqlFlowCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
            	identification:	{ minlength: 5, required: true }
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
               
            	checkKsqlFlowAvailable($('#identification').val());
   			 	var error1 = $('.alert-danger');
   			 	var success1 = $('.alert-success');
   		     
   			 	if (ksqlFlowExist) {
	   			 	error1.hide();
	                success1.show();
	                form.submit();
   			 	} else{
   			 		//Change style to red
   			 		error1.find('span').text(ksqlFlowCreateReg.dupError);
   			 		$('#identification').closest('.form-group').addClass('has-error'); 
   			 		success1.hide();
   			 		error1.show();
   			 		App.scrollTo(error1, -200);
   			 	}				
			}
        });
    }
	
	// DELETE KSQL FLOW
	var deleteKsqlFlowConfirmation = function(userId){
		console.log('deleteKsqlFlowConfirmation() -> formId: '+ userId);
		
		// no Id no fun!
		if ( !userId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO KSQLFLOW-FORM SELECTED!'}); return false; }
		
		logControl ? console.log('deleteKsqlFlowConfirmation() -> formAction: ' + $('.delete_ksql_flow_form').attr('action') + ' ID: ' + $('.delete_ksql_flow_form').attr('ksqlFlowId')) : '';
		
		// call user Confirm at header.
		HeaderController.showConfirmDialogKsqlFlow('delete_ksql_flow_form');	
	}
	
	// CREATE KSQL  RELATION
	var deleteKsqlRelation= function(ksqlRelationId){
		var navigURL = '/controlpanel/ksql/relation/';
		//Bring data from Modal depending on Resource type
		var postData = getRelationModalData();
	
		$.ajax({
			url:navigURL+ksqlRelationId,
			type:"DELETE",
			async: true,
			success: function(response,status){							
				refreshKsqlRelationsList();
			},
			error: function(request, status, error) {
				$.alert({title: 'ALERT!', theme: 'dark', type: 'orange', content: request.responseText});
			}
			
		});
	}
	
	// DELETE KSQL RELATION
	var deleteKsqlRelationConfirmation = function(ksqlRelationId){
		console.log('deleteKsqlRelationConfirmation() -> formId: '+ ksqlRelationId);
		
		// no Id no fun!
		if ( !ksqlRelationId ) {$.alert({title: 'ERROR!',type: 'red' , theme: 'dark', content: 'NO KSQLRELATION-FORM SELECTED!'}); return false; }
		
		logControl ? console.log('deleteKsqlFlowConfirmation() -> formAction: ' + $('.delete_ksql_relation_form').attr('action') + ' ID: ' + $('.delete_ksql_relation_form').attr('ksqlRelationId')) : '';
		
		$("#delete_ksql_relation_form").attr('action','/controlpanel/ksql/relation/'+ksqlRelationId);
		$("[name=ksqlRelationId]").val(ksqlRelationId);
		// call user Confirm at header.
		HeaderController.showConfirmDialogKsqlRelation(ksqlRelationId, deleteKsqlRelation);	
	}
	
	// CREATE KSQL  RELATION
	var createKsqlRelation= function(){
		var insertURL = '/controlpanel/ksql/relation/create';
		//Bring data from Modal depending on Resource type
		var postData = getRelationModalData();
	
		$.ajax({
			url:insertURL,
			type:"POST",
			async: true,
			data: JSON.stringify(postData),
			contentType: "application/json; charset=utf-8",
			dataType:"json",
			success: function(response,status){							
				closeCreationModal();
				refreshKsqlRelationsList();
			},
			error: function(request, status, error) {
				$.alert({title: 'ALERT!', theme: 'dark', type: 'orange', content: request.responseJSON.msg});
			}
			
		});
	}
	
	var updateKsqlRelationChanges = function (){
		var updateURL = '/controlpanel/ksql/relation/update/'+updateKsqlResourceId;
		//Bring data from Modal depending on Resource type
		var postData = getRelationModalData();
		
		$.ajax({
			url:updateURL,
			type:"PUT",
			async: true,
			data: JSON.stringify(postData),
			contentType: "application/json; charset=utf-8",
			dataType:"json",
			success: function(response,status){							
				closeCreationModal();
				refreshKsqlRelationsList();
			},
			error: function(request, status, error) {
				$.alert({title: 'ALERT!', theme: 'dark', type: 'orange', content: request.responseJSON.msg});
			}
			
		});
	}
	
	var refreshKsqlRelationsList = function(){
		var urlRefresh = window.location.pathname.replace("flow","relation").replace("update/","list?flowId=");
		navigateUrl(urlRefresh); 
	}
	
	var loadKsqlResourceDataToModal = function(id){
		$("#id_relation_id").val(mapKsqlRelation.get(id).id);
		$("#id_relation_description").val(mapKsqlRelation.get(id).description);
		$("#id_relation_statement").val(mapKsqlRelation.get(id).statement);
		var resourceType = mapKsqlRelation.get(id).resourceType.$name;
		$("#id_relation_type").val(resourceType);
		if (resourceType != "PROCESS" && mapKsqlRelation.get(id).ontology != null){
			$("#id_relation_ontology_selector").val(mapKsqlRelation.get(id).ontology.identification);
		}
		updateKsqlResourceId = mapKsqlRelation.get(id).id;
		resourceTypeSwitchCreationModal();
	}
	var enableCreationModal= function(mode, id){
		resourceTypeSwitchCreationModal();
		switch(mode) {
		    case "create":
		        $("#id_relation_type").val("ORIGIN");
		    	resourceTypeSwitchCreationModal();
		    	updateKsqlResourceId = null;
		        $("#modalCreateButton").show();
		        $("#modalEditButton").hide();
		        $("#id_relation_description").val("");
		        $("#id_relation_statement").val("");
		        $("#id_relation_ontology_selector").val(null);
		        $("#id_relation_id").val(null);
		        //Enable all inputs
		        $("#id_relation_type").prop('disabled',false);
		        $("#id_relation_ontology_selector").prop('disabled',false);
		        
		        break;
		    case "view":
		    	$("#modalCreateButton").hide();
		        $("#modalEditButton").hide();
		        loadKsqlResourceDataToModal(id);
		        //Disable every entry
		        $("#id_relation_type").prop('disabled',true);
		        $("#id_relation_ontology_selector").prop('disabled',true);
		        $("#id_relation_description").prop('disabled',true);
		        $("#id_relation_statement").prop('disabled',true);
		        break;
		    case "edit":
		    	$("#modalCreateButton").hide();
		        $("#modalEditButton").show();
		        loadKsqlResourceDataToModal(id);
		        //Disable not editable entries
		        $("#id_relation_type").prop('disabled',true);
		        $("#id_relation_ontology_selector").prop('disabled',true);
		        $("#id_relation_description").prop('disabled',false);
		        break;
		}
		$("#dialog-relation").modal('show');
	}
	
	var closeCreationModal= function(){
		$("#dialog-relation").modal('hide');
	}
	
	// GET RESOURCE/RELATION INFO FROM MODAL
	var getRelationModalData = function(){
		var data = {};
		data.resourceType = $("#id_relation_type").find(":selected").val();
		data.description = $("#id_relation_description").val();
		data.statement = $("#id_relation_statement").val().trim();
		data.ontology = $("#id_relation_ontology_selector").find(":selected").val();
		data.flowId = $("#flowId").val();
		data.resourceId =  $("#id_relation_id").val();
		return data;
	}
	
	var resourceTypeSwitchCreationModal = function() {
		var typeSelection = $("#id_relation_type").find(":selected").val(); 
		$("#id_relation_statement").val("");
		switch(typeSelection) {
			case "ORIGIN":
		        // Disable STATEMENT edit
				$("#id_relation_statement").prop('disabled',true);
				// Visualize Ontology selector
				$("#label_relation_origin_ontology").show();
				$("#label_relation_destination_ontology").hide();
		    	$("#id_relation_ontology_selector").show();
		        break;
		    case "PROCESS":
		        // Enable STATEMENT edit
		    	$("#id_relation_statement").prop('disabled',false);
		    	// Hide Ontology selector
				$("#label_relation_origin_ontology").hide();
				$("#label_relation_destination_ontology").hide();
		    	$("#id_relation_ontology_selector").hide();
		        break;
		    case "DESTINY":
		        // Disable STATEMENT edit
		    	$("#id_relation_statement").prop('disabled',false);
				// Visualize Ontology selector
				$("#label_relation_origin_ontology").hide();
				$("#label_relation_destination_ontology").show();
		    	$("#id_relation_ontology_selector").show();
		        break;
		    default:
		}
	}
	
	return{		

		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return ksqlFlowCreateReg = Data;
		},
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleValidation();
		},
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		// DELETE FLOW 
		deleteKsqlFlow : function(flowId){
			logControl ? console.log(LIB_TITLE + ': deleteKsqlFlow()') : '';	
			deleteKsqlFlowConfirmation(flowId);			
		},
		// DELETE RELATION
		deleteKsqlRelation : function (relationId){
			logControl ? console.log(LIB_TITLE + ': deleteKsqlRelation()') : '';
			deleteKsqlRelationConfirmation(relationId);
		},
		// ENABLE CREATION MODAL
		enableCreationModal: function(mode, id){
			enableCreationModal(mode, id);
		},
		// CHANGE MODAL CONTENT DEPENDING ON TYPE SELECTION
		resourceTypeSwitchCreationModal: function(obj){
			resourceTypeSwitchCreationModal(obj);
		},
		// CHANGE ONTOLOGY (ORIGIN/DESTINATION) FOR THE RESOURCE
		selectRelationOntology:function(obj){
			var ontology = $("#id_relation_ontology_selector").find(":selected").val();
			var typeSelection = $("#id_relation_type").find(":selected").val();
			var iniPrefix = "intopic_";
			if(typeSelection == 'DESTINY'){
				iniPrefix = 'outtopic_'
			}
			// Prepare statement when ORIGIN/DESTINY
			$("#id_relation_statement").val("create STREAM "+iniPrefix+ontology+" (json String) with (kafka_topic='"+iniPrefix+ontology+"');");
		},
		// CREATE NEW RELATION/RESOURCE
		createRelation(){
			createKsqlRelation();
		},
		// UPDATE RELATION/RESOURCE (Description/Statement)
		updateRelationChanges(){
			updateKsqlRelationChanges();
		},
		// CHECK FOR NON DUPLICATE PROPERTIES
		checkProperty: function(obj){
			
			checkKsqlFlowAvailable($('#identification').val());
			 var error1 = $('.alert-danger');
		     var success1 = $('.alert-success');
		     
			if (ksqlFlowExist) {
	             error1.hide();
			} else{
				console.log('KSQL Flow Identification is not available.');
				//Change style to red
				error1.find('span').text("Identification must be unique.");
				$('#domainId').closest('.form-group').addClass('has-error'); 
				success1.hide();
	            error1.show();
			}
		}
	};
		
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editor;
	var aceEditor;
	var schema = ''; // current schema json string var
		
	KsqlFlowCreateController.load(ksqlFlowCreateJson);
	
	// AUTO INIT CONTROLLER.
	KsqlFlowCreateController.init();
});

	