(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('httpService', HttpService);

  /** @ngInject */
  function HttpService($http, $log, __env, $rootScope) {
      var vm = this;

      vm.getDatasources = function(){
        return $http.get(__env.endpointControlPanel + '/datasources/getUserGadgetDatasources');
      }

      vm.getsampleDatasources = function(ds){
        return $http.get(__env.endpointControlPanel + '/datasources/getSampleDatasource/'+ds);
      }

      vm.getDatasourceById = function(datasourceId){
        return $http.get(__env.endpointControlPanel + '/datasources/getDatasourceById/' + datasourceId);
      }

      vm.getFieldsFromDatasourceId = function(datasourceId){
        return $http.get(__env.endpointControlPanel + '/datasources/getSampleDatasource/' + datasourceId);
      }

      vm.getGadgetConfigById = function(gadgetId){
        return $http.get(__env.endpointControlPanel + '/gadgets/getGadgetConfigById/' + gadgetId);
      }

      vm.getUserGadgetsByType = function(type){
        return $http.get(__env.endpointControlPanel + '/gadgets/getUserGadgetsByType/' + type);
      }

      vm.getUserGadgetTemplate = function(){
        return $http.get(__env.endpointControlPanel + '/gadgettemplates/getUserGadgetTemplate/');
      }

      vm.getGadgetMeasuresByGadgetId = function(gadgetId){
        return $http.get(__env.endpointControlPanel + '/gadgets/getGadgetMeasuresByGadgetId/' + gadgetId);
      }

      vm.saveDashboard = function(id, dashboard){        
        return $http.put(__env.endpointControlPanel + '/dashboards/savemodel/' + id, {"model":dashboard.data.model});
      }
      vm.deleteDashboard = function(id){
        return $http.put(__env.endpointControlPanel + '/dashboards/delete/' + id,{});
      }

      vm.setDashboardEngineCredentialsAndLogin = function () {
        if(__env.dashboardEngineOauthtoken === '' || !__env.dashboardEngineOauthtoken){//No oauth token, trying login user/pass
          if(__env.dashboardEngineUsername != '' && __env.dashboardEngineUsername){
            var authdata = 'Basic ' + btoa(__env.dashboardEngineUsername + ':' + __env.dashboardEnginePassword);
            $rootScope.globals = {
              currentUser: {
                  username: __env.dashboardEngineUsername,
                  authdata: __env.dashboardEnginePassword
              }
            };
          }
          else{//anonymous login
            var authdata = 'anonymous';
          }
        }
        else{//oauth2 login
          var authdata = "Bearer " + __env.dashboardEngineOauthtoken;
          $rootScope.globals = {
            currentUser: {
                oauthtoken: __env.dashboardEngineOauthtoken
            }
          };
        }
        //$http.defaults.headers.common['Authorization'] = authdata;
        return $http.get(__env.endpointDashboardEngine + __env.dashboardEngineLoginRest, {headers: {Authorization: authdata}, timeout : __env.dashboardEngineLoginRestTimeout});
      };

      vm.getDashboardModel = function(id){
        return $http.get(__env.endpointControlPanel + '/dashboards/model/' + id);
      }

      vm.insertHttp = function(token, clientPlatform, clientPlatformId, ontology, data){
        return $http.get(__env.restUrl + "/client/join?token=" + token + "&clientPlatform=" + clientPlatform + "&clientPlatformId=" + clientPlatformId).then(
          function(e){
            $http.defaults.headers.common['Authorization'] = e.data.sessionKey;
            return $http.post(__env.restUrl + "/ontology/" + ontology,data);
          }
        )
      }
  };
})();
