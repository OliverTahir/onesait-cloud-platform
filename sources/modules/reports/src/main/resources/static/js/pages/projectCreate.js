var ProjectCreateController = function() {
	var authorizationEndpoint = '/controlpanel/projects/authorizations';
	var initTemplateElements = function() {
		$(".disabled").on("click", function(e) {
			e.preventDefault();
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.createfirst
			});
			return false;
		});
		
		$('#resource-identification-filter').keypress(function(e) {
		    if(e.which == 13) {
		        getResourcesFiltered();
		    }
		});

		$('#check-realm').on('change', function() {
			var useRealm = $('#check-realm').is(':checked');

			if (!useRealm) {
				$('#platform-users').removeClass('hide');
				$('#realms-select').addClass('hide');
			} else {
				$('#platform-users').addClass('hide');
				$('#realms-select').removeClass('hide');
			}

		});

	}

	var addWebProject = function() {
		var webProject = $('#webprojects').val();

		if (webProject != '') {
			$("#webprojects-tab-fragment").load(
					'/controlpanel/projects/addwebproject', {
						'webProject' : webProject,
						'project' : projectCreateJson.projectId
					}, function() {
						refreshSelectpickers();
					});
		}

	}

	var removeWebProject = function() {

		$("#webprojects-tab-fragment").load(
				'/controlpanel/projects/removewebproject', {
					'project' : projectCreateJson.projectId
				}, function() {
					refreshSelectpickers();
				});

	}

	var addPlatformUser = function() {
		var user = $('#users').val();
		if (user != '') {
			$("#users-tab-fragment").load('/controlpanel/projects/adduser', {
				'user' : user,
				'project' : projectCreateJson.projectId
			}, function() {
				refreshSelectpickers();
			});
		} else {
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectUser
			});
		}
	}

	var removePlatformUser = function(user) {
		if (user != '') {
			$("#users-tab-fragment").load('/controlpanel/projects/removeuser',
					{
						'user' : user,
						'project' : projectCreateJson.projectId
					}, function() {
						refreshSelectpickers();
					});
		} else {
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectUser
			});
		}
	}

	var unsetRealm = function() {
		var realm = realmLinked;
		if (realm != null && realm != '') {
			$.confirm({
				icon : 'fa fa-warning',
				title : headerReg.titleConfirm + ':',
				theme : 'light',
				type : 'red',
				columnClass : 'medium',
				content : projectCreateJson.confirm.unlinkRealm,
				draggable : true,
				dragWindowGap : 100,
				backgroundDismiss : true,
				closeIcon : true,
				buttons : {
					close : {
						text : headerReg.btnCancelar,
						btnClass : 'btn btn-circle btn-outline blue',
						action : function() {
						} // GENERIC CLOSE.
					},
					remove : {
						text : headerReg.btnEliminar,
						btnClass : 'btn btn-circle btn-outline btn-primary',
						action : function() {
							$("#users-tab-fragment").load(
									'/controlpanel/projects/unsetrealm', {
										'realm' : realm,
										'project' : projectCreateJson.projectId
									}, function() {
										refreshSelectpickers();
										refreshResourcesFragment();
									});
						}
					}
				}
			});

		}
	}

	var setRealm = function() {
		var realm = $("#realms").val();
		if (realm != '') {
			$("#users-tab-fragment").load('/controlpanel/projects/setrealm', {
				'realm' : realm,
				'project' : projectCreateJson.projectId
			},function(){
				refreshResourcesFragment();
			});

		} else {
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectRealm
			});
		}
	}

	var refreshSelectpickers = function() {
		$('#realms').selectpicker();
		$('#users').selectpicker();
		$('#webprojects').selectpicker();
		$('.select-modal').selectpicker();
		$('#check-realm').on('change', function() {
			var useRealm = $('#check-realm').is(':checked');

			if (!useRealm) {
				$('#platform-users').removeClass('hide');
				$('#realms-select').addClass('hide');
			} else {
				$('#platform-users').addClass('hide');
				$('#realms-select').removeClass('hide');
			}

		});
		$('.tooltips').tooltip();
	}

	var getResourcesFiltered = function() {
		var identification = $('#resource-identification-filter').val()
		var type = $('#resource-type-filter').val();
		$('#resources-modal-fragment').load(
				'/controlpanel/projects/resources?identification='
						+ identification + '&project='
						+ projectCreateJson.projectId + '&type=' + type,
				function() {
					$('#resources-modal').modal('show');
					refreshSelectpickers();
				});
	}

	var insertAuthorization = function(obj) {
		var resource = $(obj).closest('tr').find("input[name='ids\\[\\]']")
				.val();
		var accesstype = $(obj).closest('tr').find(
				'select.accesstypes :selected').val();
		var authorizing = $(obj).closest('tr').find(
				'select.authorizing :selected').val();
		if (accesstype == '' || authorizing == '') {
			$.alert({
				title : 'INFO!',
				theme : 'light',
				content : projectCreateJson.validations.selectAccessAndUser
			});
		} else {
			var authorization = {
				'project' : projectCreateJson.projectId,
				'resource' : resource,
				'authorizing' : authorizing,
				'access' : accesstype
			}
			handleAuth(authorization, 'POST').done(updateResourcesFragment)
					.fail();
		}

	}

	var removeAuthorization = function(id) {
		var payload = {
			'id' : id,
			'project' : projectCreateJson.projectId
		};
		handleAuth(payload, 'DELETE').done(updateResourcesFragment).fail();

	}
	var handleAuth = function(payload, methodType) {
		if (methodType == 'POST') {
			return $.ajax({
				url : authorizationEndpoint,
				type : methodType,
				data : JSON.stringify(payload),
				contentType : "application/json",
				dataType : "html",
			});
		} else if (methodType == 'DELETE') {
			return $.ajax({
				url : authorizationEndpoint + '?' + $.param(payload),
				type : methodType,
				data : payload,
				dataType : "html",
			});
		}

	}
	var refreshResourcesFragment = function() {
		$('#resources-tab-fragment').load(
				authorizationEndpoint + '?project='
						+ projectCreateJson.projectId, function() {
					refreshSelectpickers();
				});
	}
	var updateResourcesFragment = function(response) {
		$('#resources-tab-fragment').html(response);
		refreshSelectpickers();
	}

	return {
		removeAuthorization : function(id) {
			removeAuthorization(id);
		},
		insertAuthorization : function(obj) {
			insertAuthorization(obj);
		},
		getResourcesFiltered : function() {
			getResourcesFiltered();
		},
		addWebProject : function() {
			addWebProject();
		},
		removeWebProject : function() {
			removeWebProject();
		},
		removePlatformUser : function(user) {
			removePlatformUser(user);
		},
		addPlatformUser : function() {
			addPlatformUser();
		},
		unsetRealm : function() {
			unsetRealm();
		},
		setRealm : function() {
			setRealm();
		},

		init : function() {
			initTemplateElements();

		}

	}

}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	// ProjectCreateController.load(appCreateJson);

	// AUTO INIT CONTROLLER.
	ProjectCreateController.init();
});