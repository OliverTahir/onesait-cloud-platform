(function () {
  'use strict';

  angular.module('dashboardFramework')
    .component('gadget', {
      templateUrl: 'app/components/view/gadgetComponent/gadget.html',
      controller: GadgetController,
      controllerAs: 'vm',
      bindings:{
        id:"<?",
        timesseriesconfig: "=?" ,       
        datastatus: "=?"
      }
    });

  /** @ngInject */
  function GadgetController($log, $scope, $element,$interval, $window, $mdCompiler, $compile, datasourceSolverService, httpService, interactionService, utilsService, leafletMarkerEvents, leafletData, urlParamService) {
    var vm = this;
    vm.ds = [];
    vm.type = "loading";
    vm.config = {};//Gadget database config
    vm.measures = [];
    vm.status = "initial";
    vm.selected = [];
    vm.notSmall=true;
    vm.showCheck = [];
    // color swatches >>> vm.swatches.global, vm.swatches.blues, vm.swatches.neutral
    vm.swatches = {};
    vm.swatches.global  = ['#FFEA7F','#FFF8D2','#F7AC6F','#FCE2CC','#E88AA2','#79C6B4','#CFEBE5','#639FCB','#C8DEED','#F7D6DF','#FDE3D4','#FEF6F0','#7874B4','#CFCEE5'];
    vm.swatches.neutral = ['#060E14','#F5F5F5','#6E767D','#A2ACB3','#D5DCE0','#F9F9FB'];
    vm.swatches.blues   = ['#2E6C99','#C0D3E0','#87BEE6','#E3EBF1','#639FCB'];

    

    vm.watchTrendArray=[];
    vm.trendInterval;
    vm.focusInterval=[];



    //Chaining filters, used to propagate own filters to child elements
    vm.filterChaining=true;

    vm.$onInit = function(){
      
      //register Gadget in interaction service when gadget has id
      if(vm.id){
        interactionService.registerGadget(vm.id);
      }   
      //Activate incoming events
      vm.unsubscribeHandler = $scope.$on(vm.id,eventGProcessor);     
      $scope.reloadContent();   
      if(typeof vm.timesseriesconfig==='undefined'){
        vm.timesseriesconfig = {
          startDate:moment().subtract(8,'hour'),
          endDate:moment(),
          realTime:false,
          timesSeriesinterval : 'h',
          timesSeriesintervalRealTime : 8
        }        
      }


      vm.watchTrendArray[0] =  $scope.$watch(function(scope) { return vm.timesseriesconfig.startDate.toISOString() },function(newValue, oldValue) {
      
      if(vm.measures!=null && vm.measures.length>0 && !vm.timesseriesconfig.realTime){
            console.log("startDate");
            vm.calculateIntervalPixels();
            datasourceSolverService.updateDatasourceTriggerAndRefresh(vm.id,[buildFilterTimesSeriesinterval(),buildFilterTimesSeriesIn(),buildFilterTrendStartDate(),buildFilterTrendEndDate()]);     
            console.log('onChangestartDate ' + oldValue + ' to ' + newValue);           
          }
        })
        vm.watchTrendArray[1] =  $scope.$watch(function(scope) { return vm.timesseriesconfig.endDate.toISOString() },function(newValue, oldValue) {
          if(vm.measures!=null && vm.measures.length>0 && !vm.timesseriesconfig.realTime){
            console.log("endDate");
            vm.calculateIntervalPixels();
            datasourceSolverService.updateDatasourceTriggerAndRefresh(vm.id,[buildFilterTimesSeriesinterval(),buildFilterTimesSeriesIn(),buildFilterTrendStartDate(),buildFilterTrendEndDate()]);     
            console.log('onChangeEndDate ' + oldValue + ' to ' + newValue);
          }
        })
        
        vm.watchTrendArray[2] =  $scope.$watch(function(scope) { return vm.timesseriesconfig.timesSeriesinterval },function(newValue, oldValue) {
          console.log("timesSeriesinterval");
          vm.calculateIntervalPixels();
          if(vm.measures!=null && vm.measures.length>0){
            datasourceSolverService.updateDatasourceTriggerAndRefresh(vm.id,[buildFilterTimesSeriesinterval(),buildFilterTimesSeriesIn(),buildFilterTrendStartDate(),buildFilterTrendEndDate()]); 
            console.log('onChangetimesSeriesinterval'+ vm.timesSeriesinterval);
          }
        })
        vm.watchTrendArray[3] =  $scope.$watch(function(scope) { return vm.timesseriesconfig.realTime },function(newValue, oldValue) {
          console.log("realTime");
          if(vm.measures!=null && vm.measures.length>0){
            datasourceSolverService.unregisterDatasourceTrigger(vm.measures[0].datasource.identification);
            $scope.reloadContent(); 
          }
        })
    
      
    }



    $scope.reloadContent = function(){  
          
      /*Gadget Editor Mode*/
      if(!vm.id){
       
        if(!vm.config.config){
          return;//Init editor triggered
        }
        if(typeof vm.config.config == "string"){
          vm.config.config = JSON.parse(vm.config.config);
        }
        //vm.measures = vm.gmeasures;//gadget config
        var projects = [];
        for(var index=0; index < vm.measures.length; index++){
          var jsonConfig = JSON.parse(vm.measures[index].config);
          for(var indexF = 0 ; indexF < jsonConfig.fields.length; indexF++){
            if(!utilsService.isSameJsonInArray( { op:"", field:jsonConfig.fields[indexF] },projects)){
              projects.push({op:"",field:jsonConfig.fields[indexF]});
            }
          }
          //add attribute for filter style marker to recover from datasource.
          if(vm.config.type=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
            projects.push({op:"",field:vm.config.config.markersFilter});
          }
          vm.measures[index].config = jsonConfig;
        }
        httpService.getDatasourceById(vm.ds).then(
          function(datasource){
            subscriptionDatasource(datasource.data, [], projects, []);
          }
        )
      }
      else{
      /*View Mode*/
        httpService.getGadgetConfigById(
          vm.id
        ).then(
          function(config){
            vm.config=config.data;            
            vm.config.config = JSON.parse(vm.config.config);
            return httpService.getGadgetMeasuresByGadgetId(vm.id);
          }
        ).then(
          function(measures){
            vm.measures = measures.data;

            vm.projects = [];
            for(var index=0; index < vm.measures.length; index++){
              var jsonConfig = JSON.parse(vm.measures[index].config);
              for(var indexF = 0 ; indexF < jsonConfig.fields.length; indexF++){
                if(!utilsService.isSameJsonInArray( { op:"", field:jsonConfig.fields[indexF] },vm.projects)){
                  vm.projects.push({op:"",field:jsonConfig.fields[indexF]});
                }
              }
               //add attribute for filter style marker to recover from datasource.
             if(vm.config.type=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
              vm.projects.push({op:"",field:vm.config.config.markersFilter});
             }
              vm.measures[index].config = jsonConfig;
            }
            httpService.getDatasourceById(vm.measures[0].datasource.id).then(
              function(datasource){
                subscriptionDatasource(datasource.data, [], vm.projects, []);
              }
            )
          }
        )
      }
    }

    vm.$onChanges = function(changes) {

    };

    vm.$onDestroy = function(){
      if(vm.unsubscribeHandler){
        vm.unsubscribeHandler();
        vm.unsubscribeHandler=null;
        datasourceSolverService.unregisterDatasourceTrigger(vm.measures[0].datasource.identification);
      }
       vm.cleanWatchTrendArray();
    }

  /*  vm.localeTableComparator = function(v1, v2) {
     
     console.log(vm.config.config.tablePagination.order);
      if (v1.type !== 'string' || v2.type !== 'string') {
        return (v1.index < v2.index) ? -1 : 1;
      }
  
      // Compare strings alphabetically, taking locale into account
      return v1.value.localeCompare(v2.value);
    };*/

    vm.toggleDecapite = function(){
      vm.config.config.tablePagination.options.decapitate = !vm.config.config.tablePagination.options.decapitate; 
    }

    vm.getValueOrder =  function (path) {
      return function (item) {
        var index="";
        var value="";
        if(typeof item !== "undefined" && Object.keys(item).length>0){
          if(typeof vm.config.config.tablePagination.order !== "undefined" && vm.config.config.tablePagination.order.charAt(0) === '-'){
            index=vm.config.config.tablePagination.order.substring(1,vm.config.config.tablePagination.order.length);
          }else if(typeof vm.config.config.tablePagination.order !== "undefined" && vm.config.config.tablePagination.order.charAt(0) !== '-'){
            index=vm.config.config.tablePagination.order.substring(0,vm.config.config.tablePagination.order.length)
          }else{
            index = Object.keys(item)[0];
          }
          value = item[index];
        }
        return value;
      }
    };

    function subscriptionDatasource(datasource, filter, project, group) {
      
      //Add parameters filters
      filter = urlParamService.generateFiltersForGadgetId(vm.id);
      if(vm.config.type=="trend" || vm.config.type=="pieTimesSeries"){       
       vm.initTrend(datasource, filter, project, group);
      }else{
        //clean watchs
        vm.cleanWatchTrendArray();
        datasourceSolverService.registerSingleDatasourceAndFirstShot(//Raw datasource no group, filter or projections
          {
            type: datasource.mode,
            name: datasource.identification,
            refresh: datasource.refresh,
            triggers: [{params:{filter:filter, group:group, project:project},emitTo:vm.id}]
          }
        );
      }
    };

    function processDataToGadget(data){ //With dynamic loading this will change
      
      switch(vm.config.type){
        case "line":
        case "bar":
        case "radar":
        case "pie":
          //Group X axis values
          var allLabelsField = [];
          for(var index=0; index < vm.measures.length; index++){
            allLabelsField = allLabelsField.concat(data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[0],ind)}));
          }
          if((typeof vm.config.config.scales === "undefined")||(typeof vm.config.config.scales["xAxes"][0].sort === "undefined")|| 
           (typeof vm.config.config.scales["xAxes"][0].sort) !== "undefined" && vm.config.config.scales["xAxes"][0].sort){
              allLabelsField = utilsService.sort_unique(allLabelsField);
           }
          //Match Y values
          var allDataField = [];//Data values sort by labels
          for(var index=0; index < vm.measures.length; index++){
            var dataRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[1],ind)});
            var labelRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[0],ind)});
            var sortedArray = [];
            for(var indexf = 0; indexf < dataRawSerie.length; indexf++){
              sortedArray[allLabelsField.indexOf(labelRawSerie[indexf])] = dataRawSerie[indexf];
            }
            allDataField.push(sortedArray);
          }

          vm.labels = allLabelsField;
          vm.series = vm.measures.map (function(m){return m.config.name});

          if(vm.config.type == "pie"){
            vm.data = allDataField[0];
          }
          else{
            vm.data = allDataField;
          }
        
          
          var baseOptionsChart = {           
            legend: {
                display: true, 
                fullWidth: false,
                position: 'top',      
                labels: {
                  padding: 10, 
                  fontSize: 11,
                  usePointStyle: false,
                  boxWidth:1
                }
              },
            elements: {
                arc: {
                    borderWidth: 1,
                    borderColor: '#fff'
                }
            },          
            maintainAspectRatio: false, 
            responsive: true, 
            responsiveAnimationDuration:500,
            circumference:  Math.PI,
            rotation: Math.PI,
            charType: 'pie'            
          };
          
          vm.datasetOverride = vm.measures.map (function(m){return m.config.config});
          vm.optionsChart = angular.merge({},vm.config.config,baseOptionsChart);
        

        // CONFIG FOR PIE/DOUGHNUT CHARTS
        if(vm.config.type == "pie"){

            try {
              // update legend display
              if( vm.config.config.legend.display !== undefined){ vm.optionsChart.legend.display = vm.config.config.legend.display;  } 

              // update data position 
              if( vm.config.config.legend.position !== undefined){ vm.optionsChart.legend.position = vm.config.config.legend.position;  } 

              // update data circunference 
              if( vm.config.config.circumference !== undefined){ vm.optionsChart.circumference = Number(vm.config.config.circumference);  } 
              
              // update data rotation 
              if( vm.config.config.rotation !== undefined){ vm.optionsChart.rotation = Number(vm.config.config.rotation);  } 

            } catch (error) {    } 

            
            // MERGE TOOLTIP CALLBACK ONLY FOR PIE/DOUGHNUT CHARTS
            var tooltips =  {              
              callbacks: {
                label: function(tooltipItem, data) {
                  var total = 0;
                  data.datasets[tooltipItem.datasetIndex].data.forEach(function(element /*, index, array*/ ) {
                    total += element;
                  });
                  var value = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
                  var percentTxt = Math.round(value / total * 100);
                  return data.labels[tooltipItem.index] + ': ' + data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index] + ' (' + percentTxt + '%)';
                }
              },
              xPadding: 10,
              yPadding: 16,
              backgroundColor: '#FFF',
              bodyFontFamily: 'Soho',
              bodyFontColor: '#555',
              displayColors: true,
              bodyFontSize: 11,
              borderWidth: 1,
              borderColor: '#CCC'              
            };
            // add tooltip to pie/doughtnut conf.
            vm.optionsChart.tooltips = tooltips;
          
        }   
         

          if(vm.config.type==="line"||vm.config.type==="bar"){   
            
            try {
              // update legend display
              if( vm.config.config.legend.display !== undefined){ vm.optionsChart.legend.display = vm.config.config.legend.display;  } 

              // update data position 
              if( vm.config.config.legend.position !== undefined){ vm.optionsChart.legend.position = vm.config.config.legend.position;  } 
             
            } catch (error) {    } 


              //Ticks options
              vm.optionsChart.scales.xAxes[0].ticks={
                callback: function(dataLabel, index) {									
                  if(typeof vm.optionsChart.scales.xAxes[0].hideLabel ==="undefined"){return index % 1 === 0 ? dataLabel : '';}
                  else{
                    return index % vm.optionsChart.scales.xAxes[0].hideLabel === 0 ? dataLabel : '';
                  }
                }
              }
              
              var linebarTooltips = {
                bodySpacing : 15,
                xPadding: 10,
                yPadding: 16,
                titleFontColor: '#6E767D',
                backgroundColor: '#F9F9FB',
                bodyFontFamily: 'Soho',
                bodyFontColor: '#555',
                displayColors: true,
                bodyFontSize: 11,
                titleMarginBottom: 8,                
                callbacks: { 
                  label: function(tooltipItem, chart){ 
                   var datasetLabel = chart.datasets[tooltipItem.datasetIndex].label || ''; 
                   return datasetLabel + ': ' + formatNumber(tooltipItem.yLabel, 0,'',''); 
                    // TO-DO: return decimals, thousands and decimal sep.
                    // *  example: formatNumber(1234.56, 2, '.', ','); 
                    // *  return: '1.234,56' 
                  } 
                 } 
              };
              
              // add tooltip to line/bar 
              vm.optionsChart.tooltips = linebarTooltips;     

            }
          break;
          case "mixed":
          //Group X axis values
          var allLabelsField = [];
          for(var index=0; index < vm.measures.length; index++){
            allLabelsField = allLabelsField.concat(data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[0],ind)}));
          }
          if((typeof vm.config.config.scales["xAxes"][0].sort === "undefined")|| 
          (typeof vm.config.config.scales["xAxes"][0].sort) !== "undefined" && vm.config.config.scales["xAxes"][0].sort){
            allLabelsField = utilsService.sort_unique(allLabelsField);
          }
          //Match Y values
          var allDataField = [];//Data values sort by labels
          for(var index=0; index < vm.measures.length; index++){
            var dataRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[1],ind)});
            var labelRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[0],ind)});
            var sortedArray = [];
            for(var ind=0; ind < vm.measures.length; ind++){
              sortedArray[ind]=null;
            }
            for(var indexf = 0; indexf < dataRawSerie.length; indexf++){
              sortedArray[allLabelsField.indexOf(labelRawSerie[indexf])] = dataRawSerie[indexf];
            }
            allDataField.push(sortedArray);
          }

          vm.labels = allLabelsField;
          vm.series = vm.measures.map (function(m){return m.config.name});

        
            vm.data = allDataField;
        

          var baseOptionsChart = {
            legend: {
              display: true, 
              labels: {
                boxWidth: 11
              }
            }, 
            maintainAspectRatio: false, 
            responsive: true, 
            responsiveAnimationDuration:500
          };

          vm.datasetOverride = vm.measures.map (function(m){
            if(m.config.config.type==='line'){
              return m.config.config;
            }else if(m.config.config.type==='bar'){
              return m.config.config;
            }else if(m.config.config.type==='points'){
              m.config.config.type= 'line';
              m.config.config.borderWidth= 0;
              if(typeof m.config.config.pointRadius ==="undefined" ||m.config.config.pointRadius<1 ){
                m.config.config.pointRadius=4;
              }
              m.config.config.showLine=false;              
              return m.config.config;
            }
            return m.config.config});
          vm.optionsChart = angular.merge({},vm.config.config,baseOptionsChart); 
         //Ticks options
          vm.optionsChart.scales.xAxes[0].ticks={
            callback: function(dataLabel, index) {									
              if(typeof vm.optionsChart.scales.xAxes[0].hideLabel ==="undefined"){return index % 1 === 0 ? dataLabel : '';}
              else{
                return index % vm.optionsChart.scales.xAxes[0].hideLabel === 0 ? dataLabel : '';
              }
            }
          } 
          //tooltips options
          vm.optionsChart.tooltips= {
            callbacks: {
                label: function(tooltipItem, data) {               
                    var label = data.datasets[tooltipItem.datasetIndex].label || '';
                    if (label) {
                        label += ': ';
                    }
                    if(!isNaN(tooltipItem.yLabel)){
                      label += tooltipItem.yLabel;
                    }else{
                      label ='';
                    }
                    return label;
                
              }
            }
        }   
          break;
        case 'wordcloud':
          //Get data in an array
          var arrayWordSplited = data.reduce(function(a,b){return a.concat(( utilsService.getJsonValueByJsonPath(b,utilsService.replaceBrackets(vm.measures[0].config.fields[0]),0)))},[])//data.flatMap(function(d){return getJsonValueByJsonPath(d,vm.measures[index].config.fields[0]).split(" ")})
          var hashWords = {};
          var counterArray = []
          for(var index = 0; index < arrayWordSplited.length; index++){
            var word = arrayWordSplited[index];
            if(word in hashWords){
              counterArray[hashWords[word]].count++;
            }
            else{
              hashWords[word]=counterArray.length;
              counterArray.push({text:word,count:1});
            }
          }

          vm.counterArray = counterArray.sort(function(a, b){
            return b.count - a.count;
          })
          redrawWordCloud();
          $scope.$on("$resize",redrawWordCloud);
          break;
        case "map":
        
         /* leafletData.getDirectiveControls('lmap' + vm.id).then(function (controls) {
            if(controls.markers){
              controls.markers.clean();
            }
          });*/

          vm.center = vm.center || vm.config.config.center;
          //IF defined intervals for marker 
          if(typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
            var jsonMarkers = JSON.parse(vm.config.config.jsonMarkers);
            
            vm.markers = data.map(
              function(d){
                return {
                  lat: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[0]),0)),
                  lng: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[1]),1)),
  
                  message: vm.measures[0].config.fields.slice(3).reduce(
                    function(a, b){
                      return a + "<b>" + b + ":</b>&nbsp;" + utilsService.getJsonValueByJsonPath(d,b) + "<br/>";
                    }
                    ,""
                  ),
                  id: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[2],2),
                  icon: utilsService.getMarkerForMap(utilsService.getJsonValueByJsonPath(d,vm.config.config.markersFilter,2),jsonMarkers),
                }
              }
            )
          
          }else{
          vm.markers = data.map(
            function(d){
              return {
                lat: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[0]),0)),
                lng: parseFloat(utilsService.getJsonValueByJsonPath(d,utilsService.replaceBrackets(vm.measures[0].config.fields[1]),1)),

                message: vm.measures[0].config.fields.slice(3).reduce(
                  function(a, b){
                    return a + "<b>" + b + ":</b>&nbsp;" + utilsService.getJsonValueByJsonPath(d,b) + "<br/>";
                  }
                  ,""
                ),
                id: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[2],2)
               
              }
            }
          )
        }

          $scope.events = {
            markers: {
                enable: leafletMarkerEvents.getAvailableEvents(),
            }
          };
          
          //Init map events
          var eventName = 'leafletDirectiveMarker.lmap' + vm.id + '.click';
          $scope.$on(eventName, vm.clickMarkerMapEventProcessorEmitter);
          
          redrawLeafletMap();
          $scope.$on("$resize",redrawLeafletMap);
          break;
          case "table":
          vm.data=data;
          if(data.length>0){
            var listMeasuresFields=[];
            var measures = orderTable(vm.measures);
            for (var index = 0; index < measures.length; index++) {
              measures[index].config.order=  measures[index].config.fields[0];

              var tokenizer = measures[index].config.fields[0].split(".");
              var last = tokenizer[tokenizer.length-1];
              if(last.indexOf('[') > -1){
                last = last.substring(
                  last.lastIndexOf("[") + 1, 
                  last.lastIndexOf("]"));
              }
              var proyected = {order: measures[index].config.fields[0],value:last};
              listMeasuresFields.push(proyected);
              measures[index].config.last=  last;
              if(typeof measures[index].config.name === "undefined" || measures[index].config.name.trim() === "" ){
                measures[index].config.name = last;
              }
            }
            vm.data = data.map(function (data, index, array) {
              var obj={};
                for (var i = 0; i < listMeasuresFields.length; i++) {
                  obj[listMeasuresFields[i].order]=utilsService.getJsonValueByJsonPath(data,utilsService.replaceBrackets(listMeasuresFields[i].order),index);
                }
              
              return obj;           
          });   
          }          
          vm.config.config.tablePagination.limitOptions = vm.config.config.tablePagination.options.limitSelect ? [5, 10, 20, 50 ,100]  : undefined;
          redrawTable();
          $scope.$on("$resize",redrawTable);
          break;
          case "trend":
          vm.optionsChart = vm.config.config;
       
          var allLabelsField = [];
         // for(var index=0; index < vm.measures.length; index++){
            //allLabelsField = allLabelsField.concat(data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[0],ind)}));
         //   allLabelsField = allLabelsField.concat(data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,"timestamp.$date",ind)}));
         // }

          allLabelsField = getTimestampForXAxis(data);

          vm.optionsChart.chart.xAxis= {
            //axisLabel: 'X Axis',
            showMaxMin: false,
            rotateLabels:-10,
            fontSize:11,
            tickFormat: function(d) {
                
                var dx = allLabelsField[d] ;
                if (dx !=null) {
                  if (Number(dx)) { return d3.format('.02f')(dx); }
                  else { return vm.formatLabel(dx) }
                }
                return null;
            }
        };
        vm.optionsChart.chart.x2Axis= {
          showMaxMin: false,          
          fontSize:11,
          tickFormat: function(d) {
            
              var dx = allLabelsField[d] ;
              if (dx !=null) {
                if (Number(dx)) { return d3.format('.02f')(dx); }
                else { return vm.formatLabel(dx)  }
              }
              return null;
            }
        };

        vm.optionsChart.chart.yAxis={
          //axisLabel: 'Y Axis',
          tickFormat: function(dx){
             if (dx !=null) {
                 if (Number(dx)) { return d3.format('.02f')(dx); }
                else { return dx }
            }
            return null;
          }
      }
      vm.optionsChart.chart.y2Axis={      
        tickFormat: function(dx){
           if (dx !=null) {
               if (Number(dx)) { return d3.format('.02f')(dx); }
              else { return dx }
          }
          return null;
        }
    };
    //cubic interpolation that preserves monotonicity in y.
    vm.optionsChart.chart.interpolate="monotone";
    vm.optionsChart.chart.duration=0;
      
     
      var datformated = []                   
        for(var index = 0; index < vm.measures.length; index++){
          var hashMap = [];
          for(var i=0; i < data.length; i++){
            if(data[i].signalId===vm.measures[index].config.fields[1]){
              for(var j=0; j < data[i].values.length; j++){
              hashMap[data[i].values[j].timestamp.$date]={y:data[i].values[j].value,label:data[i].values[j].timestamp.$date};
            }
        }
        }
        var dat = {"key":vm.measures[index].config.name, "values":[]};
        for(var ind=0; ind < allLabelsField.length; ind++){
        var a=hashMap[allLabelsField[ind]];
          if(typeof a !== "undefined"){
            a.x = ind;
            dat.values.push(a);
          }
        }
        datformated.push(dat);
      }
      var stateLegends=[];
      try {
  
      var datLege = vm.api.getScope().data;
      for (var index = 0; index < datLege.length; index++) {
        var element = datLege[index];
        if(typeof element.disabled != 'undefined'){
          stateLegends[index]=element.disabled;
        }else{
          stateLegends[index]=false;
        }
        
      }
  
      } catch (error) {
        
      }       
          vm.data = datformated;
      try {     
        if(stateLegends.length>0){
          for (var index = 0; index < stateLegends.length; index++) {
            vm.data[index].disabled=stateLegends[index];
          }
        }
      } catch (error) {
          
      }    
       /* ExampleData
          vm.data= [{
          key: "Cumulative Return",
          values: [
              { "x" : 0 , "y" : -29.765957771107,"label":"2018-08-06T00:00:00Z" },
              { "x" : 1 , "y" : 0,"label":"2018-08-06T00:00:00Z" }             
          ]
      }];*/
      redrawTrend();
      $scope.$on("$resize",redrawTrend);     
      break;
     
      case "pieTimesSeries":
      vm.optionsChart = vm.config.config;
   
      var allLabelsField = [];
     for(var index = 0; index < vm.measures.length; index++){ 
      allLabelsField.push( vm.measures[index].config.fields[1]);
     }
     //set labels
     vm.labels = allLabelsField;
    

      var baseOptionsChart = {           
        legend: {
            display: true, 
            fullWidth: false,
            position: 'top',      
            labels: {
              padding: 10, 
              fontSize: 11,
              usePointStyle: false,
              boxWidth:1
            }
          },
        elements: {
            arc: {
                borderWidth: 1,
                borderColor: '#fff'
            }
        },          
        maintainAspectRatio: false, 
        responsive: true, 
        responsiveAnimationDuration:500,
        circumference:  Math.PI,
        rotation: Math.PI,
        charType: 'pie'            
      };
      
     
      vm.optionsChart = angular.merge({},vm.config.config,baseOptionsChart);
    

    // CONFIG FOR PIE/DOUGHNUT CHARTS
           try {
          // update legend display
          if( vm.config.config.legend.display !== undefined){ vm.optionsChart.legend.display = vm.config.config.legend.display;  } 

          // update data position 
          if( vm.config.config.legend.position !== undefined){ vm.optionsChart.legend.position = vm.config.config.legend.position;  } 

          // update data circunference 
          if( vm.config.config.circumference !== undefined){ vm.optionsChart.circumference = Number(vm.config.config.circumference);  } 
          
          // update data rotation 
          if( vm.config.config.rotation !== undefined){ vm.optionsChart.rotation = Number(vm.config.config.rotation);  } 

        } catch (error) {    } 

        
        // MERGE TOOLTIP CALLBACK ONLY FOR PIE/DOUGHNUT CHARTS
        var tooltips =  {              
          callbacks: {
            label: function(tooltipItem, data) {
              var total = 0;
              data.datasets[tooltipItem.datasetIndex].data.forEach(function(element /*, index, array*/ ) {
                total += element;
              });
              var value = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
              var percentTxt = Math.round(value / total * 100);
              return data.labels[tooltipItem.index] + ': ' + data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index] + ' (' + percentTxt + '%)';
            }
          },
          xPadding: 10,
          yPadding: 16,
          backgroundColor: '#FFF',
          bodyFontFamily: 'Soho',
          bodyFontColor: '#555',
          displayColors: true,
          bodyFontSize: 11,
          borderWidth: 1,
          borderColor: '#CCC'              
        };
        // add tooltip to pie/doughtnut conf.
        vm.optionsChart.tooltips = tooltips;
      
     
     
 
  var datformated = []                   
    for(var index = 0; index < vm.measures.length; index++){
      var average = 0;
      for(var i=0; i < data.length; i++){
        if(data[i].signalId===vm.measures[index].config.fields[1]){
          for(var j=0; j < data[i].values.length; j++){
            average = average+Number(data[i].values[j].value);          
        }
        if(average != 0 && data[i].values.length > 0){
          average = average / data[i].values.length;
        }
    }
    }   
    datformated.push(Math.round(average * 100) / 100);
  }
      vm.data = datformated;
   
   /* ExampleData
      vm.labels = ["signal_a","signal_b","signal_c"]
      vm.data = [100,200,300]
     */
  
  
  break;
  }
      vm.type = vm.config.type;//Activate gadget
      utilsService.forceRender($scope);
    }


    function orderTable(measures){
		
      var neworder = measures.sort(function (a,b){
        var a = Number(a.config.config.position);			
        var b = Number(b.config.config.position);		
        return a-b;
      });
      return neworder;
      
    }

    function redrawWordCloud(){
      var element = $element[0];
      var height = element.offsetHeight;
      var width = element.offsetWidth;
      var maxCount = vm.counterArray[0].count;
      var minCount = vm.counterArray[vm.counterArray.length - 1].count;
      var maxWordSize = width * 0.15;
      var minWordSize = maxWordSize / 5;
      var spread = maxCount - minCount;
      if (spread <= 0) spread = 1;
      var step = (maxWordSize - minWordSize) / spread;
      vm.words = vm.counterArray.map(function(word) {
          return {
              text: word.text,
              size: Math.round(maxWordSize - ((maxCount - word.count) * step)),
              tooltipText: word.count + ' ocurrences'
          }
      })
      vm.width = width;
      vm.height = height;
    }

    function redrawTrend(){ 
      try {      
        var element = $element[0];
        var height = element.offsetHeight;
        var width = element.offsetWidth - 10;     
        vm.optionsChart.chart.width = width;
        vm.optionsChart.chart.height = height-10;
      
        if(vm.focusInterval.length>0){
          vm.optionsChart.chart.brushExtent=vm.focusInterval;
        }else{
          vm.optionsChart.chart.brushExtent=null;
        }
          
        vm.optionsChart.chart.focus={dispatch:{
          brush:function(t){
              vm.focusInterval=t.extent;         
              if(t.brush.empty()){            
                vm.focusInterval=[];
              }
          }}}   
          
         


            
        if(vm.optionsChart.chart.height>370){
          vm.optionsChart.chart.focusEnable=true;
        }else{
          vm.optionsChart.chart.focusEnable=false;
        }
        if(typeof vm.api != "undefined"){
          vm.api.refresh();
          //remove tooltips
          $('.nvtooltip.xy-tooltip').remove();
        }
      } catch (error) {          
      }     
    }

   

    function redrawTable(){
     var element = $element[0];   
      var width = element.offsetWidth;
      
      if(width<600){
        vm.notSmall=false;
      }else{
        vm.notSmall=true;
      }
    }


    function redrawLeafletMap(){
      
      var element = $element[0];
      var height = element.offsetHeight;
      var width = element.offsetWidth;
      vm.width = width;
      vm.height = height;
      
    }

    function eventGProcessor(event,dataEvent){            
      if(dataEvent.type === "data" && dataEvent.data.length===0 && vm.type !=="trend" && vm.type !=="pieTimesSeries"){
        vm.type="nodata";
        vm.status = "ready";
      }
      else{
        switch(dataEvent.type){
          case "data":
            switch(dataEvent.name){ 
              case "refresh":
                if(vm.status === "initial" || vm.status === "ready"){
                  processDataToGadget(dataEvent.data);
                }
                else{
                  console.log("Ignoring refresh event, status " + vm.status);
                }
                break;
              case "add":
                //processDataToGadget(data);
                break;
              case "filter":
                if(vm.status === "pending"){
                  processDataToGadget(dataEvent.data);
                  vm.status = "ready";
                }
                break;
              case "drillup":
                //processDataToGadget(data);
                break;
              case "drilldown":
                //processDataToGadget(data);
                break;
              default:
                console.error("Not allowed data event: " + dataEvent.name);
                break;
            } 
            break;
          case "filter":
            vm.status = "pending";
            //vm.type = "loading";
            if(!vm.datastatus){
              vm.datastatus = {};
            }
            if(dataEvent.data.length){
              for(var index in dataEvent.data){
                vm.datastatus[angular.copy(dataEvent.data[index].field)] = {
                  value: angular.copy(dataEvent.data[index].value),
                  id: angular.copy(dataEvent.id)
                }
              }             
               
            }
            else{
              delete vm.datastatus[dataEvent.field];
              if(Object.keys(vm.datastatus).length === 0 ){
                vm.datastatus = undefined;
              }               
            }           
            datasourceSolverService.updateDatasourceTriggerAndShot(vm.id,buildFilterStt(dataEvent));
     
            break;
          default:
            console.error("Not allowed event: " + dataEvent.type);
            break;
        }
      }
      utilsService.forceRender($scope);
    }

    function buildFilterStt(dataEvent){
      return {
        filter: {
          id: dataEvent.id,
          data: dataEvent.data.map(
            function(f){
              //quotes for string identification
              if(typeof f.value === "string"){
                f.value = "\"" + f.value + "\""
              }
              return {
                field: f.field,
                op: "=",
                exp: f.value
              }
            }
          )
        } , 
        group:[], 
        project:vm.projects
      }
    }

    //Chartjs click event
    vm.clickChartEventProcessorEmitter = function(points, evt){
      var originField;
      var originValue;
      if(typeof points[0]!=='undefined'){
        switch(vm.config.type){          
          case "bar":
            //find serie x field if there are diferent x field in measures
            for(var index in vm.data){
              if(vm.data[index][points[0]._index]){
                originField = vm.measures[index].config.fields[0];
                break;
              }
            }
            originValue = points[0]._model.label;
            break;
            case "line":
            case "mixed":
            //find serie x field if there are diferent x field in measures
            for(var index in vm.data){
              if(vm.data[index][points[0]._index]){
                originField = vm.measures[index].config.fields[0];
                break;
              }
            }
            originValue = points[0]._chart.config.data.labels[points[0]._index];
            break;
            case "radar":
            //find serie x field if there are diferent x field in measures
            for(var index in vm.data){
              if(vm.data[index][points[0]._index]){
                originField = vm.measures[index].config.fields[0];
                break;
              }
            }
            originValue = points[0]._chart.config.data.labels[points[0]._index];
            break;
          case "pie":
            originField = vm.measures[0].config.fields[0];
            originValue = points[0]._model.label;
            break;
        }
        sendEmitterEvent(originField,originValue);
      }
    }


 //word-cloud click event
 vm.clickWordCloudEventProcessorEmitter = function(word){
  var originField = vm.measures[0].config.fields[0];
  var originValue = word.text;
  
  sendEmitterEvent(originField,originValue);
}



    //leafletjs click marker event, by Point Id
    vm.clickMarkerMapEventProcessorEmitter = function(event, args){
      var originField = vm.measures[0].config.fields[2];
      var originValue = args.model.id;
      sendEmitterEvent(originField,originValue);
    }

    vm.selectItemTable = function (item) {
      
      console.log(item, 'was selected');
      for (var index = 0; index < vm.measures.length; index++) {
        var element = vm.measures[index];
        var originField = element.config.fields[0];
        var originValue = item[element.config.order];
        sendEmitterEvent(originField,originValue);
      }      
    };
  

    function sendEmitterEvent(originField,originValue){
      var filterStt = angular.copy(vm.datastatus)||{};     
      filterStt[originField]={value: originValue, id: vm.id};
      interactionService.sendBroadcastFilter(vm.id,filterStt);
    };

    vm.classPie = function () {
      if (vm.config.config.charType === undefined){ return true; } else {
          if (vm.config.config.charType === 'pie'){ return true; } else { return false; }
      }
    };
    
    
    function formatNumber(number, decimals, dec_point, thousands_sep) { 
      // *  example: formatNumber(1234.56, 2, ',', '.'); 
      // *  return: '1.234,56' 
          number = (number + '').replace(',', '').replace(' ', ''); 
          var n = !isFinite(+number) ? 0 : +number, 
            prec = !isFinite(+decimals) ? 0 : Math.abs(decimals), 
            sep = (typeof thousands_sep === 'undefined') ? '.' : thousands_sep, 
            dec = (typeof dec_point === 'undefined') ? ',' : dec_point, 
            s = '', 
            toFixedFix = function (n, prec) { 
             var k = Math.pow(10, prec); 
             return '' + Math.round(n * k)/k; 
            }; 
          // Fix for IE parseFloat(0.55).toFixed(0) = 0; 
          s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.'); 
          if (s[0].length > 3) { 
           s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep); 
          } 
          if ((s[1] || '').length < prec) { 
           s[1] = s[1] || ''; 
           s[1] += new Array(prec - s[1].length + 1).join('0'); 
          } 
          return s.join(dec); 
      } 


    function generateTrendFilter(){
      var filters =[];
      var filtertimesSeriesinterval = {
        id: vm.id+"timesSeriesinterval",
        field: "timesSeriesinterval",
        op: "=",
        exp: "\'" + vm.timesseriesconfig.timesSeriesinterval + "\'",
        data:[{
        field: "timesSeriesinterval",
        op: "=",
        exp: "\'" + vm.timesseriesconfig.timesSeriesinterval + "\'"}
        ]
      
      };
      filters.push(filtertimesSeriesinterval);      
      var filtertimesSeriesIn = {
        id: vm.id+"signalId",
        field: "signal.signalId",
        op: "IN",
        exp: "(" +createTrendIn() + ")",
        data:[{
        field: "signal.signalId",
        op: "IN",
        exp: "(" +createTrendIn() + ")"}
        ]      
      };
      filters.push(filtertimesSeriesIn);
      var filterStart = {
        id: vm.id+"StartDate",
        field: vm.measures[0].config.fields[0],
        op: ">=",
        exp: "\'" + vm.timesseriesconfig.startDate.toISOString() + "\'",
        data:[{
        field: vm.measures[0].config.fields[0],
        op: ">=",
        exp: "\'" + vm.timesseriesconfig.startDate.toISOString() + "\'"}
        ]
      
      };
      filters.push(filterStart);
      var filterEnd = {
        id: vm.id+"EndDate",
        field: vm.measures[0].config.fields[0],
        op: "<=",
        exp: "\'" +vm.timesseriesconfig.endDate.toISOString() + "\'"
        ,
        data:[{
        field: vm.measures[0].config.fields[0],
        op: "<=",
        exp: "\'" + vm.timesseriesconfig.endDate.toISOString() + "\'"}
        ]
      
      };
      filters.push(filterEnd);
      return filters;
    }

    function buildFilterTrendStartDate(){
      return {
        filter: {
          id: vm.id+"StartDate",
          data:[{
            field: vm.measures[0].config.fields[0],
            op: ">=",
            exp: "\'" + vm.timesseriesconfig.startDate.toISOString() + "\'"}
            ] , 
        group:[], 
        project:vm.projects
      }
    }
  }
    function buildFilterTrendEndDate(){
      return {
        filter: {
          id: vm.id+"EndDate",
          data:[{
            field: vm.measures[0].config.fields[0],
            op: "<=",
            exp: "\'" + vm.timesseriesconfig.endDate.toISOString() + "\'"}
            ] , 
        group:[], 
        project:vm.projects
      }
    }
  }
  function buildFilterTimesSeriesinterval(){
    return {
      filter: {
        id: vm.id+"timesSeriesinterval",
        data:[{
          field: "timesSeriesinterval",
          op: "=",
          exp: "\'" + vm.timesseriesconfig.timesSeriesinterval + "\'"}
          ] , 
      group:[], 
      project:vm.projects
    }
  }
}

