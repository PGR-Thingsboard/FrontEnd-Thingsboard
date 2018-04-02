
import addParcelTemplate from './add-parcel.tpl.html';
import parcelCard from './parcel-card.tpl.html';
import assignToCustomerTemplate from './assign-to-customer.tpl.html';
import addParcelsToCustomerTemplate from './add-parcels-to-customer.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function ParcelCardController(types) {

    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        if (vm.item && vm.item.customerId && vm.parentCtl.parcelsScope === 'tenant' &&
            vm.item.customerId.id != vm.types.id.nullUid && !vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }

    vm.isPublic = function() {
        if (vm.item && vm.item.assignedCustomer && vm.parentCtl.parcelsScope === 'tenant' && vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }
}


/*@ngInject*/
export function ParcelController($rootScope, userService, parcelService, customerService, $state, $stateParams,
                                $document, $mdDialog, $q, $translate, types) {

    var customerId = $stateParams.customerId;

    var parcelActionsList = [];

    var parcelGroupActionsList = [];

    var vm = this;

    vm.types = types;

    vm.parcelGridConfig = {
        deleteItemTitleFunc: deleteParcelTitle,
        deleteItemContentFunc: deleteParcelText,
        deleteItemsTitleFunc: deleteParcelsTitle,
        deleteItemsActionTitleFunc: deleteParcelsActionTitle,
        deleteItemsContentFunc: deleteParcelsText,

        saveItemFunc: saveParcel,

        getItemTitleFunc: getParcelTitle,

        itemCardController: 'ParcelCardController',
        itemCardTemplateUrl: parcelCard,
        parentCtl: vm,

        actionsList: parcelActionsList,
        groupActionsList: parcelGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addParcelTemplate,

        addItemText: function() { return $translate.instant('parcel.add-parcel-text') },
        noItemsText: function() { return $translate.instant('parcel.no-parcels-text') },
        itemDetailsText: function() { return $translate.instant('parcel.parcel-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.parcelGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.parcelGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.parcelsScope = $state.$current.data.parcelsType;

    vm.assignToCustomer = assignToCustomer;
    vm.makePublic = makePublic;
    vm.unassignFromCustomer = unassignFromCustomer;

    initController();

    function initController() {
        var fetchParcelsFunction = null;
        var deleteParcelFunction = null;
        var refreshParcelsParamsFunction = null;

        var user = userService.getCurrentUser();

        if (user.authority === 'CUSTOMER_USER') {
            vm.parcelsScope = 'customer_user';
            customerId = user.customerId;
        }
        if (customerId) {
            vm.customerParcelsTitle = $translate.instant('customer.parcels');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerParcelsTitle = $translate.instant('customer.public-parcels');
                    }
                }
            );
        }

        if (vm.parcelsScope === 'tenant') {
            fetchParcelsFunction = function (pageLink, parcelType) {
                return parcelService.getTenantParcels(pageLink, true, null, parcelType);
            };
            deleteParcelFunction = function (parcelId) {
                return parcelService.deleteParcel(parcelId);
            };
            refreshParcelsParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            parcelActionsList.push({
                onAction: function ($event, item) {
                    makePublic($event, item);
                },
                name: function() { return $translate.instant('action.share') },
                details: function() { return $translate.instant('parcel.make-public') },
                icon: "share",
                isEnabled: function(parcel) {
                    return parcel && (!parcel.customerId || parcel.customerId.id === types.id.nullUid);
                }
            });

            parcelActionsList.push(
                {
                    onAction: function ($event, item) {
                        assignToCustomer($event, [ item.id.id ]);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('parcel.assign-to-customer') },
                    icon: "assignment_ind",
                    isEnabled: function(parcel) {
                        return parcel && (!parcel.customerId || parcel.customerId.id === types.id.nullUid);
                    }
                }
            );

            parcelActionsList.push(
                {
                    onAction: function ($event, item) {
                        unassignFromCustomer($event, item, false);
                    },
                    name: function() { return $translate.instant('action.unassign') },
                    details: function() { return $translate.instant('parcel.unassign-from-customer') },
                    icon: "assignment_return",
                    isEnabled: function(parcel) {
                        return parcel && parcel.customerId && parcel.customerId.id !== types.id.nullUid && !parcel.assignedCustomer.isPublic;
                    }
                }
            );

            parcelActionsList.push({
                onAction: function ($event, item) {
                    unassignFromCustomer($event, item, true);
                },
                name: function() { return $translate.instant('action.make-private') },
                details: function() { return $translate.instant('parcel.make-private') },
                icon: "reply",
                isEnabled: function(parcel) {
                    return parcel && parcel.customerId && parcel.customerId.id !== types.id.nullUid && parcel.assignedCustomer.isPublic;
                }
            });

            parcelActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('parcel.delete') },
                    icon: "delete"
                }
            );

            parcelGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignParcelsToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('parcel.assign-parcels') },
                    details: function(selectedCount) {
                        return $translate.instant('parcel.assign-parcels-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            parcelGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('parcel.delete-parcels') },
                    details: deleteParcelsActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.parcelsScope === 'customer' || vm.parcelsScope === 'customer_user') {
            fetchParcelsFunction = function (pageLink, parcelType) {
                return parcelService.getCustomerParcels(customerId, pageLink, true, null, parcelType);
            };
            deleteParcelFunction = function (parcelId) {
                return parcelService.unassignParcelFromCustomer(parcelId);
            };
            refreshParcelsParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.parcelsScope === 'customer') {
                parcelActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, false);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('parcel.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(parcel) {
                            return parcel && !parcel.assignedCustomer.isPublic;
                        }
                    }
                );
                parcelActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, true);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('parcel.make-private') },
                        icon: "reply",
                        isEnabled: function(parcel) {
                            return parcel && parcel.assignedCustomer.isPublic;
                        }
                    }
                );

                parcelGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignParcelsFromCustomer($event, items);
                        },
                        name: function() { return $translate.instant('parcel.unassign-parcels') },
                        details: function(selectedCount) {
                            return $translate.instant('parcel.unassign-parcels-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.parcelGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addParcelsToCustomer($event);
                    },
                    name: function() { return $translate.instant('parcel.assign-parcels') },
                    details: function() { return $translate.instant('parcel.assign-new-parcel') },
                    icon: "add"
                };


            } else if (vm.parcelsScope === 'customer_user') {
                vm.parcelGridConfig.addItemAction = {};
            }
        }

        vm.parcelGridConfig.refreshParamsFunc = refreshParcelsParamsFunction;
        vm.parcelGridConfig.fetchItemsFunc = fetchParcelsFunction;
        vm.parcelGridConfig.deleteItemFunc = deleteParcelFunction;

    }

    function deleteParcelTitle(parcel) {
        return $translate.instant('parcel.delete-parcel-title', {parcelName: parcel.name});
    }

    function deleteParcelText() {
        return $translate.instant('parcel.delete-parcel-text');
    }

    function deleteParcelsTitle(selectedCount) {
        return $translate.instant('parcel.delete-parcels-title', {count: selectedCount}, 'messageformat');
    }

    function deleteParcelsActionTitle(selectedCount) {
        return $translate.instant('parcel.delete-parcels-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteParcelsText () {
        return $translate.instant('parcel.delete-parcels-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getParcelTitle(parcel) {
        return parcel ? parcel.name : '';
    }

    function saveParcel(parcel) {
        var deferred = $q.defer();
        parcelService.saveParcel(parcel).then(
            function success(savedParcel) {
                $rootScope.$broadcast('parcelSaved');
                var parcels = [ savedParcel ];
                customerService.applyAssignedCustomersInfo(parcels).then(
                    function success(items) {
                        if (items && items.length == 1) {
                            deferred.resolve(items[0]);
                        } else {
                            deferred.reject();
                        }
                    },
                    function fail() {
                        deferred.reject();
                    }
                );
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function isCustomerUser() {
        return vm.parcelsScope === 'customer_user';
    }

    function assignToCustomer($event, parcelIds) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        customerService.getCustomers({limit: pageSize, textSearch: ''}).then(
            function success(_customers) {
                var customers = {
                    pageSize: pageSize,
                    data: _customers.data,
                    nextPageLink: _customers.nextPageLink,
                    selection: null,
                    hasNext: _customers.hasNext,
                    pending: false
                };
                if (customers.hasNext) {
                    customers.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AssignParcelToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: assignToCustomerTemplate,
                    locals: {parcelIds: parcelIds, customers: customers},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                }, function () {
                });
            },
            function fail() {
            });
    }

    function addParcelsToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        parcelService.getTenantParcels({limit: pageSize, textSearch: ''}, false).then(
            function success(_parcels) {
                var parcels = {
                    pageSize: pageSize,
                    data: _parcels.data,
                    nextPageLink: _parcels.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _parcels.hasNext,
                    pending: false
                };
                if (parcels.hasNext) {
                    parcels.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddParcelsToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addParcelsToCustomerTemplate,
                    locals: {customerId: customerId, parcels: parcels},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                }, function () {
                });
            },
            function fail() {
            });
    }

    function assignParcelsToCustomer($event, items) {
        var parcelIds = [];
        for (var id in items.selections) {
            parcelIds.push(id);
        }
        assignToCustomer($event, parcelIds);
    }

    function unassignFromCustomer($event, parcel, isPublic) {
        if ($event) {
            $event.stopPropagation();
        }
        var title;
        var content;
        var label;
        if (isPublic) {
            title = $translate.instant('parcel.make-private-parcel-title', {parcelName: parcel.name});
            content = $translate.instant('parcel.make-private-parcel-text');
            label = $translate.instant('parcel.make-private');
        } else {
            title = $translate.instant('parcel.unassign-parcel-title', {parcelName: parcel.name});
            content = $translate.instant('parcel.unassign-parcel-text');
            label = $translate.instant('parcel.unassign-parcel');
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            parcelService.unassignParcelFromCustomer(parcel.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function unassignParcelsFromCustomer($event, items) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('parcel.unassign-parcels-title', {count: items.selectedCount}, 'messageformat'))
            .htmlContent($translate.instant('parcel.unassign-parcels-text'))
            .ariaLabel($translate.instant('parcel.unassign-parcel'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            var tasks = [];
            for (var id in items.selections) {
                tasks.push(parcelService.unassignParcelFromCustomer(id));
            }
            $q.all(tasks).then(function () {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, parcel) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('parcel.make-public-parcel-title', {parcelName: parcel.name}))
            .htmlContent($translate.instant('parcel.make-public-parcel-text'))
            .ariaLabel($translate.instant('parcel.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            parcelService.makeParcelPublic(parcel.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }
}
