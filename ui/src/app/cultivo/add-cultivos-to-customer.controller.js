/*@ngInject*/
export default function AddCultivosToCustomerController(cultivoService, $mdDialog, $q, customerId, cultivos) {

    var vm = this;

    vm.cultivos = cultivos;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchCultivoTextUpdated = searchCultivoTextUpdated;
    vm.toggleCultivoSelection = toggleCultivoSelection;

    vm.theCultivos = {
        getItemAtIndex: function (index) {
            if (index > vm.cultivos.data.length) {
                vm.theCultivos.fetchMoreItems_(index);
                return null;
            }
            var item = vm.cultivos.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },

        getLength: function () {
            if (vm.cultivos.hasNext) {
                return vm.cultivos.data.length + vm.cultivos.nextPageLink.limit;
            } else {
                return vm.cultivos.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.cultivos.hasNext && !vm.cultivos.pending) {
                vm.cultivos.pending = true;
                cultivoService.getTenantCultivos(vm.cultivos.nextPageLink, false).then(
                    function success(cultivos) {
                        vm.cultivos.data = vm.cultivos.data.concat(cultivos.data);
                        vm.cultivos.nextPageLink = cultivos.nextPageLink;
                        vm.cultivos.hasNext = cultivos.hasNext;
                        if (vm.cultivos.hasNext) {
                            vm.cultivos.nextPageLink.limit = vm.cultivos.pageSize;
                        }
                        vm.cultivos.pending = false;
                    },
                    function fail() {
                        vm.cultivos.hasNext = false;
                        vm.cultivos.pending = false;
                    });
            }
        }
    };

    function cancel () {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var cultivoId in vm.cultivos.selections) {
            tasks.push(cultivoService.assignCultivoToCustomer(customerId, cultivoId));
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

    function noData() {
        return vm.cultivos.data.length == 0 && !vm.cultivos.hasNext;
    }

    function hasData() {
        return vm.cultivos.data.length > 0;
    }

    function toggleCultivoSelection($event, cultivo) {
        $event.stopPropagation();
        var selected = angular.isDefined(cultivo.selected) && cultivo.selected;
        cultivo.selected = !selected;
        if (cultivo.selected) {
            vm.cultivos.selections[cultivo.id.id] = true;
            vm.cultivos.selectedCount++;
        } else {
            delete vm.cultivos.selections[cultivo.id.id];
            vm.cultivos.selectedCount--;
        }
    }

    function searchCultivoTextUpdated() {
        vm.cultivos = {
            pageSize: vm.cultivos.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.cultivos.pageSize,
                textSearch: vm.searchText
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }

}