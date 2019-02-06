<!-- Copyright Indra Sistemas, S.A. 2013-2018 SPAIN -->
<html xmlns:th="http://www.thymeleaf.org" xmlns:dt="http://www.thymeleaf.org/dandelion/datatables" th:with="lang=${#locale.language}" th:lang="${lang}">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
		<meta http-equiv="Content-Language" th:content="${lang}"/>
		<title th:text="#{name.app}"/>
		
		<!-- STYLE SHEETS -->
		<link rel="stylesheet" type="text/css" media="all" th:href="@{/static/css/bootstrap.min.css}"/>
		<link rel="stylesheet" type="text/css" media="all" th:href="@{/static/css/components.css}"/>
		<link rel="stylesheet" type="text/css" media="all" th:href="@{/static/css/plugins.css}"/>
		<link rel="stylesheet" type="text/css" media="all" th:href="@{/static/css/layout.css}"/>
		<!-- THEME -->
		<link rel="stylesheet" type="text/css" media="all" th:href="@{/static/webjars/sofia2_theme/css/sofia2.css}"/>
		
		<!-- PLUGINS STYLE SHEETS -->
		<link rel="stylesheet" type="text/css" media="all" th:href="@{/static/vendor/datatable/datatables.bootstrap.css}"/>	
	</head>	
	
	<!-- page-sidebar-closed to start page with collapsed menu -->
	<body class="page-header-fixed  page-content-white page-sidebar-closed">

	<!-- MAIN PAGE WRAPPER -->
	<div class="page-wrapper">
	
		<!-- BEGIN HEADER INCLUDE -->
		<div th:include="fragments/header::#headerFragment" class="page-header navbar navbar-fixed-top"></div>
		<!-- END HEADER -->
		
		<!-- BEGIN HEADER AND CONTENT DIVIDER -->
		<div class="clearfix"> </div>		
			
		<!-- BEGIN SIDEBAR -->
		<div th:include="fragments/menu::#menuFragment" class="page-sidebar-wrapper"></div>
		<!-- END SIDEBAR -->
		
				
		<!-- BEGIN CONTENT -->
		<div class="page-content-wrapper">
			
			<!-- BEGIN CONTENT BODY -->
			<div class="page-content">
				
				<!-- BEGIN PAGE HEADER-->
				
				<!-- BEGIN PAGE BAR AND BREADCRUM -->
				<div class="page-bar margin-bottom-20">
					<ul class="page-breadcrumb">
						<li><a th:href="@{/}">Home</a><i class="fa fa-angle-right"></i></li>						
						<li><span th:text="#{reports.my}">My Dashboards</span></li>
					</ul>						
				</div>
				<!-- END PAGE BAR -->
				
				<!-- BEGIN PAGE TITLE-->
				<h1 class="page-title hide "><span th:text="#{reports.my}">My Dashboards </span></h1>
				<!-- END PAGE TITLE-->			
				
				<!-- MAIN ROW -->
				<div class="row">
					<div class="col-md-12">					
						<div class="portlet light">
							
							<!-- TITTLE CONTENT -->
							<div class="portlet-title">
								<div class="caption">									
									<span class="caption-subject" th:text="#{reports.my}"> My Dashboard </span>									
								</div>									
								<div class="tools hide">
									<a href="" class="collapse" data-original-title="" title=""> </a>
									<a href="" class="fullscreen" data-original-title="" title=""> </a>
								</div>								
								<div class="actions margin-right-20">
									<div class="btn-group">										
										<button id="search-toggle" type="button" class="btn btn-circle btn-outline blue" onclick="$('#searchFilter').toggleClass('hide')"><span th:text="#{gen.search}"> Search </span></button>																												
										<button sec:authorize="hasAuthority('ROLE_ADMINISTRATOR') or hasAuthority('ROLE_DEVELOPER') or hasAuthority('ROLE_DATASCIENTIST')" type="button" class="btn btn-circle btn-outline btn-primary" id="btn-report-create"><span th:text="#{gen.create}">New </span></button>
									</div>										
								</div>
							</div><!-- // TITTLE CONTENT -->
							
							<!-- BODY CONTENT -->
							<div class="portlet-body" style="display: block; height: auto;">
								<div class="row">
									<!-- SEARCH -->									
									<div id="searchFilter"  class="col-md-12 hide">
										<form id="form_dashboard" class="" role="form" method="get">								
											<div class="form-body " style="border-bottom: 1px solid #eef1f5;">
												<div class="row">
													<div class="col-md-4 col-xs-12">
														<div class="form-group">															
															<div class="input-group">
																<span class="input-group-addon">
																	<i class="fa fa-tag font-grey-mint"></i>
																</span>															
																<input id="name" th:placeholder="#{gen.name}" name="name" class="form-control" type="text" maxlength="50" value=""/>
															</div>
														</div>
													</div>
													
													<div sec:authorize="hasAuthority('ROLE_ADMINISTRATOR')" class="col-md-4 col-xs-12">
														<div class="form-group">															
															<div class="input-group">
																<span class="input-group-addon">
																	<i class="fa fa-tag font-grey-mint"></i>
																</span>
																<input id="type" th:placeholder="#{gen.type}" name="type" class="form-control" type="text" maxlength="45" value=""/>
															</div>
														</div>
													</div>
													<div class="col-md-4 col-xs-12">
														<div class="btn-group">
															<button type="button" id="btn-report-search" name="btn-report-search" th:title="#{gen.search}" th:value="#{dashboard_search_button_form}" value="Search" class="btn btn-sm btn-circle btn-outline blue" th:text="#{gen.search}">Search </button>
															<button type="button" id="btn-report-refresh" name="btn-report-refresh" value="Reset" class="btn btn-sm btn-circle btn-outline blue" title="Reset"><i class="la la-refresh"></i>&nbsp;</button>													
														</div>
													</div>
												</div>
											</div>												
										</form>								
									</div><!-- // SEARCH -->
																											
									<!-- REPORT LIST -->
									<div class="col-md-12">											
										<div id="contenedor-tabla-outside" class="contiene margin-bottom-30">																						
											<div>
												<!-- DATATABLE DANDELION CONF. -->												
												<table id="dashboards" class="table table-hover table-striped" dt:table="true" dt:paginationtype="full_numbers" dt:url="@{/reports/list/data}">
													<thead>
														<tr class="cabecera-tabla">
															<th dt:visible="false" dt:property="id" class="titulo-columnas hide" th:text="#{gen.name}">Name</th>
															<th dt:property="name" class="titulo-columnas" th:text="#{gen.name}">Name</th>
															<th dt:property="description" class="titulo-columnas" th:text="#{gen.description}">Description</th>
															<th dt:property="owner" class="titulo-columnas" th:text="#{gen.owner}">Owner</th>
															<th dt:property="created" dt:renderFunction="dtRenderDateCenterColumn" class="titulo-columnas text-center">Created At</th>
															<th dt:property="isPublic" dt:renderFunction="dtRenderPublicColumn" class="titulo-columnas text-center" th:text="#{gen.public}">Public</th>	
															<th dt:sortable="false" dt:renderFunction="dtRenderActionColumn" class="titulo-columnas text-center"><span th:text="#{gen.options}">Options</span></th>
														</tr>
													</thead>
												</table><!-- // DATATABLE DANDELION CONF. -->
											</div>
										</div>											
									</div>
								</div><!-- // REPORT LIST -->
							</div><!-- // BODY CONTENT -->
						</div><!-- // PORTLET BASIC LIGHT -->							
					</div><!-- // COL-12 -->						
				</div><!-- // MAIN ROW -->				
			</div><!-- // CONTENT BODY -->
		</div><!-- // CONTENT page-content-wrapper -->		
	</div><!-- // MAIN PAGE WRAPPER -->
	
	<!-- FOOTER-INCLUDE -->
	<footer th:include="fragments/footer::#footerFragment" class="page-footer"> </footer>	
	
	<!-- CORE CONTROLLERS -->
	<script th:src="@{/static/js/app.js}"/>
	<script th:src="@{/static/js/layout.js}"/>
	
	<!-- PLUGINS -->
	<script th:src="@{/static/vendor/jquery/jquery.dataTables.min.js}"/>
	<script th:src="@{/static/vendor/datatable/datatables.bootstrap.js}"/>	
	<script th:src="@{/static/vendor/jquery/jquery.autocomplete.js}"/>
	<script th:src="@{/static/vendor/jquery-format-datetime/jquery.formatDateTime.js}"/>
	
	
	<script type="text/javascript" th:inline="javascript">	
		
	

	
	// TEMPLATE SEARCH FORM.
	var sname = [[${param.name}]] || '';
	var stype = [[${param.type}]] || '';
	
	sname ? $('#name').val(sname) : sname = '';
	stype ? $('#type').val(stype) : stype = '';
	console.log('SEARCHING ... [ Name: ' + sname + ' Type: ' + stype + ']');
	if (( sname != '') || (stype != '')) { $('#search-toggle').trigger('click'); }
	
	// DATATABLES LANGUAJE FROM PROPERTIES.
	datatable_lang = [[#{datatables_lang}]];	
	var languageJson = JSON.parse(datatable_lang);
	if ( languageJson ) { 
		$.extend( true, $.fn.dataTable.defaults, { language: languageJson }); 
	}

	</script>
	
	<script th:src="@{/static/js/pages/reports/list.js}" th:inline="javascript"/>
</body>
</html> 
