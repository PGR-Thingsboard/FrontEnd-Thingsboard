
import addCultivoTemplate from './add-cultivo.tpl.html';
import cultivoCard from './cultivo-card.tpl.html';
import assignToCustomerTemplate from './assign-to-customer.tpl.html';
import addCultivosToCustomerTemplate from './add-cultivos-to-customer.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function CultivoCardController(types) {

    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        if (vm.item && vm.item.customerId && vm.parentCtl.cultivosScope === 'tenant' &&
            vm.item.customerId.id != vm.types.id.nullUid && !vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }

    vm.isPublic = function() {
        if (vm.item && vm.item.assignedCustomer && vm.parentCtl.cultivosScope === 'tenant' && vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }
}


/*@ngInject*/
export function CultivoController($rootScope, userService, cultivoService, customerService, $state, $stateParams,
                                $document, $mdDialog, $q, $translate, types) {

    var customerId = $stateParams.customerId;

    var cultivoActionsList = [];

    var cultivoGroupActionsList = [];

    var vm = this;

    vm.types = types;

    vm.cultivoGridConfig = {
        deleteItemTitleFunc: deleteCultivoTitle,
        deleteItemContentFunc: deleteCultivoText,
        deleteItemsTitleFunc: deleteCultivosTitle,
        deleteItemsActionTitleFunc: deleteCultivosActionTitle,
        deleteItemsContentFunc: deleteCultivosText,

        saveItemFunc: saveCultivo,

        getItemTitleFunc: getCultivoTitle,

        itemCardController: 'CultivoCardController',
        itemCardTemplateUrl: cultivoCard,
        parentCtl: vm,

        actionsList: cultivoActionsList,
        groupActionsList: cultivoGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addCultivoTemplate,

        addItemText: function() { return $translate.instant('cultivo.add-cultivo-text') },
        noItemsText: function() { return $translate.instant('cultivo.no-cultivos-text') },
        itemDetailsText: function() { return $translate.instant('cultivo.cultivo-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.cultivoGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.cultivoGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.cultivosScope = $state.$current.data.cultivosType;

    vm.assignToCustomer = assignToCustomer;
    vm.makePublic = makePublic;
    vm.unassignFromCustomer = unassignFromCustomer;

    initController();

    function initController() {
        var fetchCultivosFunction = null;
        var deleteCultivoFunction = null;
        var refreshCultivosParamsFunction = null;

        var user = userService.getCurrentUser();

        if (user.authority === 'CUSTOMER_USER') {
            vm.cultivosScope = 'customer_user';
            customerId = user.customerId;
        }
        if (customerId) {
            vm.customerCultivosTitle = $translate.instant('customer.cultivos');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerCultivosTitle = $translate.instant('customer.public-cultivos');
                    }
                }
            );
        }

        if (vm.cultivosScope === 'tenant') {
            fetchCultivosFunction = function (pageLink, cultivoType) {
                return cultivoService.getTenantCultivos(pageLink, true, null, cultivoType);
            };
            deleteCultivoFunction = function (cultivoId) {
                return cultivoService.deleteCultivo(cultivoId);
            };
            refreshCultivosParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            cultivoActionsList.push({
                onAction: function ($event, item) {
                    makePublic($event, item);
                },
                name: function() { return $translate.instant('action.share') },
                details: function() { return $translate.instant('cultivo.make-public') },
                icon: "share",
                isEnabled: function(cultivo) {
                    return cultivo && (!cultivo.customerId || cultivo.customerId.id === types.id.nullUid);
                }
            });

            cultivoActionsList.push(
                {
                    onAction: function ($event, item) {
                        assignToCustomer($event, [ item.id.id ]);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('cultivo.assign-to-customer') },
                    icon: "assignment_ind",
                    isEnabled: function(cultivo) {
                        return cultivo && (!cultivo.customerId || cultivo.customerId.id === types.id.nullUid);
                    }
                }
            );

            cultivoActionsList.push(
                {
                    onAction: function ($event, item) {
                        unassignFromCustomer($event, item, false);
                    },
                    name: function() { return $translate.instant('action.unassign') },
                    details: function() { return $translate.instant('cultivo.unassign-from-customer') },
                    icon: "assignment_return",
                    isEnabled: function(cultivo) {
                        return cultivo && cultivo.customerId && cultivo.customerId.id !== types.id.nullUid && !cultivo.assignedCustomer.isPublic;
                    }
                }
            );

            cultivoActionsList.push({
                onAction: function ($event, item) {
                    unassignFromCustomer($event, item, true);
                },
                name: function() { return $translate.instant('action.make-private') },
                details: function() { return $translate.instant('cultivo.make-private') },
                icon: "reply",
                isEnabled: function(cultivo) {
                    return cultivo && cultivo.customerId && cultivo.customerId.id !== types.id.nullUid && cultivo.assignedCustomer.isPublic;
                }
            });

            cultivoActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('cultivo.delete') },
                    icon: "delete"
                }
            );

            cultivoGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignCultivosToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('cultivo.assign-cultivos') },
                    details: function(selectedCount) {
                        return $translate.instant('cultivo.assign-cultivos-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            cultivoGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('cultivo.delete-cultivos') },
                    details: deleteCultivosActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.cultivosScope === 'customer' || vm.cultivosScope === 'customer_user') {
            fetchCultivosFunction = function (pageLink, cultivoType) {
                return cultivoService.getCustomerCultivos(customerId, pageLink, true, null, cultivoType);
            };
            deleteCultivoFunction = function (cultivoId) {
                return cultivoService.unassignCultivoFromCustomer(cultivoId);
            };
            refreshCultivosParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.cultivosScope === 'customer') {
                cultivoActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, false);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('cultivo.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(cultivo) {
                            return cultivo && !cultivo.assignedCustomer.isPublic;
                        }
                    }
                );
                cultivoActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, true);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('cultivo.make-private') },
                        icon: "reply",
                        isEnabled: function(cultivo) {
                            return cultivo && cultivo.assignedCustomer.isPublic;
                        }
                    }
                );

                cultivoGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignCultivosFromCustomer($event, items);
                        },
                        name: function() { return $translate.instant('cultivo.unassign-cultivos') },
                        details: function(selectedCount) {
                            return $translate.instant('cultivo.unassign-cultivos-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.cultivoGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addCultivosToCustomer($event);
                    },
                    name: function() { return $translate.instant('cultivo.assign-cultivos') },
                    details: function() { return $translate.instant('cultivo.assign-new-cultivo') },
                    icon: "add"
                };


            } else if (vm.cultivosScope === 'customer_user') {
                vm.cultivoGridConfig.addItemAction = {};
            }
        }

        vm.cultivoGridConfig.refreshParamsFunc = refreshCultivosParamsFunction;
        vm.cultivoGridConfig.fetchItemsFunc = fetchCultivosFunction;
        vm.cultivoGridConfig.deleteItemFunc = deleteCultivoFunction;

    }

    function deleteCultivoTitle(cultivo) {
        return $translate.instant('cultivo.delete-cultivo-title', {cultivoName: cultivo.name});
    }

    function deleteCultivoText() {
        return $translate.instant('cultivo.delete-cultivo-text');
    }

    function deleteCultivosTitle(selectedCount) {
        return $translate.instant('cultivo.delete-cultivos-title', {count: selectedCount}, 'messageformat');
    }

    function deleteCultivosActionTitle(selectedCount) {
        return $translate.instant('cultivo.delete-cultivos-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteCultivosText () {
        return $translate.instant('cultivo.delete-cultivos-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getCultivoTitle(cultivo) {
        return cultivo ? cultivo.name : '';
    }

    function saveCultivo(cultivo) {
        var deferred = $q.defer();
        cultivoService.saveCultivo(cultivo).then(
            function success(savedCultivo) {
                $rootScope.$broadcast('cultivoSaved');
                var cultivos = [ savedCultivo ];
                customerService.applyAssignedCustomersInfo(cultivos).then(
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
        return vm.cultivosScope === 'customer_user';
    }

    function assignToCustomer($event, cultivoIds) {
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
                    controller: 'AssignCultivoToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: assignToCustomerTemplate,
                    locals: {cultivoIds: cultivoIds, customers: customers},
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

    function addCultivosToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        cultivoService.getTenantCultivos({limit: pageSize, textSearch: ''}, false).then(
            function success(_cultivos) {
                var cultivos = {
                    pageSize: pageSize,
                    data: _cultivos.data,
                    nextPageLink: _cultivos.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _cultivos.hasNext,
                    pending: false
                };
                if (cultivos.hasNext) {
                    cultivos.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddCultivosToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addCultivosToCustomerTemplate,
                    locals: {customerId: customerId, cultivos: cultivos},
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

    function assignCultivosToCustomer($event, items) {
        var cultivoIds = [];
        for (var id in items.selections) {
            cultivoIds.push(id);
        }
        assignToCustomer($event, cultivoIds);
    }

    function unassignFromCustomer($event, cultivo, isPublic) {
        if ($event) {
            $event.stopPropagation();
        }
        var title;
        var content;
        var label;
        if (isPublic) {
            title = $translate.instant('cultivo.make-private-cultivo-title', {cultivoName: cultivo.name});
            content = $translate.instant('cultivo.make-private-cultivo-text');
            label = $translate.instant('cultivo.make-private');
        } else {
            title = $translate.instant('cultivo.unassign-cultivo-title', {cultivoName: cultivo.name});
            content = $translate.instant('cultivo.unassign-cultivo-text');
            label = $translate.instant('cultivo.unassign-cultivo');
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            cultivoService.unassignCultivoFromCustomer(cultivo.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function unassignCultivosFromCustomer($event, items) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('cultivo.unassign-cultivos-title', {count: items.selectedCount}, 'messageformat'))
            .htmlContent($translate.instant('cultivo.unassign-cultivos-text'))
            .ariaLabel($translate.instant('cultivo.unassign-cultivo'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            var tasks = [];
            for (var id in items.selections) {
                tasks.push(cultivoService.unassignCultivoFromCustomer(id));
            }
            $q.all(tasks).then(function () {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, cultivo) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('cultivo.make-public-cultivo-title', {cultivoName: cultivo.name}))
            .htmlContent($translate.instant('cultivo.make-public-cultivo-text'))
            .ariaLabel($translate.instant('cultivo.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            cultivoService.makeCultivoPublic(cultivo.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }
}
