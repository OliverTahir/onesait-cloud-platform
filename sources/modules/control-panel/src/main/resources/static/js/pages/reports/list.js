Report.List = (function() {
	"use-strict";
	var mountableModel = $('#table_parameters').find('tr.parameters-model')[0].outerHTML;
	
	var init = function() {
		
		// Create event
		$('#btn-report-create').on('click', function (e) {
			e.preventDefault(); 
			window.location = '/controlpanel/reports/create';
		})		
	};
	
	var dtRenderActionColumn = function (data, type, row) {
		return '<div class="grupo-iconos text-center">'
		+ '<span data-id="' + row.id + '" class="icon-report-play btn btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="Play"><i class="la la-play font-hg"></i></span>'																																																			
		+ '<span data-id="' + row.id + '" class="icon-report-download btn btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="Download"><i class="la la-download font-hg"></i></span>'																																																
		+ '<span data-id="' + row.id + '" class="icon-report-edit btn btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="Edit"><i class="la la-edit font-hg"></i></span>'																																																			
		+ '<span data-id="' + row.id + '" class="icon-report-trash btn btn-xs btn-no-border btn-circle btn-outline blue tooltips" data-container="body" data-placement="bottom" data-original-title="Delete"><i class="la la-trash font-hg"></i></span>'																											
		+ '</div>';
	};
	
	function initCompleteCallback(settings, json) {
		
		initTableEvents();
		
		//customizeDatatable();
		$('#reports_processing').remove();
	}
	
	/*function customizeDatatable() {
		
	}*/
	
	function reloadReportTable() {
		var oTable = $('.datatable').dataTable();
		reloadDataTable(oTable);
	}
	
	function reloadDataTable(oTable) {		
		oTable.fnClearTable();
		
		oTable.DataTable().ajax.reload(function() {
			Report.List.initCompleteCallback()
		}, true);
	}
	
	var ajaxDownload = function(url, httpMethod, payload){
		$.fileDownload(url, {
    		httpMethod: httpMethod,
    		dataType:"json", 
            contentType:"application/json",
            data: payload,
    		successCallback: function(url) {
    			//$('#loadingDialog').dialog('close');
    		},
    		failCallback: function(responseHtml, url) {
    			alert('Ha ocurrido un error');
    		}
    	});
	}
	
	var runReportWithParameters = function(obj){
		var id = $('#current-report').val();
		var elements =  $(obj).closest('tbody').find('tr');
		

	}
	
	function initTableEvents() {
		
		$('.icon-report-play').each(function() {
			$(this).on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				 $.ajax({
			       	 	url : '/controlpanel/reports/' +id +'/parameters',
			            type : 'GET'
			        }).done(function(data) {
			        	var parameters = data;
			        	if(parameters == null || parameters.length == 0)
			        		ajaxDownload('/controlpanel/reports/download/report/'+ id, 'GET', []);
			        	else{
			        		if ($('#parameters').attr('data-loaded') === 'true'){
			    				$('#table_parameters > tbody').html("");
			    				$('#table_parameters > tbody').append(mountableModel);
			    			}
			        		
			        		$('#table_parameters').mounTable(parameters,{
			    				model: '.parameters-model',
			    				noDebug: false							
			    			});
			        		$('#parameters').removeClass('hide');
			    			$('#parameters').attr('data-loaded',true);
			    			$('#parametersModal').modal('show');
			    			$('#current-report').val(id);
			        		
			        	}
			        		
			        	
			        }).fail(function(error) {
			        	alert('Zorro plateado comunica: Ha ocurrido un error ' + error);
			        	$tabs.css({ "visibility" : "hidden" });
			        });
				
			});
		})
		
		$('.icon-report-trash').each(function() {
			$(this).on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id'); 
				deleteReportDialog(id);
			});
		});

		$('.icon-report-download').each(function() {
			$(this).on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				$(this).on('click', function (e) {
					e.preventDefault(); 
					var id = $(this).data('id');
					$.fileDownload('/controlpanel/reports/download/report-design/'+ id, {
			    		httpMethod: 'GET',
			    		successCallback: function(url) {
			    			//$('#loadingDialog').dialog('close');
			    		},
			    		failCallback: function(responseHtml, url) {
			    			alert('Ha ocurrido un error');
			    		}
			    	});
				});
			});
		});
		
		$('.icon-report-edit').each(function() {
			$(this).on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				window.location = '/controlpanel/reports/edit/' + id;
			});
		});
	}
	
	var deleteReportDialog = function(id) {
		$.confirm({
			icon: 'fa fa-warning',
			title: "Confirmation",
			theme: 'light',
			columnClass: 'medium',
			content: "You are going to delete a report, are you sure?",
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			closeIcon: true,
			buttons: {
				close: {
					text: 'Close',
					btnClass: 'btn btn-sm btn-circle btn-outline blue',
					action: function (){} //GENERIC CLOSE.		
				},
				Ok: {
					text: "Delete",
					btnClass: 'btn btn-sm btn-circle btn-outline btn-blue',
					action: function() { 
						$.ajax({ 
						    url : '/controlpanel/reports/delete/' + id,
						    type : 'DELETE'
						}).done(function( result ) {							
							reloadReportTable();
						}).fail(function( error ) {
						   	alert('TODO: Pasar a un modal. \n\nHa habido un error');
						}).always(function() {
						});
					}											
				}					
			}
		});
	}
	
	// Public API
	return {
		init: init,
		dtRenderActionColumn: dtRenderActionColumn,
		initCompleteCallback: initCompleteCallback,
		reloadReportTable: reloadReportTable,
		runReportWithParameters: runReportWithParameters
		
	};
	
})();

$(document).ready(function() {	
	
	Report.List.init();
	
	/*$.fn.dataTable.ext.buttons.alert = {
	    className: 'buttons-alert',
	 
	    action: function ( e, dt, node, config ) {
	        alert( this.text() );
	    }
	};
	
	
	$('.datatable').DataTable( {
        dom: 'Bfrtip',
        buttons: [
            {
                extend: 'alert',
                text: 'My button 1'
            },
            {
                extend: 'alert',
                text: 'My button 2'
            },
            {
                extend: 'alert',
                text: 'My button 3'
            }
        ]
    } );*/
	
	
	/*$('.datatable').DataTable( {
        dom: 'Bfrtip',
        buttons: [
            {
                text: 'Reload',
                action: function ( e, dt, node, config ) {
                    alert( 'Reload' );
                    reloadReportTable()
                }
            }
        ]
    });*/
});
