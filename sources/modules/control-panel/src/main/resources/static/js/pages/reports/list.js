Report.List = (function() {
	"use-strict";
	
	var init = function() {
		
		// Create event
		$('#btn-report-create').on('click', function (e) {
			e.preventDefault(); 
			window.location = '/controlpanel/reports/create';
		})
		
		
		$('#reports_processing').hide();
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
	}
	
	function reloadReportTable() {
		var oTable = $('.datatable').dataTable();
		reloadDataTable(oTable);
	}
	
	function reloadDataTable(oTable) {
		//var oTable = $('.datatable').dataTable();
		oTable.fnClearTable();
		oTable.DataTable().ajax.reload(function() {
			Report.List.initCompleteCallback()
		}, true);
		
	}
	
	function initTableEvents() {
		
		$('.icon-report-play').each(function() {
			$(this).on('click', function (e) {
				e.preventDefault(); 
				var id = $(this).data('id');
				$.fileDownload('/controlpanel/download/report/'+ id, {
		    		httpMethod: 'GET',
		    		successCallback: function(url) {
		    			//$('#loadingDialog').dialog('close');
		    		},
		    		failCallback: function(responseHtml, url) {
		    			alert('Ha ocurrido un error');
		    		}
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
				alert('Debe descargar el informe original (jrxml o jasper) : ' + id);
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
		reloadReportTable: reloadReportTable
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
