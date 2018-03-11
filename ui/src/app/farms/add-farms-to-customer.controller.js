

/*@ngInject*/
export default function AddFarmsToCustomerController(farmService, $mdDialog, $q, customerId, farms) {

    var vm = this;

    vm.farms = farms;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchFarmTextUpdated = searchFarmTextUpdated;
    vm.toggleFarmSelection = toggleFarmSelection;

    vm.theFarms = {
        getItemAtIndex: function (index) {
            if (index > vm.farms.data.length) {
                vm.theFarms.fetchMoreItems_(index);
                return null;
            }
            var item = vm.farms.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },

        getLength: function () {
            if (vm.farms.hasNext) {
                return vm.farms.data.length + vm.farms.nextPageLink.limit;
            } else {
                return vm.farms.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.farms.hasNext && !vm.farms.pending) {
                vm.farms.pending = true;
                farmService.getTenantFarms(vm.farms.nextPageLink, false).then(
                    function success(farms) {
                        vm.farms.data = vm.farms.data.concat(farms.data);
                        vm.farms.nextPageLink = farms.nextPageLink;
                        vm.farms.hasNext = farms.hasNext;
                        if (vm.farms.hasNext) {
                            vm.farms.nextPageLink.limit = vm.farms.pageSize;
                        }
                        vm.farms.pending = false;
                    },
                    function fail() {
                        vm.farms.hasNext = false;
                        vm.farms.pending = false;
                    });
            }
        }
    };

    function cancel () {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var farmId in vm.farms.selections) {
            tasks.push(farmService.assignFarmToCustomer(customerId, farmId));
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

    function noData() {
        return vm.farms.data.length == 0 && !vm.farms.hasNext;
    }

    function hasData() {
        return vm.farms.data.length > 0;
    }

    function toggleFarmSelection($event, farm) {
        $event.stopPropagation();
        var selected = angular.isDefined(farm.selected) && farm.selected;
        farm.selected = !selected;
        if (farm.selected) {
            vm.farms.selections[farm.id.id] = true;
            vm.farms.selectedCount++;
        } else {
            delete vm.farms.selections[farm.id.id];
            vm.farms.selectedCount--;
        }
    }

    function searchFarmTextUpdated() {
        vm.farms = {
            pageSize: vm.farms.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.farms.pageSize,
                textSearch: vm.searchText
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }

}