(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('element', {
      templateUrl: 'app/components/view/elementComponent/element.html',
      controller: ElementController,
      controllerAs: 'vm',
      bindings:{
        element:"=",
        iframe: "=",
        editmode:"<"
      }
    });

  /** @ngInject */
  function ElementController($log, $scope, $mdDialog, $sanitize, $sce, $rootScope, gadgetManagerService) {
    var vm = this;
    vm.isMaximized = false;

    vm.$onInit = function () {
      inicializeIncomingsEvents(); 
     
      
       vm.timesseriesconfig = {
        startDate:moment().subtract(8,'hour'),
        endDate:moment(),
        realTime:true,
        timesSeriesinterval : 'h',
        timesSeriesintervalRealTime : 8
      }  
      vm.startDate = new Date(vm.timesseriesconfig.startDate.format("YYYY-MM-DDTHH:mm"));
      vm.endDate =  new Date(vm.timesseriesconfig.endDate.format("YYYY-MM-DDTHH:mm"));
      
      vm.arrayTimesSeriesRealTimeinterval=[
        {key:"8 h", value:8 },
        {key:"16 h", value:16},
        {key:"24 h", value:24}]; 
        vm.arrayTimesSeriesinterval=[
          {key:"h", value:"h" },
          {key:"m", value:"m"},
          {key:"s", value:"s"},
          {key:"d", value:"d"}];
    };

    vm.openMenu = function($mdMenu){      
      vm.startDate = new Date(vm.timesseriesconfig.startDate.format("YYYY-MM-DDTHH:mm"));
      vm.endDate =  new Date(vm.timesseriesconfig.endDate.format("YYYY-MM-DDTHH:mm"));
      $mdMenu.open();
    }

    

    vm.updateDates = function(){      
      vm.timesseriesconfig.startDate = moment(vm.startDate);
      vm.timesseriesconfig.endDate = moment(vm.endDate) ;     
    }

    function inicializeIncomingsEvents(){
      $scope.$on("global.style",
        function(ev,style){
          angular.merge(vm.element,vm.element,style);
        }
      );

      /* Global handler by id */
      /*$scope.$on(vm.element.id,
        function(ev,data){
          angular.merge(vm.element,vm.element,data);
        }
      );*/
    }

    vm.openEditGadgetIframe = function(ev) {     
      $mdDialog.show({
        parent: angular.element(document.body),
        targetEvent: ev,
        fullscreen: true,
        template:
          '<md-dialog id="dialogCreateGadget"  aria-label="List dialog">' +
          '  <md-dialog-content >'+
          '<iframe id="iframeCreateGadget" style=" height: 100vh; width: 100vw;" frameborder="0" src="'+__env.endpointControlPanel+'/gadgets/updateiframe/'+vm.element.id+'"+></iframe>'+                     
          '  </md-dialog-content>' +             
          '</md-dialog>',
          locals: {
            element: vm.element
          },
        controller: DialogIframeEditGadgetController
     });
     function DialogIframeEditGadgetController($scope, $mdDialog, element) {
       $scope.element = element;
       $scope.closeDialog = function() {
         var gadgets =document.querySelectorAll( 'gadget' ) ;
         if(gadgets.length>0){
          for (var index = 0; index < gadgets.length; index++) {
            var gad = gadgets[index];
            angular.element(gad).scope().$$childHead.reloadContent();
          }        
        }
         $mdDialog.hide();
       }
      };


     };

     // toggle gadget to fullscreen and back.
     vm.toggleFullScreen = function($scope, element){               
       vm.isMaximized = !vm.isMaximized;
     };


    vm.openEditContainerDialog = function (ev) {
      $mdDialog.show({
        controller: EditContainerDialog,
        templateUrl: 'app/partials/edit/editContainerDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    function EditContainerDialog($scope, $mdDialog,utilsService, element) {
      $scope.icons = utilsService.icons;

      $scope.element = element;

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

    vm.openEditGadgetDialog = function (ev) {
      $mdDialog.show({
        controller: EditGadgetDialog,
        templateUrl: 'app/partials/edit/editGadgetDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
       
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {
       
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    
    };

    function EditGadgetDialog($timeout,$scope, $mdDialog,  element, httpService) {
      $scope.editor;
      
      $scope.element = element;

      $scope.codemirrorLoaded = function(_editor){
        // Editor part
        var _doc = _editor.getDoc();
        _editor.focus();
        $scope.refreshCodemirror = true;
        $timeout(function () {
          $scope.refreshCodemirror = false;
        }, 100);
        $scope.loadDatasources();
      };
    


      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };

      $scope.datasources = [];

      $scope.loadDatasources = function(){
        return httpService.getDatasources().then(
          function(response){
            $scope.datasources=response.data;
          },
          function(e){
            console.log("Error getting datasources: " +  JSON.stringify(e))
          }
        );
      };

    }

    vm.trustHTML = function(html_code) {
      return $sce.trustAsHtml(html_code)
    }

    vm.calcHeight = function(){
      vm.element.header.height = (vm.element.header.height=='inherit'?25:vm.element.header.height);
      /*return "calc(100% - " + (vm.element.header.enable?(parseInt(vm.element.header.height)+75) + "px" :15 + "%") + ")";*/
      return "calc(100% - 15%)";
    }

    vm.deleteElement = function(){
      $rootScope.$broadcast("deleteElement",vm.element);
    }

    vm.generateFilterInfo = function(filter){    
      //return filter.value + ' (' + gadgetManagerService.findGadgetById(filter.id).header.title.text + ')'; 
      return filter.value;
    }

    vm.deleteFilter = function(id, field){     
      $rootScope.$broadcast(vm.element.id,{id: id,type:'filter',data:[],field:field})
    }
  }
})();
