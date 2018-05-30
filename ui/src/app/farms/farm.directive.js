import farmFieldsetTemplate from './farm-fieldset.tpl.html';



/* eslint-enable import/no-unresolved, import/default */
/* global google */
/*@ngInject*/
export default function FarmDirective($compile, $templateCache, toast, $translate, types, farmService, customerService,$log,$window) {
    var linker = function (scope, element) {
        var template = $templateCache.get(farmFieldsetTemplate);
        element.html(template);

        scope.types = types;
        scope.isAssignedToCustomer = false;
        scope.isPublic = false;
        scope.assignedCustomer = null;

        scope.$watch('farm', function(newVal) {
            if (newVal) {
                if (scope.farm.customerId && scope.farm.customerId.id !== types.id.nullUid) {
                    scope.isAssignedToCustomer = true;
                    customerService.getShortCustomerInfo(scope.farm.customerId.id).then(
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

        scope.onFarmIdCopied = function() {
            toast.showSuccess($translate.instant('farm.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };


        $compile(element.contents())(scope);

        function Polygon() {
            this.coordinates = [];
            this.type = 'Polygon';
        }


        function Area(){
            this.extension=0.0;
            this.symbol='';

        }

        function FarmDetails(){
            this.destination='';
            this.useDetails='';
            this.publicServices=[];
            this.productionTransport='';
            this.waterPoints=[];
        }

        function Climatology() {
            this.temperature='';
            this.humidity='';
            this.rainFall='';
            this.solarIrradiance='';
        }



        function Enviroment(){
            this.climatology= new Climatology();
            this.orography='';
            this.municipalDistance=0.0;
            this.access=[];
            this.highwayState='';
        }

        function Person() {
            this.name ="";
            this.birthday = new Date();
            this.birthplace="";
            this.maritalStatus="";
            this.ethnicGroup="";
            this.relation = "";
        }

        function HomeDetails(){
            this.homeMaterial="";
            this.floorMaterial="";
            this.rooms=0;
            this.bathroom="";
            this.kitchen="";
            this.dependingPeople=0;
            this.workers=0;
            this.people=[];
        }



        function WaterPoint() {
            this.numberPoint = 0;
            this.resolution = '';
            this.validity = new Date();
        }

        function IrrigationSystem(){
            this.name = '';
            this.description = '';
        }

        scope.tempWaterPointNumber = 0;
        scope.tempWaterPointResolution = '';

        scope.addWaterPoint = function(){
            var waterPoint = new WaterPoint();
            waterPoint.numberPoint = scope.tempWaterPointNumber;
            waterPoint.resolution = scope.tempWaterPointResolution;
            scope.farm.farmDetails.waterPoints.push(waterPoint);
            scope.tempWaterPointNumber = 0;
            scope.tempWaterPointResolution = '';
        }


        scope.exists = function (item, list) {
            return list.indexOf(item) > -1;
        };

        scope.toggle = function (item, list) {
            var idx = list.indexOf(item);
            if (idx > -1) {
                list.splice(idx, 1);
            }
            else {
                list.push(item);
            }
        };

        scope.tempName ="";
        scope.tempBirthday = new Date();
        scope.tempBirthplace="";
        scope.tempMaritalStatus="";
        scope.tempEthnicGroup="";
        scope.tempRelation = "";
        scope.addPerson = function(){
            $log.log("su mensaje")
            var person = new Person();
            person.name =scope.tempName;
            person.birthday=scope.tempBirthday;
            person.birthplace=scope.tempBirthplace;
            person.maritalStatus=scope.tempMaritalStatus;
            person.ethnicGroup=scope.tempEthnicGroup;
            person.relation = scope.tempRelation;
            scope.farm.homeDetails.people.push(person);
            scope.tempName ="";
            scope.tempBirthday = new Date();
            scope.tempBirthplace="";
            scope.tempMaritalStatus="";
            scope.tempEthnicGroup="";
            scope.tempRelation = "";

        }

        scope.tempNameIrrigation = '';
        scope.tempDescriptionIrrigation = '';

        scope.addIrrigationSystem = function(){
            var irrigationSystem = new IrrigationSystem();
            irrigationSystem.name = scope.tempNameIrrigation;
            irrigationSystem.description = scope.tempDescriptionIrrigation;
            scope.farm.irrigationsSystems.push(irrigationSystem);
            scope.tempNameIrrigation = '';
            scope.tempDescriptionIrrigation = '';
        }

        var polygon = new Polygon();
        scope.longitude = '';
        scope.latitude = '';
        scope.climatology = function(){
            farmService.getFarmClimatology(scope.farm.name,scope.longitude,scope.latitude).then(function(result){
                $log.log(result);
                scope.farm.enviroment.climatology.temperature = result.main.temp;
                scope.farm.enviroment.climatology.humidity = result.main.humidity;
                scope.farm.enviroment.climatology.rainFall = result.main.pressure;
                scope.farm.enviroment.climatology.solarIrradiance = result.wind.speed;
            });
        };

        scope.tempLatitude = -34.397;
        scope.tempLongitude = 150.644;
        var drawMap = [];
        function direction(){
            if(scope.farm.location.coordinates.length > 0){
                scope.tempLatitude = scope.farm.location.coordinates[0][1];
                scope.tempLongitude = scope.farm.location.coordinates[0][0];
                for(var i=0; i<scope.farm.location.coordinates.length; i++){
                    drawMap.push({lat: scope.farm.location.coordinates[i][1],lng:  scope.farm.location.coordinates[i][0]});
                }
            }
        }



        var map;

        scope.mostrar = function () {
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
                fillColor: '#FF0000',
                fillOpacity: 0.35
            }).setMap(map);
            drawMap = [];
        }



        scope.destination = ['Familiar','Production'];
        scope.symbol = ['ha','fg'];
        scope.orography = ['Valley','Mountain',"Plain","Volcano"];
        scope.ways=["air","land","fluvial"];
        scope.publicServices=["electricity","water","sewerage","gas","garbage collection","internet","telephony","television"];
        scope.hmaterial=["Block, Brick, Stone, Polished wood","Concrete drained","Prefabricated material","Tapia tread, Bahareque, Adobe", "Crude wood, Board, Plank","Reed (Plant), Mat, Other vegetables"]
        scope.fmaterial=["Marble, Parquetry, Polished or lacquered wood","Tile, Vinyl, Tablet, Brick, Laminate","Carpet","Cement, Gravel","Crude wood, Board, Plank, Other vegetable","Soil, Sand, mud"]
        scope.kitchen=["In a room used only for cooking","In a room also used for sleeping","In a living room with dishwasher","In a living room without dishwasher","In a patio, corridor, trellis or outdoors","They do not prepare food in the house"]
        scope.bathroom=["Toilet connected to the sewer","Toilet connected to septic tank","Toilet without connection","Latrine","Toilet with direct discharge to water sources (low tide)","Does not have sanitary service"]
        scope.maritalStatus=["Single","Married","Free Union","Widower"]
        scope.ethnicGroup=["Native","Romani","Afrodescendant","None"]
        scope.relation=["Spouse","Son/Daughter","Stepson/Stepdaughter","Son-in-law/Daughter-in-law","Father/Mother","Stepfather/Stepmother","Father in law/Mother in law","Brother/Sister","Stepbrother/Stepsister","Brother in law/Sister in law","Grandson/Granddaughter","Grandfather/Grandmother","Another relative","Not related"]
        scope.highwayState=["Paved","Not Paved","Passable","Volcano"];
        scope.productionTransport=["Own","Cooperative","Third"];


        if(scope.farm.farmDetails == null){
            scope.farm.farmDetails = new FarmDetails();
        }


        if(scope.farm.totalArea == null){
            scope.farm.totalArea = new Area();
        }

        if (scope.farm.enviroment==null){
            scope.farm.enviroment= new Enviroment();
        }

        if (scope.farm.homeDetails == null){
            scope.farm.homeDetails= new HomeDetails();
        }

        if(scope.farm.irrigationsSystems == null){
            scope.farm.irrigationsSystems = [];
        }

        scope.labels = [1,2,3];
        scope.latitudes = new Array(scope.labels.size);
        scope.longitudes = new Array(scope.labels.size);

        scope.addCoordinate = function(){
            scope.labels.push(scope.labels.length + 1);
        };

        scope.deleteCoordinate = function(){
            if(scope.labels.length > 3){
                scope.labels.splice(-1,1);
            }
        };


        scope.saveEverything = function() {
            for (var i = 0; i < scope.labels.length; i++) {
                polygon.coordinates[i]=[parseFloat(scope.longitudes[i]),parseFloat(scope.latitudes[i])];
            }
            if(calcPolygonArea(polygon.coordinates) > 0 && calcPolygonArea(polygon.coordinates) <= 0.0008063810777798608){
                scope.farm.location = polygon;
            }else{
                $window.alert("The area of ​​the farm exceeds the limit, please re-enter the coordinates");
            }
        };



        function calcPolygonArea(vertices) {
            var total = 0;

            for (var i = 0, l = vertices.length; i < l; i++) {
                var addX = vertices[i][0];
                var addY = vertices[i == vertices.length - 1 ? 0 : i + 1][1];
                var subX = vertices[i == vertices.length - 1 ? 0 : i + 1][0];
                var subY = vertices[i][1];

                total += (addX * addY * 0.5);
                total -= (subX * subY * 0.5);
            }

            return Math.abs(total);
        }




        /*var map;
        function initMap() {
            map = new google.maps.Map(angular.element('#mapa')[0], {
                center: {lat: -34.397, lng: 150.644},
                zoom: 8
            });
        }
        initMap();
        var isClosed = false;
        var poly = new google.maps.Polyline({ map: map, path: [], strokeColor: "#FF0000", strokeOpacity: 1.0, strokeWeight: 2 });
        google.maps.event.addListener(map, 'click', function (clickEvent) {
            if (isClosed)
                return;
            var markerIndex = poly.getPath().length;
            var isFirstMarker = markerIndex === 0;
            var marker = new google.maps.Marker({ map: map, position: clickEvent.latLng, draggable: true });
            if (isFirstMarker) {
                google.maps.event.addListener(marker, 'click', function () {
                    if (isClosed)
                        return;
                    var path = poly.getPath();
                    poly.setMap(null);
                    poly = new google.maps.Polygon({ map: map, path: path, strokeColor: "#FFF000", strokeOpacity: 0.8, strokeWeight: 2, fillColor: "#FF0000", fillOpacity: 0.35 });
                    isClosed = true;
                    for(var i = 0; i< poly.getPath().getArray().length; i++){
                        polygon.coordinates[i] = [poly.getPath().getArray()[i].lng(),poly.getPath().getArray()[i].lat()]
                    }

                    $log.log(polygon);
                });
            }
            poly.getPath().push(clickEvent.latLng);

        });*/




    };
    return {
        restrict: 'E',
        link: linker,
        scope: {
            farm: '=',
            isEdit: '=',
            farmScope: '=',
            theForm: '=',
            onAssignToCustomer: '&',
            onMakePublic: '&',
            onUnassignFromCustomer: '&',
            onDeleteFarm: '&'
        }
    };


}