function buildFilterTimesSeriesIn(){
  return {
    filter: {
      id: vm.id+"signalId",
      data:[{
        field: "signal.signalId",
        op: "IN",
        exp: "(" +createTrendIn() + ")"}
        ] , 
    group:[], 
    project:vm.projects
  }
}
}

function createTrendIn(){
  var signals =[];
  for(var index = 0; index < vm.measures.length; index++){
    signals.push("'"+vm.measures[index].config.fields[1]+"'");
  }
 return signals.join(",");
}

function getTimestampForXAxis(data){
  var allLabelsField = [];
          for(var i=0; i < data.length; i++){
            for(var j=0; j < data[i].values.length; j++){
              allLabelsField.push(data[i].values[j].timestamp.$date);
            }
          }
          allLabelsField = utilsService.sort_unique(allLabelsField);
          return allLabelsField;
}



vm.initTrend = function(datasource, filter, project, group){
  //clear the interval if exist
  if(vm.trendInterval!=null){    
    $interval.cancel(vm.trendInterval);
  }
  //Check if is realtime
  if(vm.timesseriesconfig.realTime){
    //calc initial interval
    vm.timesseriesconfig.startDate = moment().subtract(vm.timesseriesconfig.timesSeriesintervalRealTime,'hour');
    vm.timesseriesconfig.endDate = moment();
    vm.calculateIntervalPixels();
    filter = generateTrendFilter();
    var refreshTemp = 0;
    if(datasource.refresh === 0){
      refreshTemp = 4;
    }else{
      refreshTemp = datasource.refresh;
    }
    datasourceSolverService.registerSingleDatasourceAndFirstShot(//Raw datasource no group, filter or projections
      {
        type: datasource.mode,
        name: datasource.identification,
        refresh: 0,
        triggers: [{params:{filter:filter, group:group, project:project},emitTo:vm.id}]
      }
    );
   
    vm.trendInterval = $interval(/*Datasource passed as parameter in order to call every refresh time*/
      function(){        
        vm.timesseriesconfig.startDate = moment().subtract(vm.timesseriesconfig.timesSeriesintervalRealTime,'hour');
        vm.timesseriesconfig.endDate = moment();
        vm.calculateIntervalPixels();
        datasourceSolverService.updateDatasourceTriggerAndRefresh(vm.id,[buildFilterTimesSeriesinterval(),buildFilterTimesSeriesIn(),buildFilterTrendStartDate(),buildFilterTrendEndDate()]);
      },refreshTemp * 1000, 0, true, datasource
    );
  }else{
    filter = generateTrendFilter();
    datasourceSolverService.registerSingleDatasourceAndFirstShot(//Raw datasource no group, filter or projections
      {
        type: datasource.mode,
        name: datasource.identification,
        refresh: 0,
        triggers: [{params:{filter:filter, group:group, project:project},emitTo:vm.id}]
      }
    );
   
    try {
      vm.focusInterval=[];
      vm.optionsChart.chart.brushExtent=null;
    } catch (error) {
      
    }
   
    vm.trendInterval = $interval(/*Datasource passed as parameter in order to call every refresh time*/
      function(){        
        redrawTrend();
      },4 * 1000, 0, true, datasource
    );
  }
}


