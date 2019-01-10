webpackJsonp([10],{0:function(t,e,n){(function(t){"use strict";var e=n(1),i=e.module("sba-applications-auditevents",["sba-applications"]);t.sbaModules.push(i.name),i.controller("auditeventsCtrl",n(82)),i.component("sbaAuditevent",n(83)),i.config(["$stateProvider",function(t){t.state("applications.auditevents",{url:"/auditevents",templateUrl:"applications-auditevents/auditevents.html",controller:"auditeventsCtrl"})}]),i.run(["ApplicationViews","$sce","$http",function(t,e,n){t.register({order:55,title:e.trustAsHtml('<i class="fa fa-user-circle-o fa-fw"></i>Audit'),state:"applications.auditevents",show:function(t){return n.head("api/applications/"+t.id+"/auditevents").then(function(){return!0}).catch(function(){return!1})}})}])}).call(e,function(){return this}())},34:function(t,e){},82:function(t,e){"use strict";t.exports=["$scope","$http","application",function(t,e,n){"ngInject";var i=new Date;i.setMinutes(i.getMinutes()-30),i.setSeconds(0),i.setMilliseconds(0),t.auditevents=[],t.filter={principal:null,type:null,after:i},t.setFilter=function(e,n){t.filter[e]=n,t.search()};var a=function(t){return null!==t&&(t=t.trim(),""===t&&(t=null)),t},s=function(t){return null===t?t:t.toISOString().replace(/Z$/,"+0000").replace(/\.\d{3}\+/,"+")};t.search=function(){e({url:"api/applications/"+n.id+"/auditevents",method:"GET",params:{principal:a(t.filter.principal),type:a(t.filter.type),after:s(t.filter.after)}}).then(function(e){t.auditevents=e.data.events,t.error=null}).catch(function(e){t.error=e.data})},t.search()}]},83:function(t,e,n){"use strict";n(34),t.exports={bindings:{auditevent:"<value",filterCallback:"&filterCallback"},controller:function(){var t=this;t.show=!1,t.toggle=function(){t.show=!t.show},t.filterPrincipal=function(e){t.filterCallback()("principal",t.auditevent.principal),e.stopPropagation()},t.filterType=function(e){t.filterCallback()("type",t.auditevent.type),e.stopPropagation()},t.getStatusClass=function(){return/success/i.test(t.auditevent.type)?"success":/failure/i.test(t.auditevent.type)||/denied/i.test(t.auditevent.type)?"failure":"unknown"}},template:n(143)}},143:function(t,e){t.exports='<div class="auditevent" ng-class="$ctrl.getStatusClass()">\n  <div class="header" ng-click="$ctrl.toggle()">\n    <div class="time">{{$ctrl.auditevent.timestamp | date:\'HH:mm:ss.sss\'}}<br>\n      <small class="muted" ng-bind="$ctrl.event.timestamp | date:\'dd.MM.yyyy\'"></small>\n    </div>\n    <span class="title">{{$ctrl.auditevent.principal}}<button class="add-to-filter" ng-click="$ctrl.filterPrincipal($event)"><i class="fa fa-search-plus"></i></button></span>\n    <span class="title muted"> - {{$ctrl.auditevent.type}}<button class="add-to-filter" ng-click="$ctrl.filterType($event)"><i class="fa fa-search-plus"></i></button></span>\n  </div>\n  <pre ng-show="$ctrl.show" ng-bind="$ctrl.auditevent | json"></pre>\n</div>'}});