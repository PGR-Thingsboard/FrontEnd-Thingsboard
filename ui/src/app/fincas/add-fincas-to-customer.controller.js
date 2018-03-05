

/*@ngInject*/
export default function AddFincasToCustomerController(fincaService, $mdDialog, $q, customerId, fincas) {

    var vm = this;

    vm.fincas = fincas;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchFincaTextUpdated = searchFincaTextUpdated;
    vm.toggleFincaSelection = toggleFincaSelection;

    vm.theFincas = {
        getItemAtIndex: function (index) {
            if (index > vm.fincas.data.length) {
                vm.theFincas.fetchMoreItems_(index);
                return null;
            }
            var item = vm.fincas.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },

        getLength: function () {
            if (vm.fincas.hasNext) {
                return vm.fincas.data.length + vm.fincas.nextPageLink.limit;
            } else {
                return vm.fincas.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.fincas.hasNext && !vm.fincas.pending) {
                vm.fincas.pending = true;
                fincaService.getTenantFincas(vm.fincas.nextPageLink, false).then(
                    function success(fincas) {
                        vm.fincas.data = vm.fincas.data.concat(fincas.data);
                        vm.fincas.nextPageLink = fincas.nextPageLink;
                        vm.fincas.hasNext = fincas.hasNext;
                        if (vm.fincas.hasNext) {
                            vm.fincas.nextPageLink.limit = vm.fincas.pageSize;
                        }
                        vm.fincas.pending = false;
                    },
                    function fail() {
                        vm.fincas.hasNext = false;
                        vm.fincas.pending = false;
                    });
            }
        }
    };

    function cancel () {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var fincaId in vm.fincas.selections) {
            tasks.push(fincaService.assignFincaToCustomer(customerId, fincaId));
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

    function noData() {
        return vm.fincas.data.length == 0 && !vm.fincas.hasNext;
    }

    function hasData() {
        return vm.fincas.data.length > 0;
    }

    function toggleFincaSelection($event, finca) {
        $event.stopPropagation();
        var selected = angular.isDefined(finca.selected) && finca.selected;
        finca.selected = !selected;
        if (finca.selected) {
            vm.fincas.selections[finca.id.id] = true;
            vm.fincas.selectedCount++;
        } else {
            delete vm.fincas.selections[finca.id.id];
            vm.fincas.selectedCount--;
        }
    }

    function searchFincaTextUpdated() {
        vm.fincas = {
            pageSize: vm.fincas.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.fincas.pageSize,
                textSearch: vm.searchText
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }

}