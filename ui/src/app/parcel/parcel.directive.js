
import parcelFieldsetTemplate from './parcel-fieldset.tpl.html';
import Action from './action';


/* eslint-enable import/no-unresolved, import/default */
/*@ngInject*/
export default function ParcelDirective($compile, $templateCache, toast, $translate, types, parcelService, farmService, customerService, $log) {
    var linker = function (scope, element) {
        var template = $templateCache.get(parcelFieldsetTemplate);
        element.html(template);


        scope.types = types;
        scope.isAssignedToCustomer = false;
        scope.isPublic = false;
        scope.assignedCustomer = null;
        farmService.getAllFarms().then(function(result){
            $log.log(result[0]);
            scope.farms=result
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
            if(scope.parcel.crop === null) {
                scope.parcel.crop = new Crop();
                scope.parcel.cropsHistory = [];
            }else{
                crop = true;
            }

            return crop;
        };

        if(scope.parcel.totalArea === null){
            scope.parcel.totalArea = new Area();
        }

        //------------------------------------------------------------------------
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
            scope.parcel.location = polygon;
        };

        //----------------------------------------------------------------------------
    }
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
