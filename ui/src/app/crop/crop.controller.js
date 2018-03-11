
import addCropTemplate from './add-crop.tpl.html';
import cropCard from './crop-card.tpl.html';
import assignToCustomerTemplate from './assign-to-customer.tpl.html';
import addCropsToCustomerTemplate from './add-crops-to-customer.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function CropCardController(types) {

    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        if (vm.item && vm.item.customerId && vm.parentCtl.cropsScope === 'tenant' &&
            vm.item.customerId.id != vm.types.id.nullUid && !vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }

    vm.isPublic = function() {
        if (vm.item && vm.item.assignedCustomer && vm.parentCtl.cropsScope === 'tenant' && vm.item.assignedCustomer.isPublic) {
            return true;
        }
        return false;
    }
}


/*@ngInject*/
export function CropController($rootScope, userService, cropService, customerService, $state, $stateParams,
                                $document, $mdDialog, $q, $translate, types, $log) {

    var customerId = $stateParams.customerId;

    var cropActionsList = [];

    var cropGroupActionsList = [];

    var vm = this;

    vm.types = types;

    vm.cropGridConfig = {
        deleteItemTitleFunc: deleteCropTitle,
        deleteItemContentFunc: deleteCropText,
        deleteItemsTitleFunc: deleteCropsTitle,
        deleteItemsActionTitleFunc: deleteCropsActionTitle,
        deleteItemsContentFunc: deleteCropsText,

        saveItemFunc: saveCrop,

        getItemTitleFunc: getCropTitle,

        itemCardController: 'CropCardController',
        itemCardTemplateUrl: cropCard,
        parentCtl: vm,

        actionsList: cropActionsList,
        groupActionsList: cropGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addCropTemplate,

        addItemText: function() { return $translate.instant('crop.add-crop-text') },
        noItemsText: function() { return $translate.instant('crop.no-crops-text') },
        itemDetailsText: function() { return $translate.instant('crop.crop-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.cropGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.cropGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.cropsScope = $state.$current.data.cropsType;

    vm.assignToCustomer = assignToCustomer;
    vm.makePublic = makePublic;
    vm.unassignFromCustomer = unassignFromCustomer;

    initController();

    function initController() {
        var fetchCropsFunction = null;
        var deleteCropFunction = null;
        var refreshCropsParamsFunction = null;

        var user = userService.getCurrentUser();

        if (user.authority === 'CUSTOMER_USER') {
            vm.cropsScope = 'customer_user';
            customerId = user.customerId;
        }
        if (customerId) {
            vm.customerCropsTitle = $translate.instant('customer.crops');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerCropsTitle = $translate.instant('customer.public-crops');
                    }
                }
            );
        }

        if (vm.cropsScope === 'tenant') {
            fetchCropsFunction = function (pageLink, cropType) {
                return cropService.getTenantCrops(pageLink, true, null, cropType);
            };
            deleteCropFunction = function (cropId) {
                return cropService.deleteCrop(cropId);
            };
            refreshCropsParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            cropActionsList.push({
                onAction: function ($event, item) {
                    makePublic($event, item);
                },
                name: function() { return $translate.instant('action.share') },
                details: function() { return $translate.instant('crop.make-public') },
                icon: "share",
                isEnabled: function(crop) {
                    return crop && (!crop.customerId || crop.customerId.id === types.id.nullUid);
                }
            });

            cropActionsList.push(
                {
                    onAction: function ($event, item) {
                        assignToCustomer($event, [ item.id.id ]);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('crop.assign-to-customer') },
                    icon: "assignment_ind",
                    isEnabled: function(crop) {
                        return crop && (!crop.customerId || crop.customerId.id === types.id.nullUid);
                    }
                }
            );

            cropActionsList.push(
                {
                    onAction: function ($event, item) {
                        unassignFromCustomer($event, item, false);
                    },
                    name: function() { return $translate.instant('action.unassign') },
                    details: function() { return $translate.instant('crop.unassign-from-customer') },
                    icon: "assignment_return",
                    isEnabled: function(crop) {
                        return crop && crop.customerId && crop.customerId.id !== types.id.nullUid && !crop.assignedCustomer.isPublic;
                    }
                }
            );

            cropActionsList.push({
                onAction: function ($event, item) {
                    unassignFromCustomer($event, item, true);
                },
                name: function() { return $translate.instant('action.make-private') },
                details: function() { return $translate.instant('crop.make-private') },
                icon: "reply",
                isEnabled: function(crop) {
                    return crop && crop.customerId && crop.customerId.id !== types.id.nullUid && crop.assignedCustomer.isPublic;
                }
            });

            cropActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('crop.delete') },
                    icon: "delete"
                }
            );

            cropGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignCropsToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('crop.assign-crops') },
                    details: function(selectedCount) {
                        return $translate.instant('crop.assign-crops-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            cropGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('crop.delete-crops') },
                    details: deleteCropsActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.cropsScope === 'customer' || vm.cropsScope === 'customer_user') {
            fetchCropsFunction = function (pageLink, cropType) {
                return cropService.getCustomerCrops(customerId, pageLink, true, null, cropType);
            };
            deleteCropFunction = function (cropId) {
                return cropService.unassignCropFromCustomer(cropId);
            };
            refreshCropsParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.cropsScope === 'customer') {
                cropActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, false);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('crop.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(crop) {
                            return crop && !crop.assignedCustomer.isPublic;
                        }
                    }
                );
                cropActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, true);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('crop.make-private') },
                        icon: "reply",
                        isEnabled: function(crop) {
                            return crop && crop.assignedCustomer.isPublic;
                        }
                    }
                );

                cropGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignCropsFromCustomer($event, items);
                        },
                        name: function() { return $translate.instant('crop.unassign-crops') },
                        details: function(selectedCount) {
                            return $translate.instant('crop.unassign-crops-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.cropGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addCropsToCustomer($event);
                    },
                    name: function() { return $translate.instant('crop.assign-crops') },
                    details: function() { return $translate.instant('crop.assign-new-crop') },
                    icon: "add"
                };


            } else if (vm.cropsScope === 'customer_user') {
                vm.cropGridConfig.addItemAction = {};
            }
        }

        vm.cropGridConfig.refreshParamsFunc = refreshCropsParamsFunction;
        vm.cropGridConfig.fetchItemsFunc = fetchCropsFunction;
        vm.cropGridConfig.deleteItemFunc = deleteCropFunction;

    }

    function deleteCropTitle(crop) {
        return $translate.instant('crop.delete-crop-title', {cropName: crop.name});
    }

    function deleteCropText() {
        return $translate.instant('crop.delete-crop-text');
    }

    function deleteCropsTitle(selectedCount) {
        return $translate.instant('crop.delete-crops-title', {count: selectedCount}, 'messageformat');
    }

    function deleteCropsActionTitle(selectedCount) {
        return $translate.instant('crop.delete-crops-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteCropsText () {
        return $translate.instant('crop.delete-crops-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getCropTitle(crop) {
        return crop ? crop.name : '';
    }

    function saveCrop(crop) {
        $log.log("TTTTTTTTTTTTTTTTTTTTTTTTT");
        $log.log(crop)
        var deferred = $q.defer();
        cropService.saveCrop(crop).then(
            function success(savedCrop) {
                $rootScope.$broadcast('cropSaved');
                var crops = [ savedCrop ];
                customerService.applyAssignedCustomersInfo(crops).then(
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
        return vm.cropsScope === 'customer_user';
    }

    function assignToCustomer($event, cropIds) {
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
                    controller: 'AssignCropToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: assignToCustomerTemplate,
                    locals: {cropIds: cropIds, customers: customers},
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

    function addCropsToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        cropService.getTenantCrops({limit: pageSize, textSearch: ''}, false).then(
            function success(_crops) {
                var crops = {
                    pageSize: pageSize,
                    data: _crops.data,
                    nextPageLink: _crops.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _crops.hasNext,
                    pending: false
                };
                if (crops.hasNext) {
                    crops.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddCropsToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addCropsToCustomerTemplate,
                    locals: {customerId: customerId, crops: crops},
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

    function assignCropsToCustomer($event, items) {
        var cropIds = [];
        for (var id in items.selections) {
            cropIds.push(id);
        }
        assignToCustomer($event, cropIds);
    }

    function unassignFromCustomer($event, crop, isPublic) {
        if ($event) {
            $event.stopPropagation();
        }
        var title;
        var content;
        var label;
        if (isPublic) {
            title = $translate.instant('crop.make-private-crop-title', {cropName: crop.name});
            content = $translate.instant('crop.make-private-crop-text');
            label = $translate.instant('crop.make-private');
        } else {
            title = $translate.instant('crop.unassign-crop-title', {cropName: crop.name});
            content = $translate.instant('crop.unassign-crop-text');
            label = $translate.instant('crop.unassign-crop');
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            cropService.unassignCropFromCustomer(crop.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function unassignCropsFromCustomer($event, items) {
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('crop.unassign-crops-title', {count: items.selectedCount}, 'messageformat'))
            .htmlContent($translate.instant('crop.unassign-crops-text'))
            .ariaLabel($translate.instant('crop.unassign-crop'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            var tasks = [];
            for (var id in items.selections) {
                tasks.push(cropService.unassignCropFromCustomer(id));
            }
            $q.all(tasks).then(function () {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, crop) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('crop.make-public-crop-title', {cropName: crop.name}))
            .htmlContent($translate.instant('crop.make-public-crop-text'))
            .ariaLabel($translate.instant('crop.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            cropService.makeCropPublic(crop.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }
}