vm.calculateIntervalPixels= function(){
  var ms = vm.timesseriesconfig.endDate.diff(vm.timesseriesconfig.startDate);
  var d = moment.duration(ms);

  var width = 300;
  
  if(vm.config.type == "trend" && vm.optionsChart!=null){
    width = vm.optionsChart.chart.width;
  }
  if(width > Math.floor(d.asSeconds())){
    vm.timesseriesconfig.timesSeriesinterval = 's';
  }else if (width > Math.floor(d.asMinutes())){
    vm.timesseriesconfig.timesSeriesinterval = 'm';
  }else if (width > Math.floor(d.asHours())){
    vm.timesseriesconfig.timesSeriesinterval = 'h';
  }else {
    vm.timesseriesconfig.timesSeriesinterval = 'd';
  }
}


vm.formatLabel= function(label){
  var date = moment(label);
  if(vm.timesseriesconfig.timesSeriesinterval=='s'){
    return date.utc().format('mm:ss');
  }else if(vm.timesseriesconfig.timesSeriesinterval=='m'){
    return date.utc().format('HH:mm:ss');
  }else if(vm.timesseriesconfig.timesSeriesinterval=='h'){
    return date.utc().format('MM-DD HH:mm');
  }else {
    return date.utc().format('YYYY-MM-DD');
  } 
}



vm.cleanWatchTrendArray = function(){
  vm.watchTrendArray.forEach(function(item, index){item();})
}
}
})();
