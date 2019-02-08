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
	};	

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
