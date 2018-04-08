

import farmFieldsetTemplate from './farm-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */
/*@ngInject*/
export default function FarmDirective($compile, $templateCache, toast, $translate, types, farmService, customerService) {
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

//------------------------------------------------Class--------------------------------------
        function Polygon() {
            this.coordinates = [];
            this.type = 'Polygon';
        }


        function Area(){
            this.extension=0.0;
            this.symbol='';

        }

        function PublicServices(){
            this.electricity=false;
            this.water=false;
            this.sewerage=false;
            this.gas=false;
            this.garbage_collection=false;
            this.collection=false;
            this.internet=false;
            this.telephony=false;
            this.television=false;
        }

        function FarmDetails(){
            this.destination='';
            this.useDetails='';
            this.publicServices=new PublicServices();
            this.productionTransport='';
            this.waterPoints=[];
        }

        function Climatology() {
            this.temperature='';
            this.humidity='';
            this.rainFall='';
            this.solarIrradiance='';
        }

        function Access(){
            this.air=false;
            this.land=false;
            this.fluvial=false;
        }


        function Enviroment(){
            this.climatology= new Climatology();
            this.orography='';
            this.municipalDistance=0.0;
            this.access=new Access();
            this.highwayState='';
        }

       /* function Person() {
            this.name ="";
            this.birthday = birthday;
            this.birthplace="";
            this.maritalStatus="";
            this.ethnicGroup="";
            this.relation = "";
        }
*/
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


        /*function WaterPoints() {
            this.numberPoint = 0;
            this.resolution = '';
            this.validity = null;
        }*/
//---------------------------------------------------------------------------------------------

        var polygon = new Polygon();
        scope.destination = ['Familiar','Production'];
        scope.symbol = ['ha','fg'];
        scope.orography = ['Valley','Mountain',"Plain","Volcano"];
        scope.ways=["air","land","fluvial"];
        scope.publicServices=["electricity","water","sewerage","gas","garbage_collection","internet","telephony","television"];
        scope.hmaterial=["Block, Brick, Stone, Polished wood","Concrete drained","Prefabricated material","Tapia tread, Bahareque, Adobe", "Crude wood, Board, Plank","Reed (Plant), Mat, Other vegetables"]
        scope.fmaterial=["Marble, Parquetry, Polished or lacquered wood","Tile, Vinyl, Tablet, Brick, Laminate","Carpet","Cement, Gravel","Crude wood, Board, Plank, Other vegetable","Soil, Sand, mud"]
        scope.kitchen=["In a room used only for cooking","In a room also used for sleeping","In a living room with dishwasher","In a living room without dishwasher","In a patio, corridor, trellis or outdoors","They do not prepare food in the house"]
        scope.bathroom=["Toilet connected to the sewer","Toilet connected to septic tank","Toilet without connection","Latrine","Toilet with direct discharge to water sources (low tide)","Does not have sanitary service"]


        if(scope.farm.farmDetails === null){
            scope.farm.farmDetails = new FarmDetails();
        }


        if(scope.farm.totalArea === null){
            scope.farm.totalArea = new Area();
        }

        if (scope.farm.enviroment===null){
            scope.farm.enviroment= new Enviroment();
        }

        if (scope.farm.homeDetails===null){
            scope.farm.homeDetails= new HomeDetails();
        }
        scope.saveEverything = function() {
            for (var i = 0; i < scope.labels.length; i++) {
                polygon.coordinates[i]=[parseFloat(scope.longitudes[i]),parseFloat(scope.latitudes[i])]
            }
            scope.farm.location = polygon;
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
