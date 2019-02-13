//== Set Vars of all the site
var Report = Report || {};

Report.Create = (function() {
	"use-strict";
	
	var $form = $("#form-report");
	
	var init = function() {
		
		// -- Events -- //
		$("#btn-report-cancel").on('click', function (e) {
			e.preventDefault();
			window.location = '/controlpanel/reports/list';
		});
		
		$("#btn-report-reset").on('click', function (e) {
			e.preventDefault();
		});
		
		$("#btn-report-save").on('click', function (e) {
			e.preventDefault();
			submitForm($('#report-save-action').val(), $('#report-save-method').val());
		});
		
		maxsize = 60000000; // TODO
		setEventListeners(maxsize);
		
		$("#btn-report-upload").on('click', function (e) {
			$("#btn-report-upload-file").click();
		});
	};	

	var setEventListeners = function (maxsize) {
		$("#btn-report-upload-file").bind('change', function() {
			 if(this.files[0].size> maxsize){
				 $("#modal-error").modal('show');
				 return false;
			 } else {
				 $("#modal-error").modal('hide');
				 uploadFile();
				 //$("#report-settings").css({ "visibility" : "visible" });
			 }
		 });
	}
	
	var uploadFile = function () {
		
		/*var data = new FormData();
		 $.each($('#file')[0].files, function(i, file) {
		     data.append('file-'+i, file);
		 });*/
		 
		var formData = new FormData();
        formData.append('file', $('input[type=file]')[0].files[0]);
        console.log("formData " + formData);
		 
        $.ajax({
       	 	url : '/controlpanel/reports/info',
       	 	enctype: 'multipart/form-data',
       	 	data : formData,
            processData : false,
            contentType : false,
            type : 'POST'
        }).done(function(data) {
        	console.log(JSON.stringify(data));
        	
        	$tableParams = $("#table-report-parameters");
        	$tbodyParams = $tableParams.find('tbody');
        	
        	// {"parameters":[{"name":"title","description":"Titulo del informe","value":null,"type":"java.lang.String"}],"fields":[]}
        	
        	var parameters = data.parameters;
        	for (i = 0; i < parameters.length; i++) { 
        		var row = $('<tr>');
        		row.append($('<td>').html(parameters[i].name))
        			.append($('<td>').html('<input type="text" value="" size="32" />')) // parameters[i].value)
        			.append($('<td>').html(parameters[i].description));
        		$tbodyParams.append(row);
        	}
        	
        	$("#report-settings").css({ "visibility" : "visible" });
        }).fail(function() {
        	$("#report-settings").css({ "visibility" : "hidden" });
        });
	}
	
	function submitForm(action, method) {
		$form.attr('action', action);
		$form.attr('method', 'post');
		$form.submit();
	}
	
	// Public API
	return {
		init: init
	};
})();

$(document).ready(function() {	
	
	Report.Create.init();
});
