(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('editDashboard', {
      templateUrl: 'app/components/edit/editDashboardComponent/edit.dashboard.html',
      controller: EditDashboardController,
      controllerAs: 'ed',
      bindings: {
        "dashboard":"=",
        "iframe":"=",
        "public":"&",
        "id":"&",
        "selectedpage" : "&"
      }
    });

  /** @ngInject */
  function EditDashboardController($log, $window,__env, $scope, $mdSidenav, $mdDialog, $mdBottomSheet, httpService, interactionService, urlParamService,utilsService) {
    var ed = this;
    
    //Gadget source connection type list
    var typeGadgetList = ["pie","bar","map","livehtml","radar","table","mixed","line","wordcloud"];

    ed.$onInit = function () {
      ed.selectedlayer = 0;
      //ed.selectedpage = ed.selectedpage;
      ed.icons = utilsService.icons;
      /*When global style change, that changes are broadcasted to all elements*/
      ed.global = {
        style: {
          header:{
            height: 25,
            enable: "initial",
            backgroundColor: "initial",
            title: {
              textColor: "initial",
              iconColor: "initial"
            }
          },
          border: {},
          backgroundColor: "initial"
        }
      };
    }

    ed.removePage = function (event, index) {
      event.stopPropagation();
      event.preventDefault();
      vm.dashboard.pages.splice(index, 1);
      $scope.$applyAsync();
    };

    ed.pagesEdit = function (ev) {
      $mdDialog.show({
        controller: PagesController,
        templateUrl: 'app/partials/edit/pagesDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          icons: ed.icons
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog pages closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.layersEdit = function (ev) {
      $mdDialog.show({
        controller: LayersController,
        templateUrl: 'app/partials/edit/layersDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage(),
          selectedlayer: ed.selectedlayer
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog layers closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.datasourcesEdit = function (ev) {
      $mdDialog.show({
        controller: DatasourcesController,
        templateUrl: 'app/partials/edit/datasourcesDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage()
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog datasources closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardEdit = function (ev) {
      $mdDialog.show({
        controller: EditDashboardController,
        templateUrl: 'app/partials/edit/editDashboardDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Edit closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardStyleEdit = function (ev) {
      $mdDialog.show({
        controller: EditDashboardStyleController,
        templateUrl: 'app/partials/edit/editDashboardStyleDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          style: ed.global.style
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Edit closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.showDatalink = function (ev) {
      $mdDialog.show({
        controller: DatalinkController,
        templateUrl: 'app/partials/edit/datalinkDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage()
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog datalink closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.showUrlParam = function (ev) {
      $mdDialog.show({
        controller: UrlParamController,
        templateUrl: 'app/partials/edit/urlParamDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage()
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog url Param closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };




    ed.savePage = function (ev) {
      ed.dashboard.interactionHash = interactionService.getInteractionHash();
      ed.dashboard.parameterHash = urlParamService.geturlParamHash();
      httpService.saveDashboard(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}}).then(
        function(d){
          if(d){
            $mdDialog.show({
              controller: DialogController,
              templateUrl: 'app/partials/edit/saveDialog.html',
              parent: angular.element(document.body),
              targetEvent: ev,
              clickOutsideToClose:true,
              fullscreen: false // Only for -xs, -sm breakpoints.
            })
            .then(function(answer) {
              $scope.status = 'You said the information was "' + answer + '".';
            }, function() {
              $scope.status = 'You cancelled the dialog.';
            });

          }
        }
      ).catch(
        function(d){
          if(d){           
            $mdDialog.show({
              controller: DialogController,
              templateUrl: 'app/partials/edit/saveErrorDialog.html',
              parent: angular.element(document.body),
              targetEvent: ev,
              clickOutsideToClose:true,
              fullscreen: false // Only for -xs, -sm breakpoints.
            })
            .then(function(answer) {
              $scope.status = 'You said the information was "' + answer + '".';
            }, function() {
              $scope.status = 'You cancelled the dialog.';
            });
          }
        }
      );
      //alert(JSON.stringify(ed.dashboard));
    };



    ed.getDataToSavePage = function (ev) {
      ed.dashboard.interactionHash = interactionService.getInteractionHash();
      ed.dashboard.parameterHash = urlParamService.geturlParamHash();
      return {"id":ed.id(),"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}};    
    };

    ed.showSaveOK = function (ev) {
      $mdDialog.show({
        controller: DialogController,
        templateUrl: 'app/partials/edit/saveDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false // Only for -xs, -sm breakpoints.
      })
    }



    function DialogController($scope, $mdDialog) {
      $scope.hide = function() {
        $mdDialog.hide();
      };
  
      $scope.cancel = function() {
        $mdDialog.cancel();
      };
  
      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
    }

    ed.deleteDashboard = function (ev) {

      $mdDialog.show({
        controller: DialogController,
        templateUrl: 'app/partials/edit/askDeleteDashboardDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false // Only for -xs, -sm breakpoints.
      })
      .then(function(answer) {
       if(answer==="DELETE"){
        httpService.deleteDashboard(ed.id()).then(
          function(d){
            if(d){
              $mdDialog.show({
                controller: DialogController,
                templateUrl: 'app/partials/edit/deleteOKDialog.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: false // Only for -xs, -sm breakpoints.
              })
              .then(function(answer) {
                $window.location.href=__env.endpointControlPanel+'/dashboards/list';
              }, function() {
                $window.location.href=__env.endpointControlPanel+'/dashboards/list';
              });
            }
          }
        ).catch(
          function(d){
            if(d){
              $mdDialog.show({
                controller: DialogController,
                templateUrl: 'app/partials/edit/deleteErrorDialog.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: false // Only for -xs, -sm breakpoints.
              })
              .then(function(answer) {
                $scope.status = 'You said the information was "' + answer + '".';
              }, function() {
                $scope.status = 'You cancelled the dialog.';
              });
            }
          }
        );
        }
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });


     
    }

    
    ed.closeDashboard = function (ev) {

 
      $mdDialog.show({
        controller: DialogController,
        templateUrl: 'app/partials/edit/askCloseDashboardDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:false,
        fullscreen: false // Only for -xs, -sm breakpoints.
      })
      .then(function(answer) {
        if(answer==="SAVE"){
          ed.dashboard.interactionHash = interactionService.getInteractionHash();
          ed.dashboard.parameterHash = urlParamService.geturlParamHash();
          httpService.saveDashboard(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}}).then(
            function(d){
              if(d){
                $mdDialog.show({
                  controller: DialogController,
                  templateUrl: 'app/partials/edit/saveDialog.html',
                  parent: angular.element(document.body),
                  targetEvent: ev,
                  clickOutsideToClose:true,
                  fullscreen: false // Only for -xs, -sm breakpoints.
                })
                .then(function(answer) {
                  $window.location.href=__env.endpointControlPanel+'/dashboards/list';
                }, function() {
                  $scope.status = 'You cancelled the dialog.';
                });
    
              }
            }
          ).catch(
            function(d){
              if(d){           
                $mdDialog.show({
                  controller: DialogController,
                  templateUrl: 'app/partials/edit/saveErrorDialog.html',
                  parent: angular.element(document.body),
                  targetEvent: ev,
                  clickOutsideToClose:true,
                  fullscreen: false // Only for -xs, -sm breakpoints.
                })
                .then(function(answer) {
                  $scope.status = 'You said the information was "' + answer + '".';
                }, function() {
                  $scope.status = 'You cancelled the dialog.';
                });
              }
            }
          );
        }
        else{
          $window.location.href=__env.endpointControlPanel+'/dashboards/list';
        }
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });



    }


    ed.changedOptions = function changedOptions() {
      //main.options.api.optionsChanged();
    };

    function PagesController($scope, $mdDialog, dashboard, icons, $timeout) {
      $scope.dashboard = dashboard;
      $scope.icons = icons;
      $scope.auxUpload = [];
      $scope.apiUpload = [];

      function auxReloadUploads(){
        /*Load previous images*/
        $timeout(function(){
          for(var page = 0; page < $scope.dashboard.pages.length; page ++){
            if($scope.dashboard.pages[page].background.file.length > 0){
              $scope.apiUpload[page].addRemoteFile($scope.dashboard.pages[page].background.file[0].filedata,$scope.dashboard.pages[page].background.file[0].lfFileName,$scope.dashboard.pages[page].background.file[0].lfTagType);
            }
          }
        });
      }

      auxReloadUploads();

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var newPage = {};
        var newLayer = {};
        //newLayer.options = JSON.parse(JSON.stringify(ed.dashboard.pages[0].layers[0].options));
        newLayer.gridboard = [
        ];
        newLayer.title = "baseLayer";
        newPage.title = angular.copy($scope.title);
        newPage.icon = angular.copy($scope.selectedIconItem);
        newPage.layers = [newLayer];
        newPage.background = {}
        newPage.background.file = angular.copy($scope.file);
        newPage.background.color="hsl(0, 0%, 100%)";
        newPage.selectedlayer= 0;
        dashboard.pages.push(newPage);
        $scope.title = "";
        $scope.icon = "";
        $scope.file = [];
        auxReloadUploads();
        $scope.$applyAsync();
      };

      $scope.onFilesChange = function(index){
        dashboard.pages[index].background.file = $scope.auxUpload[index].file;
        if(dashboard.pages[index].background.file.length > 0){
          var FR = new FileReader();
          FR.onload = function(e) {
            dashboard.pages[index].background.filedata = e.target.result;
          };
          FR.readAsDataURL( dashboard.pages[index].background.file[0].lfFile );
        }
        else{
          dashboard.pages[index].background.filedata="";
        }
      }

      $scope.delete = function(index){
        dashboard.pages.splice(index, 1);
      }

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.moveUpPage = function(index){
        var temp = dashboard.pages[index];
        dashboard.pages[index] = dashboard.pages[index-1];
        dashboard.pages[index-1] = temp;
      }

      $scope.moveDownPage = function(index){
        var temp = dashboard.pages[index];
        dashboard.pages[index] = dashboard.pages[index+1];
        dashboard.pages[index+1] = temp;
      }
    }

    function LayersController($scope, $mdDialog, dashboard, selectedpage, selectedlayer) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.selectedlayer = selectedlayer;
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var newLayer = {}
        newLayer.gridboard = [
          {cols: 5, rows: 5, y: 0, x: 0, type:"mixed"},
          {cols: 2, rows: 2, y: 0, x: 2, hasContent: true},
          {cols: 1, rows: 1, y: 0, x: 4, type:"polar"},
          {cols: 1, rows: 1, y: 2, x: 5, type:"map"},
          {cols: 2, rows: 2, y: 1, x: 0}
        ];
        newLayer.title = angular.copy($scope.title);
        dashboard.pages[$scope.selectedpage].layers.push(newLayer);
        $scope.selectedlayer = dashboard.pages[$scope.selectedpage].layers.length-1;
        $scope.title = "";
        $scope.$applyAsync();
      };

      $scope.delete = function(index){
        dashboard.pages[$scope.selectedpage].layers.splice(index, 1);
      }

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.moveUpLayer = function(index){
        var temp = dashboard.pages[$scope.selectedpage].layers[index];
        dashboard.pages[$scope.selectedpage].layers[index] = dashboard.pages[$scope.selectedpage].layers[index-1];
        dashboard.pages[$scope.selectedpage].layers[index-1] = temp;
      }

      $scope.moveDownLayer = function(index){
        var temp = dashboard.pages[$scope.selectedpage].layers[index];
        dashboard.pages[$scope.selectedpage].layers[index] = dashboard.pages[$scope.selectedpage].layers[index+1];
        dashboard.pages[$scope.selectedpage].layers[index+1] = temp;
      }
    }

    function DatasourcesController($scope, $mdDialog, httpService, dashboard, selectedpage) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.datasources = [];
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var datasource = angular.copy($scope.datasource);
        dashboard.pages[$scope.selectedpage].datasources[datasource.identification]={triggers:[],type:datasource.type,interval:datasources.refresh};
        $scope.name = "";
        $scope.$applyAsync();
      };

      $scope.delete = function(key){
        delete dashboard.pages[$scope.selectedpage].datasources[key];
      }

      $scope.loadDatasources = function(){
        return httpService.getDatasources().then(
          function(response){
            $scope.datasources=response.data;
          },
          function(e){
            console.log("Error getting datasources: " +  JSON.stringify(e))
          }
        );
      }
    }

    function EditDashboardController($scope, $mdDialog, dashboard, $timeout) {
      $scope.dashboard = dashboard;

      function auxReloadUploads(){
        /*Load previous images*/
        $timeout(function(){
          if($scope.dashboard.header.logo.file && $scope.dashboard.header.logo.file.length > 0){
            $scope.apiUpload.addRemoteFile($scope.dashboard.header.logo.filedata,$scope.dashboard.header.logo.file[0].lfFileName,$scope.dashboard.header.logo.file[0].lfTagType);
          }
        });
      }

      $scope.onFilesChange = function(){
        $scope.dashboard.header.logo.file = $scope.auxUpload.file;
        if($scope.dashboard.header.logo.file.length > 0){
          var FR = new FileReader();
          FR.onload = function(e) {
            $scope.dashboard.header.logo.filedata = e.target.result;
          };
          FR.readAsDataURL( $scope.dashboard.header.logo.file[0].lfFile );
        }
        else{
          $scope.dashboard.header.logo.filedata="";
        }
      }

      auxReloadUploads();

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.changedOptions = function changedOptions() {
        $scope.dashboard.gridOptions.api.optionsChanged();
      };

    }

    function compareJSON(obj1, obj2) {
      var result = {};
      for(var key in obj1) {
          if(obj2[key] != obj1[key]) result[key] = obj2[key];
          if(typeof obj2[key] == 'array' && typeof obj1[key] == 'array')
              result[key] = compareJSON(obj1[key], obj2[key]);
          if(typeof obj2[key] == 'object' && typeof obj1[key] == 'object')
              result[key] = compareJSON(obj1[key], obj2[key]);
      }
      return result;
    }

    function EditDashboardStyleController($scope,$rootScope, $mdDialog, style, $timeout) {
      $scope.style = style;

      $scope.$watch('style',function(newValue, oldValue){
        if (newValue===oldValue) {
          return;
        }
        var diffs = compareJSON(oldValue, newValue);
        $rootScope.$broadcast('global.style', diffs);
      }, true)

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }

    function DatalinkController($scope,$rootScope, $mdDialog, interactionService, utilsService, httpService, dashboard, selectedpage) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;

      var rawInteractions = interactionService.getInteractionHash();

      initConnectionsList();
      generateGadgetsLists();

      function initConnectionsList(){
        $scope.connections = [];
        for(var source in rawInteractions){
          for(var indexFieldTargets in rawInteractions[source]){
            for(var indexTargets in rawInteractions[source][indexFieldTargets].targetList){
              var rowInteraction = {
                source:source,
                sourceField:rawInteractions[source][indexFieldTargets].emiterField,
                target:rawInteractions[source][indexFieldTargets].targetList[indexTargets].gadgetId,
                targetField:rawInteractions[source][indexFieldTargets].targetList[indexTargets].overwriteField,
                filterChaining:rawInteractions[source][indexFieldTargets].filterChaining
              }
              $scope.connections.push(rowInteraction);
            }
          }
        }
      }

      $scope.refreshGadgetEmitterFields = function(gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          $scope.gadgetEmitterFields = [];
        }
        else{
          setGadgetEmitterFields(gadget);
        }
      }

      $scope.refreshGadgetTargetFields = function(gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          $scope.gadgetEmitterFields = [];
        }
        else{
          setGadgetTargetFields(gadget);
        }
      }

      function setGadgetEmitterFields(gadget){
        switch(gadget.type){
          case "pie":
          case "bar":
          case "line":
          case "wordcloud":
          case "mixed":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
          case "radar":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
          case "map":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[2]})).map(function(m){return {field:m}});
            break;
          case "livehtml":
            var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
            $scope.emitterDatasource = gadgetData.datasource.name;
            httpService.getFieldsFromDatasourceId(gadgetData.datasource.id).then(
              function(data){
                $scope.gadgetEmitterFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
              }
            )
            $scope.gadgetEmitterFields = [];
            break;
            case "table":
            
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
        }
      }

      //Destination are all gadget fields
      function setGadgetTargetFields(gadget){        
        $scope.targetDatasource="";
        var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
        if(gadget.type === 'livehtml'){
          $scope.targetDatasource = gadgetData.datasource.name;
          var dsId = gadgetData.datasource.id;
        }
        else{
          $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
          var dsId = gadgetData.measures[0].datasource.id;
        }
        httpService.getFieldsFromDatasourceId(dsId).then(
          function(data){
            $scope.gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
          }
        )
        $scope.gadgetTargetFields = [];
      }

      //Get gadget JSON and return string info for UI
      $scope.prettyGadgetInfo = function(gadget){
       
          return gadget.header.title.text + " (" + gadget.type + ")";
        
      }

      $scope.generateGadgetInfo = function (gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          return gadgetId;
        }
        else{
          return $scope.prettyGadgetInfo(gadget)
        }
      }

      function generateGadgetsLists(){
        $scope.gadgetsSources = getGadgetsSourcesInDashboard();
        $scope.gadgetsTargets = getGadgetsInDashboard();
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsSourcesInDashboard(){        
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeGadgetList.indexOf(gadget.type) != -1});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsInDashboard(){
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }

      function findGadgetInDashboard(gadgetId){
        for(var p=0;p<$scope.dashboard.pages.length;p++){
          var page = $scope.dashboard.pages[p];       
          for (var i = 0; i < page.layers.length; i++) {
            var layer = page.layers[i];
            var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
            if(gadgets.length){
              return gadgets[0];
            }
          }
        }
        return null;
      }

      $scope.create = function(sourceGadgetId, originField , targetGadgetId, destinationField,filterChaining) {
        if(sourceGadgetId && originField && targetGadgetId && destinationField){
          interactionService.registerGadgetInteractionDestination(sourceGadgetId, targetGadgetId, originField, destinationField,filterChaining);
          initConnectionsList();
        }
      };

      $scope.delete = function(sourceGadgetId, targetGadgetId, originField, destinationField,filterChaining){
        interactionService.unregisterGadgetInteractionDestination(sourceGadgetId, originField, targetGadgetId, destinationField,filterChaining);
        initConnectionsList();
      }

      $scope.edit = function(sourceGadgetId, originField,targetGadgetId,  destinationField,filterChaining){
        $scope.refreshGadgetEmitterFields(sourceGadgetId);
        $scope.refreshGadgetTargetFields(targetGadgetId)
        $scope.emitterGadget  =sourceGadgetId;
        $scope.emitterGadgetField = originField;
        $scope.targetGadget = targetGadgetId;        
        $scope.targetGadgetField = destinationField;
        $scope.filterChaining = filterChaining;
       
       }
 

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }



    function UrlParamController($scope,$rootScope, $mdDialog,urlParamService , utilsService, httpService, dashboard, selectedpage) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.types=["string","number"];
      var rawParameters = urlParamService.geturlParamHash();

      initUrlParamList();
      generateGadgetsLists();

      function initUrlParamList(){
        $scope.parameters = [];
        for(var paramName in rawParameters){
          for(var indexFieldTargets in rawParameters[paramName]){
            for(var indexTargets in rawParameters[paramName][indexFieldTargets].targetList){
              var rowInteraction = {
                paramName:paramName,
                type:rawParameters[paramName][indexFieldTargets].type,
                mandatory:rawParameters[paramName][indexFieldTargets].mandatory,
                target:rawParameters[paramName][indexFieldTargets].targetList[indexTargets].gadgetId,
                targetField:rawParameters[paramName][indexFieldTargets].targetList[indexTargets].overwriteField
              }
              $scope.parameters.push(rowInteraction);
            }
          }
        }
      }

      $scope.refreshGadgetTargetFields = function(gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget != null){
          setGadgetTargetFields(gadget);
        }
      }

      //Destination are all gadget fields
      function setGadgetTargetFields(gadget){
        
        $scope.targetDatasource="";
        var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
        if(gadget.type === 'livehtml'){
          $scope.targetDatasource = gadgetData.datasource.name;
          var dsId = gadgetData.datasource.id;
        }
        else{
          $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
          var dsId = gadgetData.measures[0].datasource.id;
        }
        httpService.getFieldsFromDatasourceId(dsId).then(
          function(data){
            $scope.gadgetTargetFields = utilsService.transformJsonFieldsArrays(utilsService.getJsonFields(data.data[0],"", []));
          }
        )
        $scope.gadgetTargetFields = [];
      }

      //Get gadget JSON and return string info for UI
      $scope.prettyGadgetInfo = function(gadget){
       
          return gadget.header.title.text + " (" + gadget.type + ")";
        
      }

      $scope.generateGadgetInfo = function (gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          return gadgetId;
        }
        else{
          return $scope.prettyGadgetInfo(gadget)
        }
      }

      function generateGadgetsLists(){
     
        $scope.gadgetsTargets = getGadgetsInDashboard();
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsInDashboard(){
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }

      function findGadgetInDashboard(gadgetId){
        for(var p=0;p<$scope.dashboard.pages.length;p++){
          var page = $scope.dashboard.pages[p];       
          for (var i = 0; i < page.layers.length; i++) {
            var layer = page.layers[i];
            var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
            if(gadgets.length){
              return gadgets[0];
            }
          }
        }
        return null;
      }

      $scope.create = function(parameterName, parameterType , targetGadgetId, destinationField, mandatory) {
        if(parameterName && parameterType && targetGadgetId && destinationField){
          urlParamService.registerParameter(parameterName, parameterType, targetGadgetId, destinationField, mandatory);
          initUrlParamList();
        }
      };

      $scope.delete = function(parameterName, parameterType , targetGadgetId, destinationField, mandatory){
        urlParamService.unregisterParameter(parameterName, parameterType , targetGadgetId, destinationField, mandatory);
        initUrlParamList();
      }
      $scope.edit = function(parameterName, parameterType , targetGadgetId, destinationField, mandatory){
       $scope.refreshGadgetTargetFields(targetGadgetId);
       $scope.paramName=parameterName;
       $scope.type=parameterType;
       $scope.targetGadget=targetGadgetId;
       
       $scope.targetGadgetField=destinationField;
       $scope.mandatory=mandatory;
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }



    ed.showListBottomSheet = function() {
      $window.dispatchEvent(new Event("resize"));      
      $mdBottomSheet.show({
        templateUrl: 'app/partials/edit/addWidgetBottomSheet.html',
        controller: AddWidgetBottomSheetController,
        disableParentScroll: false,
        disableBackdrop: true,
        clickOutsideToClose: true       
      }).then(function(clickedItem) {
        $scope.alert = clickedItem['name'] + ' clicked!';
        
      }).catch(function(error) {
        // User clicked outside or hit escape
      });
      
    };

    function AddWidgetBottomSheetController($scope, $mdBottomSheet){
      $scope.closeBottomSheet = function() {         
        $mdBottomSheet.hide();
      }
    }

    $scope.$on('deleteElement',function (event, item) {
      var dashboard = $scope.ed.dashboard;
      var page = dashboard.pages[$scope.ed.selectedpage()];
      var layer = page.layers[page.selectedlayer];
      layer.gridboard.splice(layer.gridboard.indexOf(item), 1);
      $scope.$applyAsync();
    });
  }
})();
