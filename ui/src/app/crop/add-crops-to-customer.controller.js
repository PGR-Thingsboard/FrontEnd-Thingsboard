/*@ngInject*/
export default function AddCropsToCustomerController(cropService, $mdDialog, $q, customerId, crops) {

    var vm = this;

    vm.crops = crops;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchCropTextUpdated = searchCropTextUpdated;
    vm.toggleCropSelection = toggleCropSelection;

    vm.theCrops = {
        getItemAtIndex: function (index) {
            if (index > vm.crops.data.length) {
                vm.theCrops.fetchMoreItems_(index);
                return null;
            }
            var item = vm.crops.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },

        getLength: function () {
            if (vm.crops.hasNext) {
                return vm.crops.data.length + vm.crops.nextPageLink.limit;
            } else {
                return vm.crops.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.crops.hasNext && !vm.crops.pending) {
                vm.crops.pending = true;
                cropService.getTenantCrops(vm.crops.nextPageLink, false).then(
                    function success(crops) {
                        vm.crops.data = vm.crops.data.concat(crops.data);
                        vm.crops.nextPageLink = crops.nextPageLink;
                        vm.crops.hasNext = crops.hasNext;
                        if (vm.crops.hasNext) {
                            vm.crops.nextPageLink.limit = vm.crops.pageSize;
                        }
                        vm.crops.pending = false;
                    },
                    function fail() {
                        vm.crops.hasNext = false;
                        vm.crops.pending = false;
                    });
            }
        }
    };

    function cancel () {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var cropId in vm.crops.selections) {
            tasks.push(cropService.assignCropToCustomer(customerId, cropId));
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

    function noData() {
        return vm.crops.data.length == 0 && !vm.crops.hasNext;
    }

    function hasData() {
        return vm.crops.data.length > 0;
    }

    function toggleCropSelection($event, crop) {
        $event.stopPropagation();
        var selected = angular.isDefined(crop.selected) && crop.selected;
        crop.selected = !selected;
        if (crop.selected) {
            vm.crops.selections[crop.id.id] = true;
            vm.crops.selectedCount++;
        } else {
            delete vm.crops.selections[crop.id.id];
            vm.crops.selectedCount--;
        }
    }

    function searchCropTextUpdated() {
        vm.crops = {
            pageSize: vm.crops.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.crops.pageSize,
                textSearch: vm.searchText
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }

}