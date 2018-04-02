/*@ngInject*/
export default function AddParcelsToCustomerController(parcelService, $mdDialog, $q, customerId, parcels) {

    var vm = this;

    vm.parcels = parcels;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchParcelTextUpdated = searchParcelTextUpdated;
    vm.toggleParcelSelection = toggleParcelSelection;

    vm.theParcels = {
        getItemAtIndex: function (index) {
            if (index > vm.parcels.data.length) {
                vm.theParcels.fetchMoreItems_(index);
                return null;
            }
            var item = vm.parcels.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },

        getLength: function () {
            if (vm.parcels.hasNext) {
                return vm.parcels.data.length + vm.parcels.nextPageLink.limit;
            } else {
                return vm.parcels.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.parcels.hasNext && !vm.parcels.pending) {
                vm.parcels.pending = true;
                parcelService.getTenantParcels(vm.parcels.nextPageLink, false).then(
                    function success(parcels) {
                        vm.parcels.data = vm.parcels.data.concat(parcels.data);
                        vm.parcels.nextPageLink = parcels.nextPageLink;
                        vm.parcels.hasNext = parcels.hasNext;
                        if (vm.parcels.hasNext) {
                            vm.parcels.nextPageLink.limit = vm.parcels.pageSize;
                        }
                        vm.parcels.pending = false;
                    },
                    function fail() {
                        vm.parcels.hasNext = false;
                        vm.parcels.pending = false;
                    });
            }
        }
    };

    function cancel () {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var parcelId in vm.parcels.selections) {
            tasks.push(parcelService.assignParcelToCustomer(customerId, parcelId));
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

    function noData() {
        return vm.parcels.data.length == 0 && !vm.parcels.hasNext;
    }

    function hasData() {
        return vm.parcels.data.length > 0;
    }

    function toggleParcelSelection($event, parcel) {
        $event.stopPropagation();
        var selected = angular.isDefined(parcel.selected) && parcel.selected;
        parcel.selected = !selected;
        if (parcel.selected) {
            vm.parcels.selections[parcel.id.id] = true;
            vm.parcels.selectedCount++;
        } else {
            delete vm.parcels.selections[parcel.id.id];
            vm.parcels.selectedCount--;
        }
    }

    function searchParcelTextUpdated() {
        vm.parcels = {
            pageSize: vm.parcels.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.parcels.pageSize,
                textSearch: vm.searchText
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }

}