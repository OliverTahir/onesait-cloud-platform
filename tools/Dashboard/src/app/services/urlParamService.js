(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('urlParamService', urlParamService);

  /** @ngInject */
  function urlParamService($log, __env, $rootScope) {
    
    var vm = this;
    //Gadget interaction hash table, {gadgetsource:{emiterField:"field1", targetList: [{gadgetId,overwriteField}]}}
    vm.urlParamHash = {

    };

    vm.seturlParamHash = function(urlParamHash){
      vm.urlParamHash = urlParamHash;
    };

    vm.geturlParamHash = function(){
      return vm.urlParamHash;
    };


    vm.registerParameter = function (parameterName, parameterType, targetGadgetId, destinationField, mandatory) {
      //Auto generated
      
      if(!(parameterName in vm.urlParamHash) ){        
        vm.urlParamHash[parameterName] = [];
        vm.urlParamHash[parameterName].push(
          {
            targetList: [],
            type: parameterType,
            mandatory:mandatory
          }
        )
      }
      var parameter = vm.urlParamHash[parameterName];
        parameter[0].mandatory = mandatory;
        parameter[0].type = parameterType;
        var found = -1;
        for (var keyGDest in parameter[0].targetList) {
          var destination = parameter[0].targetList[keyGDest];
          if (destination.gadgetId == targetGadgetId) {
            found = keyGDest;
            destination.gadgetId = targetGadgetId;
            destination.overwriteField = destinationField;
          }
        }
        if (found == -1) {
          parameter[0].targetList.push({
          gadgetId: targetGadgetId,
          overwriteField: destinationField        
          })
        }
    };

    

    vm.unregisterParameter = function (parameterName, parameterType, targetGadgetId, destinationField, mandatory) {      
      var parameter = vm.urlParamHash[parameterName].filter(
        function (elem) {
          return elem.type == parameterType && elem.mandatory == mandatory;
        }
      );
      var found = -1;
      parameter[0].targetList.map(
        function (dest, index) {
          if (dest.overwriteField == destinationField && dest.gadgetId == targetGadgetId) {
            found = index;
          }
        }
      );
      if (found != -1) {
        parameter[0].targetList.splice(found, 1);
      }
      if(parameter[0].targetList.length == 0){
       delete vm.urlParamHash[parameterName];
      }
    };

    vm.unregisterGadget = function (gadgetId) {    
      //Delete from destination list
      for (var keyGadget in vm.urlParamHash) {
        var destinationList = vm.urlParamHash[keyGadget];
        for (var keyDest in destinationList) {
          var destinationFieldBundle = destinationList[keyDest];
          var found = -1; //-1 not found other remove that position in targetList array
          for (var keyGDest in destinationFieldBundle.targetList) {
            var destination = destinationFieldBundle.targetList[keyGDest];
            if (destination.gadgetId == gadgetId) {
              found = keyGDest;
              break;
            }
          }
          //delete targetList entry if diferent -1
          if (found != -1) {
            destinationBundle.targetList.splice(found, 1);
          }
        }
      }
    };


  vm.generateFiltersForGadgetId = function (gadgetId){    
  var filterList=[];
  for (var keyParameter in vm.urlParamHash) {
    var destinationList = vm.urlParamHash[keyParameter];
    for (var keyDest in destinationList) {
      var destinationFieldBundle = destinationList[keyDest];
      var found = -1; //-1 not found other remove that position in targetList array
      for (var keyGDest in destinationFieldBundle.targetList) {
        var destination = destinationFieldBundle.targetList[keyGDest];
        if (destination.gadgetId == gadgetId) {
          if(__env.urlParameters.hasOwnProperty(keyParameter)){
            filterList.push(buildFilter(keyParameter,keyDest,keyGDest))
          }
          break;
        }
      }
     
    }
  }
  return filterList;
}

    function buildFilter(keyParameter,keyDest,keyGDest){      
      var value = __env.urlParameters[keyParameter];
      var field = vm.urlParamHash[keyParameter][keyDest].targetList[keyGDest].overwriteField;
      var op ="=";
      if(vm.urlParamHash[keyParameter][keyDest].type === "string"){
        value = "\"" + value + "\"";
      }else if(vm.urlParamHash[keyParameter][keyDest].type === "number"){
        value = Number(value);
      }

     var filter = {
      field: field,
      op: op,
      exp: value
    };
      return filter     
    }

    vm.checkParameterMandatory = function (){
      var result = [];
      for (var keyParameter in vm.urlParamHash) {
        var param = vm.urlParamHash[keyParameter];
        for (var keyDest in param) {
          if(param[keyDest].mandatory){
            if(!__env.urlParameters.hasOwnProperty(keyParameter)){
              result.push({name:keyParameter,val:""});
            }            
          }
        }
      }
      return result;
    }

    vm.generateUrlWithParam=function(url,parameters){
      var result = "";
      if((Object.getOwnPropertyNames(__env.urlParameters).length + parameters.length)>0){
        result = "?"; 
        
        for (var name in __env.urlParameters) {
          if(result.length==1){
            result=result+name+"="+__env.urlParameters[name];
          }else{
            result=result+"&"+name+"="+__env.urlParameters[name];
          }
        }
        for (var index in parameters) {
          if(result.length==1){
            result=result+parameters[index].name+"="+parameters[index].val;
          }else{
            result=result+"&"+parameters[index].name+"="+parameters[index].val;
          }
        }

      }
      return result;
    }
  };
})();
