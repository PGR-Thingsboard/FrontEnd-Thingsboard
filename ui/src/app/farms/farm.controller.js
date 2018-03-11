
import addFarmTemplate from './add-farm.tpl.html';
import farmCard from './farm-card.tpl.html';
import assignToCustomerTemplate from './assign-to-customer.tpl.html';
import addFarmsToCustomerTemplate from './add-farms-to-customer.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function FarmCardController(types) {

    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        if (vm.item && vm.item.customerId && vm.parentCtl.farmsScope === 'tenant' &&
            vm.item.customerId.id != vm.types.id.nullUid && !vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }

    vm.isPublic = function() {
        if (vm.item && vm.item.assignedCustomer && vm.parentCtl.farmsScope === 'tenant' && vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }
}


/*@ngInject*/
export function FarmController($rootScope, userService, farmService, customerService, $state, $stateParams,
                                $document, $mdDialog, $q, $translate, types) {

    var customerId = $stateParams.customerId;

    var farmActionsList = [];

    var farmGroupActionsList = [];

    var vm = this;

    vm.types = types;

    vm.farmGridConfig = {
        deleteItemTitleFunc: deleteFarmTitle,
        deleteItemContentFunc: deleteFarmText,
        deleteItemsTitleFunc: deleteFarmsTitle,
        deleteItemsActionTitleFunc: deleteFarmsActionTitle,
        deleteItemsContentFunc: deleteFarmsText,

        saveItemFunc: saveFarm,

        getItemTitleFunc: getFarmTitle,

        itemCardController: 'FarmCardController',
        itemCardTemplateUrl: farmCard,
        parentCtl: vm,

        actionsList: farmActionsList,
        groupActionsList: farmGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addFarmTemplate,

        addItemText: function() { return $translate.instant('farm.add-farm-text') },
        noItemsText: function() { return $translate.instant('farm.no-farms-text') },
        itemDetailsText: function() { return $translate.instant('farm.farm-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.farmGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.farmGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.farmsScope = $state.$current.data.farmsType;

    vm.assignToCustomer = assignToCustomer;
    vm.makePublic = makePublic;
    vm.unassignFromCustomer = unassignFromCustomer;

    initController();

    function initController() {
        var fetchFarmsFunction = null;
        var deleteFarmFunction = null;
        var refreshFarmsParamsFunction = null;

        var user = userService.getCurrentUser();

        if (user.authority === 'CUSTOMER_USER') {
            vm.farmsScope = 'customer_user';
            customerId = user.customerId;
        }
        if (customerId) {
            vm.customerFarmsTitle = $translate.instant('customer.farms');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerFarmsTitle = $translate.instant('customer.public-farms');
                    }
                }
            );
        }

        if (vm.farmsScope === 'tenant') {
            fetchFarmsFunction = function (pageLink, farmType) {
                return farmService.getTenantFarms(pageLink, true, null, farmType);
            };
            deleteFarmFunction = function (farmId) {
                return farmService.deleteFarm(farmId);
            };
            refreshFarmsParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            farmActionsList.push({
                onAction: function ($event, item) {
                    makePublic($event, item);
                },
                name: function() { return $translate.instant('action.share') },
                details: function() { return $translate.instant('farm.make-public') },
                icon: "share",
                isEnabled: function(farm) {
                    return farm && (!farm.customerId || farm.customerId.id === types.id.nullUid);
                }
            });

            farmActionsList.push(
                {
                    onAction: function ($event, item) {
                        assignToCustomer($event, [ item.id.id ]);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('farm.assign-to-customer') },
                    icon: "assignment_ind",
                    isEnabled: function(farm) {
                        return farm && (!farm.customerId || farm.customerId.id === types.id.nullUid);
                    }
                }
            );

            farmActionsList.push(
                {
                    onAction: function ($event, item) {
                        unassignFromCustomer($event, item, false);
                    },
                    name: function() { return $translate.instant('action.unassign') },
                    details: function() { return $translate.instant('farm.unassign-from-customer') },
                    icon: "assignment_return",
                    isEnabled: function(farm) {
                        return farm && farm.customerId && farm.customerId.id !== types.id.nullUid && !farm.assignedCustomer.isPublic;
                    }
                }
            );

            farmActionsList.push({
                onAction: function ($event, item) {
                    unassignFromCustomer($event, item, true);
                },
                name: function() { return $translate.instant('action.make-private') },
                details: function() { return $translate.instant('farm.make-private') },
                icon: "reply",
                isEnabled: function(farm) {
                    return farm && farm.customerId && farm.customerId.id !== types.id.nullUid && farm.assignedCustomer.isPublic;
                }
            });

            farmActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('farm.delete') },
                    icon: "delete"
                }
            );

            farmGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignFarmsToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('farm.assign-farms') },
                    details: function(selectedCount) {
                        return $translate.instant('farm.assign-farms-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            farmGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('farm.delete-farms') },
                    details: deleteFarmsActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.farmsScope === 'customer' || vm.farmsScope === 'customer_user') {
            fetchFarmsFunction = function (pageLink, farmType) {
                return farmService.getCustomerFarms(customerId, pageLink, true, null, farmType);
            };
            deleteFarmFunction = function (farmId) {
                return farmService.unassignFarmFromCustomer(farmId);
            };
            refreshFarmsParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.farmsScope === 'customer') {
                farmActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, false);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('farm.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(farm) {
                            return farm && !farm.assignedCustomer.isPublic;
                        }
                    }
                );
                farmActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, true);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('farm.make-private') },
                        icon: "reply",
                        isEnabled: function(farm) {
                            return farm && farm.assignedCustomer.isPublic;
                        }
                    }
                );

                farmGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignFarmsFromCustomer($event, items);
                        },
                        name: function() { return $translate.instant('farm.unassign-farms') },
                        details: function(selectedCount) {
                            return $translate.instant('farm.unassign-farms-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.farmGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addFarmsToCustomer($event);
                    },
                    name: function() { return $translate.instant('farm.assign-farms') },
                    details: function() { return $translate.instant('farm.assign-new-farm') },
                    icon: "add"
                };


            } else if (vm.farmsScope === 'customer_user') {
                vm.farmGridConfig.addItemAction = {};
            }
        }

        vm.farmGridConfig.refreshParamsFunc = refreshFarmsParamsFunction;
        vm.farmGridConfig.fetchItemsFunc = fetchFarmsFunction;
        vm.farmGridConfig.deleteItemFunc = deleteFarmFunction;

    }

    function deleteFarmTitle(farm) {
        return $translate.instant('farm.delete-farm-title', {farmName: farm.name});
    }

    function deleteFarmText() {
        return $translate.instant('farm.delete-farm-text');
    }

    function deleteFarmsTitle(selectedCount) {
        return $translate.instant('farm.delete-farms-title', {count: selectedCount}, 'messageformat');
    }

    function deleteFarmsActionTitle(selectedCount) {
        return $translate.instant('farm.delete-farms-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteFarmsText () {
        return $translate.instant('farm.delete-farms-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getFarmTitle(farm) {
        return farm ? farm.name : '';
    }

    function saveFarm(farm) {
        var deferred = $q.defer();
        farmService.saveFarm(farm).then(
            function success(savedFarm) {
                $rootScope.$broadcast('farmSaved');
                var farms = [ savedFarm ];
                customerService.applyAssignedCustomersInfo(farms).then(
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
        return vm.farmsScope === 'customer_user';
    }


    function assignToCustomer($event, farmIds) {
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
                    controller: 'AssignFarmToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: assignToCustomerTemplate,
                    locals: {farmIds: farmIds, customers: customers},
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

    function addFarmsToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        farmService.getTenantFarms({limit: pageSize, textSearch: ''}, false).then(
            function success(_farms) {
                var farms = {
                    pageSize: pageSize,
                    data: _farms.data,
                    nextPageLink: _farms.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _farms.hasNext,
                    pending: false
                };
                if (farms.hasNext) {
                    farms.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddFarmsToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addFarmsToCustomerTemplate,
                    locals: {customerId: customerId, farms: farms},
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

    function assignFarmsToCustomer($event, items) {
        var farmIds = [];
        for (var id in items.selections) {
            farmIds.push(id);
        }
        assignToCustomer($event, farmIds);
    }

    function unassignFromCustomer($event, farm, isPublic) {
        if ($event) {
            $event.stopPropagation();
        }
        var title;
        var content;
        var label;
        if (isPublic) {
            title = $translate.instant('farm.make-private-farm-title', {farmName: farm.name});
            content = $translate.instant('farm.make-private-farm-text');
            label = $translate.instant('farm.make-private');
        } else {
            title = $translate.instant('farm.unassign-farm-title', {farmName: farm.name});
            content = $translate.instant('farm.unassign-farm-text');
            label = $translate.instant('farm.unassign-farm');
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            farmService.unassignFarmFromCustomer(farm.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function unassignFarmsFromCustomer($event, items) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('farm.unassign-farms-title', {count: items.selectedCount}, 'messageformat'))
            .htmlContent($translate.instant('farm.unassign-farms-text'))
            .ariaLabel($translate.instant('farm.unassign-farm'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            var tasks = [];
            for (var id in items.selections) {
                tasks.push(farmService.unassignFarmFromCustomer(id));
            }
            $q.all(tasks).then(function () {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, farm) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('farm.make-public-farm-title', {farmName: farm.name}))
            .htmlContent($translate.instant('farm.make-public-farm-text'))
            .ariaLabel($translate.instant('farm.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            farmService.makeFarmPublic(farm.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }
}
