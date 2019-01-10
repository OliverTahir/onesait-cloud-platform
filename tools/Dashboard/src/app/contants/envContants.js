var env = {};

// Import variables if present (from env.js)
if(window && window.__env){
  Object.assign(env, window.__env);
}
else{//Default config
  env.socketEndpointConnect = '/dashboardengine/dsengine/solver';
  env.socketEndpointSend = '/dsengine/solver';
  env.socketEndpointSubscribe = '/dsengine/broker';
  env.endpointControlPanel = '/controlpanel';
  env.endpointDashboardEngine = '/dashboardengine';
  env.enableDebug = false;
  env.dashboardEngineUsername = '';
  env.dashboardEnginePassword = '';
  env.dashboardEngineOauthtoken = '';
  env.dashboardEngineLoginRest = '/loginRest';
  env.dashboardEngineLoginRestTimeout = 5000;
  env.restUrl = '';
  env.urlParameters ={};
  env.inIframe = false;
}

angular.module('dashboardFramework').constant('__env', env);
