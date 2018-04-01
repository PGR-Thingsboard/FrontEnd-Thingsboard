

import farmFieldsetTemplate from './farm-fieldset.tpl.html';
import mapboxgl from 'mapbox-gl';
import FarmDetails from "./FarmDetails";

/* eslint-enable import/no-unresolved, import/default */
/*@ngInject*/
export default function FarmDirective($compile, $templateCache, toast, $translate, types, farmService, customerService, $log) {
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

        scope.labels = ['1','2','3','4'];
        scope.latitudes = new Array(scope.labels.size);
        scope.longitudes = new Array(scope.labels.size);


        function Polygon() {
            this.coordinates = [];
            this.type = 'Polygon';
        }

        var polygon = new Polygon();

        scope.destination = ['Familiar','Production'];

        if(scope.farm.farmDetails === null){
            scope.farm.farmDetails = new FarmDetails();
        }

        scope.saveEverything = function() {
            for (var i = 0; i < scope.labels.length; i++) {
                polygon.coordinates[i]=[parseFloat(scope.longitudes[i]),parseFloat(scope.latitudes[i])]
            }
            scope.farm.location = polygon;
        };

        scope.ubicar = function () {
            mapboxgl.accessToken = 'pk.eyJ1IjoiZ2VhcnMyOSIsImEiOiJjamRqbGNnenAxb3p6MnFuOXM4eXc5YWh3In0.5iQInCtAntYLhJVC5OUU_w';


            var map = new mapboxgl.Map({
                container: 'map',
                style: 'mapbox://styles/mapbox/streets-v8',
                center: [40, -74.50],
                zoom: 9
            });

            map.on('load', function() {
                map.resize();
               $log.log(map);
            });
        };


    };
    return {
        restrict: "E",
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
