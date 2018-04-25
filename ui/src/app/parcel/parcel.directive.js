
import parcelFieldsetTemplate from './parcel-fieldset.tpl.html';
import Action from './action';


/* eslint-enable import/no-unresolved, import/default */
/*global google*/
/*@ngInject*/
export default function ParcelDirective($compile, $templateCache, toast, $translate, types, parcelService, farmService, customerService, $log) {
    "use strict"
    var linker = function (scope, element) {
        var template = $templateCache.get(parcelFieldsetTemplate);
        element.html(template);

        scope.types = types;
        scope.isAssignedToCustomer = false;
        scope.isPublic = false;
        scope.assignedCustomer = null;
        farmService.getAllFarms().then(function(result){
            $log.log(result[0]);
            scope.farms=result;
        });

        scope.$watch('parcel', function(newVal) {
            if (newVal) {
                if (scope.parcel.customerId && scope.parcel.customerId.id !== types.id.nullUid) {
                    scope.isAssignedToCustomer = true;
                    customerService.getShortCustomerInfo(scope.parcel.customerId.id).then(
                        function success(customer) {
                            scope.assignedCustomer = customer;
                            scope.isPublic = customer.isPublic;
                        }
                    );
                } else {
                    scope.isAssignedToCustomer = false;
                    scope.isPublic = false;
                    scope.assignedCustomer = null;
                }
            }
        });

        scope.onParcelIdCopied = function() {
            toast.showSuccess($translate.instant('parcel.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };


        $compile(element.contents())(scope);

        //-----------------------------------------------Class-------------------------------------------------------------

        function Crop(){
            this.name = '';
            this.why = '';
            this.cause = '';
            this.startCrop = new Date();
            this.weekens = 0;
            this.initialConditions = '';
            this.actions = [];
            this.finish = false;
            this.state = '';
        }

        function Area(){
            this.extension=0.0;
            this.symbol='';

        }

        function GroundFeatures(){
            this.density = '';
            this.compaction = '';
            this.inclination = '';
            this.higrologicData = '';
        }

        //-----------------------------------------------------------------------------------------------------------------
        scope.symbol = ['ha','fg'];

        scope.finishCrop = function(){
            scope.parcel.crop.finish = true;
            scope.parcel.cropsHistory.push(scope.parcel.crop);
            scope.parcel.crop = new Crop();
        };

        scope.action = '';
        scope.addActionCrop = function(){
          var newAction = new Action();
          newAction.action = scope.action;
          scope.parcel.crop.actions.push(newAction);
          scope.action = '';
        };


        scope.someCrop = function(){
           var crop = false;
            if(scope.parcel.name == null) {
                scope.parcel.crop = new Crop();
                scope.parcel.cropsHistory = [];
            }else{
                crop = true;
            }
            return crop;
        };

        var map;
        var drawMapFarm = [];
        var drawMapParcel = [];
        function Polygon() {
            this.coordinates = [];
            this.type = 'Polygon';
        }
        scope.mostrarMapaParcel =function (){
            scope.cropFarm = farmService.getFarm(scope.parcel.farmId).then(function(result){
                var polygon = new Polygon();
                scope.tempLatitude = -34.397;
                scope.tempLongitude = 150.644;
                scope.cropFarm = result;
                if(scope.cropFarm.location.coordinates.length > 0){
                    scope.tempLatitude = scope.cropFarm.location.coordinates[0][1];
                    scope.tempLongitude = scope.cropFarm.location.coordinates[0][0];
                    for(var i=0; i<scope.cropFarm.location.coordinates.length; i++){
                        drawMapFarm.push({lat: scope.cropFarm.location.coordinates[i][1],lng:  scope.cropFarm.location.coordinates[i][0]});
                    }
                    drawMapFarm.push({lat: scope.cropFarm.location.coordinates[0][1],lng:  scope.cropFarm.location.coordinates[0][0]});
                    for(var j = 0; j<scope.parcel.location.coordinates.length; j++){
                        drawMapParcel.push({lat: scope.parcel.location.coordinates[j][1],lng: scope.parcel.location.coordinates[j][0]});
                    }
                    $log.log(drawMapParcel);
                }
                map = new google.maps.Map(angular.element('#mapa')[0], {
                    center: {lat: scope.tempLatitude, lng: scope.tempLongitude},
                    zoom: 8
                });
                new google.maps.Polyline({
                    path: drawMapFarm,
                    geodesic: true,
                    strokeColor: '#FF0000',
                    strokeOpacity: 1.0,
                    strokeWeight: 2
                }).setMap(map);
                drawMapFarm = [];

                new google.maps.Polygon({
                    paths: drawMapParcel,
                    strokeColor: '#FF0000',
                    strokeOpacity: 0.8,
                    strokeWeight: 2,
                    fillColor: '#FF0000',
                    fillOpacity: 0.35
                }).setMap(map);
                drawMapParcel=[];


                var isClosed = false;
                var poly = new google.maps.Polyline({ map: map, path: [], strokeColor: "#FF0000", strokeOpacity: 1.0, strokeWeight: 2 });

                google.maps.event.addListener(map, 'click', first);
                function first (clickEvent) {
                    var markerIndex = poly.getPath().length;
                    var isFirstMarker = markerIndex === 0;
                    var marker = new google.maps.Marker({ map: map, position: clickEvent.latLng, draggable: true });
                    if (isFirstMarker) {
                        google.maps.event.addListener(marker, 'click', second);
                    }
                    poly.getPath().push(clickEvent.latLng);
                    if (isClosed){
                        return;
                    }
                }
                function second () {
                    var path = poly.getPath();
                    poly.setMap(null);
                    poly = new google.maps.Polygon({ map: map, path: path, strokeColor: "#FFF000", strokeOpacity: 0.8, strokeWeight: 2, fillColor: "#FF0000", fillOpacity: 0.35 });
                    isClosed = true;
                    if (isClosed){
                        for(var i = 0; i<path.getArray().length;i++){
                            polygon.coordinates[i] = [poly.getPath().getArray()[i].lng(),poly.getPath().getArray()[i].lat()];
                        }
                        scope.parcel.location = polygon;
                        return;
                    }
                }

            });

        };


        /*scope.mostrarMapaParcel = function (){
            direction();
            map = new google.maps.Map(angular.element('#mapa')[0], {
                center: {lat: scope.tempLatitude, lng: scope.tempLongitude},
                zoom: 8
            });
            dibujar();
        };

        function dibujar(){
            $log.log("Entro a dibujar");
            $log.log(drawMap);
            new google.maps.Polygon({
                paths: drawMap,
                strokeColor: '#FF0000',
                strokeOpacity: 0.8,
                strokeWeight: 2,
                fillOpacity: 0.35
            }).setMap(map);
            drawMap = [];
        }*/

        if(scope.parcel.totalArea == null){
            scope.parcel.totalArea = new Area();
        }

        if(scope.parcel.groundFeatures == null){
            scope.parcel.groundFeatures = new GroundFeatures();
        }

        //------------------------------------------------------------------------
        /*scope.labels = ['1','2','3','4'];
        scope.latitudes = new Array(scope.labels.size);
        scope.longitudes = new Array(scope.labels.size);

        scope.saveEverything = function() {
            for (var i = 0; i < scope.labels.length; i++) {
                polygon.coordinates[i]=[parseFloat(scope.longitudes[i]),parseFloat(scope.latitudes[i])];
            }
            scope.parcel.location = polygon;
        };*/

        //----------------------------------------------------------------------------
    };
    return {
        restrict: "E",
        link: linker,
        scope: {
            parcel: '=',
            isEdit: '=',
            parcelScope: '=',
            theForm: '=',
            onAssignToCustomer: '&',
            onMakePublic: '&',
            onUnassignFromCustomer: '&',
            onDeleteParcel: '&'
        }
    };
}
