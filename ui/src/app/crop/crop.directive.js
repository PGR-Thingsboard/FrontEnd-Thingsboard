


import cropFieldsetTemplate from './crop-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function CropDirective($compile, $templateCache, toast, $translate, types, cropService, farmService, customerService, $log) {
    var linker = function (scope, element) {
        var template = $templateCache.get(cropFieldsetTemplate);
        element.html(template);

        scope.types = types;
        scope.isAssignedToCustomer = false;
        scope.isPublic = false;
        scope.assignedCustomer = null;
        farmService.getAllFarms().then(function(result){
            $log.log(result[0]);
            scope.farms=result
        });

        scope.$watch('crop', function(newVal) {
            if (newVal) {
                if (scope.crop.customerId && scope.crop.customerId.id !== types.id.nullUid) {
                    scope.isAssignedToCustomer = true;
                    customerService.getShortCustomerInfo(scope.crop.customerId.id).then(
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

        scope.onCropIdCopied = function() {
            toast.showSuccess($translate.instant('crop.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
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

        scope.saveEverything = function() {
            for (var i = 0; i < scope.labels.length; i++) {
                polygon.coordinates[i]=[parseFloat(scope.longitudes[i]),parseFloat(scope.latitudes[i])]
            }
            $log.log(polygon);
            scope.crop.location = polygon;
        };
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            crop: '=',
            isEdit: '=',
            cropScope: '=',
            theForm: '=',
            onAssignToCustomer: '&',
            onMakePublic: '&',
            onUnassignFromCustomer: '&',
            onDeleteCrop: '&'
        }
    };
}
