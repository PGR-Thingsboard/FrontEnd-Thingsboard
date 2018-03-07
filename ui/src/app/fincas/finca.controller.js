
import addFincaTemplate from './add-finca.tpl.html';
import fincaCard from './finca-card.tpl.html';
import assignToCustomerTemplate from './assign-to-customer.tpl.html';
import addFincasToCustomerTemplate from './add-fincas-to-customer.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function FincaCardController(types) {

    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        if (vm.item && vm.item.customerId && vm.parentCtl.fincasScope === 'tenant' &&
            vm.item.customerId.id != vm.types.id.nullUid && !vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }

    vm.isPublic = function() {
        if (vm.item && vm.item.assignedCustomer && vm.parentCtl.fincasScope === 'tenant' && vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }
}


/*@ngInject*/
export function FincaController($rootScope, userService, fincaService, customerService, $state, $stateParams,
                                $document, $mdDialog, $q, $translate, types) {

    var customerId = $stateParams.customerId;

    var fincaActionsList = [];

    var fincaGroupActionsList = [];

    var vm = this;

    vm.types = types;

    vm.fincaGridConfig = {
        deleteItemTitleFunc: deleteFincaTitle,
        deleteItemContentFunc: deleteFincaText,
        deleteItemsTitleFunc: deleteFincasTitle,
        deleteItemsActionTitleFunc: deleteFincasActionTitle,
        deleteItemsContentFunc: deleteFincasText,

        saveItemFunc: saveFinca,

        getItemTitleFunc: getFincaTitle,

        itemCardController: 'FincaCardController',
        itemCardTemplateUrl: fincaCard,
        parentCtl: vm,

        actionsList: fincaActionsList,
        groupActionsList: fincaGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addFincaTemplate,

        addItemText: function() { return $translate.instant('finca.add-finca-text') },
        noItemsText: function() { return $translate.instant('finca.no-fincas-text') },
        itemDetailsText: function() { return $translate.instant('finca.finca-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.fincaGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.fincaGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.fincasScope = $state.$current.data.fincasType;

    vm.assignToCustomer = assignToCustomer;
    vm.makePublic = makePublic;
    vm.unassignFromCustomer = unassignFromCustomer;

    initController();

    function initController() {
        var fetchFincasFunction = null;
        var deleteFincaFunction = null;
        var refreshFincasParamsFunction = null;

        var user = userService.getCurrentUser();

        if (user.authority === 'CUSTOMER_USER') {
            vm.fincasScope = 'customer_user';
            customerId = user.customerId;
        }
        if (customerId) {
            vm.customerFincasTitle = $translate.instant('customer.fincas');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerFincasTitle = $translate.instant('customer.public-fincas');
                    }
                }
            );
        }

        if (vm.fincasScope === 'tenant') {
            fetchFincasFunction = function (pageLink, fincaType) {
                return fincaService.getTenantFincas(pageLink, true, null, fincaType);
            };
            deleteFincaFunction = function (fincaId) {
                return fincaService.deleteFinca(fincaId);
            };
            refreshFincasParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            fincaActionsList.push({
                onAction: function ($event, item) {
                    makePublic($event, item);
                },
                name: function() { return $translate.instant('action.share') },
                details: function() { return $translate.instant('finca.make-public') },
                icon: "share",
                isEnabled: function(finca) {
                    return finca && (!finca.customerId || finca.customerId.id === types.id.nullUid);
                }
            });

            fincaActionsList.push(
                {
                    onAction: function ($event, item) {
                        assignToCustomer($event, [ item.id.id ]);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('finca.assign-to-customer') },
                    icon: "assignment_ind",
                    isEnabled: function(finca) {
                        return finca && (!finca.customerId || finca.customerId.id === types.id.nullUid);
                    }
                }
            );

            fincaActionsList.push(
                {
                    onAction: function ($event, item) {
                        unassignFromCustomer($event, item, false);
                    },
                    name: function() { return $translate.instant('action.unassign') },
                    details: function() { return $translate.instant('finca.unassign-from-customer') },
                    icon: "assignment_return",
                    isEnabled: function(finca) {
                        return finca && finca.customerId && finca.customerId.id !== types.id.nullUid && !finca.assignedCustomer.isPublic;
                    }
                }
            );

            fincaActionsList.push({
                onAction: function ($event, item) {
                    unassignFromCustomer($event, item, true);
                },
                name: function() { return $translate.instant('action.make-private') },
                details: function() { return $translate.instant('finca.make-private') },
                icon: "reply",
                isEnabled: function(finca) {
                    return finca && finca.customerId && finca.customerId.id !== types.id.nullUid && finca.assignedCustomer.isPublic;
                }
            });

            fincaActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('finca.delete') },
                    icon: "delete"
                }
            );

            fincaGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignFincasToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('finca.assign-fincas') },
                    details: function(selectedCount) {
                        return $translate.instant('finca.assign-fincas-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            fincaGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('finca.delete-fincas') },
                    details: deleteFincasActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.fincasScope === 'customer' || vm.fincasScope === 'customer_user') {
            fetchFincasFunction = function (pageLink, fincaType) {
                return fincaService.getCustomerFincas(customerId, pageLink, true, null, fincaType);
            };
            deleteFincaFunction = function (fincaId) {
                return fincaService.unassignFincaFromCustomer(fincaId);
            };
            refreshFincasParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.fincasScope === 'customer') {
                fincaActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, false);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('finca.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(finca) {
                            return finca && !finca.assignedCustomer.isPublic;
                        }
                    }
                );
                fincaActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, true);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('finca.make-private') },
                        icon: "reply",
                        isEnabled: function(finca) {
                            return finca && finca.assignedCustomer.isPublic;
                        }
                    }
                );

                fincaGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignFincasFromCustomer($event, items);
                        },
                        name: function() { return $translate.instant('finca.unassign-fincas') },
                        details: function(selectedCount) {
                            return $translate.instant('finca.unassign-fincas-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.fincaGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addFincasToCustomer($event);
                    },
                    name: function() { return $translate.instant('finca.assign-fincas') },
                    details: function() { return $translate.instant('finca.assign-new-finca') },
                    icon: "add"
                };


            } else if (vm.fincasScope === 'customer_user') {
                vm.fincaGridConfig.addItemAction = {};
            }
        }

        vm.fincaGridConfig.refreshParamsFunc = refreshFincasParamsFunction;
        vm.fincaGridConfig.fetchItemsFunc = fetchFincasFunction;
        vm.fincaGridConfig.deleteItemFunc = deleteFincaFunction;

    }

    function deleteFincaTitle(finca) {
        return $translate.instant('finca.delete-finca-title', {fincaName: finca.name});
    }

    function deleteFincaText() {
        return $translate.instant('finca.delete-finca-text');
    }

    function deleteFincasTitle(selectedCount) {
        return $translate.instant('finca.delete-fincas-title', {count: selectedCount}, 'messageformat');
    }

    function deleteFincasActionTitle(selectedCount) {
        return $translate.instant('finca.delete-fincas-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteFincasText () {
        return $translate.instant('finca.delete-fincas-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getFincaTitle(finca) {
        return finca ? finca.name : '';
    }

    function saveFinca(finca) {
        var deferred = $q.defer();
        fincaService.saveFinca(finca).then(
            function success(savedFinca) {
                $rootScope.$broadcast('fincaSaved');
                var fincas = [ savedFinca ];
                customerService.applyAssignedCustomersInfo(fincas).then(
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
        return vm.fincasScope === 'customer_user';
    }


    function assignToCustomer($event, fincaIds) {
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
                    controller: 'AssignFincaToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: assignToCustomerTemplate,
                    locals: {fincaIds: fincaIds, customers: customers},
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

    function addFincasToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        fincaService.getTenantFincas({limit: pageSize, textSearch: ''}, false).then(
            function success(_fincas) {
                var fincas = {
                    pageSize: pageSize,
                    data: _fincas.data,
                    nextPageLink: _fincas.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _fincas.hasNext,
                    pending: false
                };
                if (fincas.hasNext) {
                    fincas.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddFincasToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addFincasToCustomerTemplate,
                    locals: {customerId: customerId, fincas: fincas},
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

    function assignFincasToCustomer($event, items) {
        var fincaIds = [];
        for (var id in items.selections) {
            fincaIds.push(id);
        }
        assignToCustomer($event, fincaIds);
    }

    function unassignFromCustomer($event, finca, isPublic) {
        if ($event) {
            $event.stopPropagation();
        }
        var title;
        var content;
        var label;
        if (isPublic) {
            title = $translate.instant('finca.make-private-finca-title', {fincaName: finca.name});
            content = $translate.instant('finca.make-private-finca-text');
            label = $translate.instant('finca.make-private');
        } else {
            title = $translate.instant('finca.unassign-finca-title', {fincaName: finca.name});
            content = $translate.instant('finca.unassign-finca-text');
            label = $translate.instant('finca.unassign-finca');
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            fincaService.unassignFincaFromCustomer(finca.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function unassignFincasFromCustomer($event, items) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('finca.unassign-fincas-title', {count: items.selectedCount}, 'messageformat'))
            .htmlContent($translate.instant('finca.unassign-fincas-text'))
            .ariaLabel($translate.instant('finca.unassign-finca'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            var tasks = [];
            for (var id in items.selections) {
                tasks.push(fincaService.unassignFincaFromCustomer(id));
            }
            $q.all(tasks).then(function () {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, finca) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('finca.make-public-finca-title', {fincaName: finca.name}))
            .htmlContent($translate.instant('finca.make-public-finca-text'))
            .ariaLabel($translate.instant('finca.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            fincaService.makeFincaPublic(finca.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }
}
