


import cultivoFieldsetTemplate from './cultivo-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function CultivoDirective($compile, $templateCache, toast, $translate, types, cultivoService, fincaService, customerService, $log) {
    var linker = function (scope, element) {
        var template = $templateCache.get(cultivoFieldsetTemplate);
        element.html(template);

        scope.types = types;
        scope.isAssignedToCustomer = false;
        scope.isPublic = false;
        scope.assignedCustomer = null;
        fincaService.getAllFincas().then(function(result){
            $log.log(result[0].name);
            scope.fincas=result
        });

        scope.$watch('cultivo', function(newVal) {
            if (newVal) {
                if (scope.cultivo.customerId && scope.cultivo.customerId.id !== types.id.nullUid) {
                    scope.isAssignedToCustomer = true;
                    customerService.getShortCustomerInfo(scope.cultivo.customerId.id).then(
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

        scope.onCultivoIdCopied = function() {
            toast.showSuccess($translate.instant('cultivo.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };


        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            cultivo: '=',
            isEdit: '=',
            cultivoScope: '=',
            theForm: '=',
            onAssignToCustomer: '&',
            onMakePublic: '&',
            onUnassignFromCustomer: '&',
            onDeleteCultivo: '&'
        }
    };
}
