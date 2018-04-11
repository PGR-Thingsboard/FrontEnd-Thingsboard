import farmFieldsetTemplate from './farm-fieldset.tpl.html';


/* eslint-enable import/no-unresolved, import/default */
/*@ngInject*/
export default function FarmDirective($compile, $templateCache, toast, $translate, types, farmService, customerService,$log) {
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

        scope.longitude = '';
        scope.latitude = '';
        scope.temperature = '';
        scope.humidity = '';
        scope.pressure = '';
        scope.windSpeed = '';
        scope.climatology = function(){
            farmService.getFarmClimatology(scope.farm.name,scope.longitude,scope.latitude).then(function(result){
                $log.log(result);
                scope.temperature = result.main.temp;
                scope.humidity = result.main.humidity;
                scope.pressure = result.main.pressure;
                scope.windSpeed = result.wind.speed;
            });
        };


        $compile(element.contents())(scope);

        scope.labels = ['1','2','3','4'];
        scope.latitudes = new Array(scope.labels.size);
        scope.longitudes = new Array(scope.labels.size);

//------------------------------------------------Class--------------------------------------
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

        function WaterPoint() {
            this.numberPoint = 0;
            this.resolution = '';
            this.validity = new Date();
        }

        function IrrigationSystem(){
            this.name = '';
            this.description = '';
        }

//---------------------------------------------------------------------------------------------

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
        scope.destination = ['Familiar','Production'];
        scope.symbol = ['ha','fg'];

        if(scope.farm.farmDetails == null){
            scope.farm.farmDetails = new FarmDetails();
        }

        if(scope.farm.totalArea == null){
            scope.farm.totalArea = new Area();
        }

        if(scope.farm.irrigationsSystems == null){
            scope.farm.irrigationsSystems = [];
        }


        scope.saveEverything = function() {
            for (var i = 0; i < scope.labels.length; i++) {
                polygon.coordinates[i]=[parseFloat(scope.longitudes[i]),parseFloat(scope.latitudes[i])]
            }
            scope.farm.location = polygon;
        };





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