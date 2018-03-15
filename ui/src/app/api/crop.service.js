

export default angular.module('thingsboard.api.crop', [])
    .factory('cropService', CropService)
    .name;

/*@ngInject*/
function CropService($http, $q, customerService, userService, $log) {

    var service = {
        getCrop: getCrop,
        getCrops: getCrops,
        getAllcrops: getAllcrops,
        saveCrop: saveCrop,
        deleteCrop: deleteCrop,
        assignCropToCustomer: assignCropToCustomer,
        unassignCropFromCustomer: unassignCropFromCustomer,
        makeCropPublic: makeCropPublic,
        getTenantCrops: getTenantCrops,
        getCustomerCrops: getCustomerCrops,
        findByQuery: findByQuery,
        fetchCropsByNameFilter: fetchCropsByNameFilter,
        getCropTypes: getCropTypes
    }

    return service;

    function getCrop(cropId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/crop/' + cropId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getCrops(cropIds, config) {
        var deferred = $q.defer();
        var ids = '';
        for (var i=0;i<cropIds.length;i++) {
            if (i>0) {
                ids += ',';
            }
            ids += cropIds[i];
        }
        var url = '/api/crops?cropIds=' + ids;
        $http.get(url, config).then(function success(response) {
            var crops = response.data;
            crops.sort(function (crop1, crop2) {
                var id1 =  crop1.id.id;
                var id2 =  crop2.id.id;
                var index1 = cropIds.indexOf(id1);
                var index2 = cropIds.indexOf(id2);
                return index1 - index2;
            });
            deferred.resolve(crops);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getAllcrops(config) {
        var deferred = $q.defer();
        var crops;
        var url = '/api/Allcrops';
        $http.get(url,config).then(function success(response) {
            crops=response.data;
            deferred.resolve(crops);
        }, function fail() {
            deferred.reject();
        });
        $log.log(deferred.promise);
        return deferred.promise;
    }

    function saveCrop(crop, ignoreErrors, config) {
        $log.log(crop);
        var deferred = $q.defer();
        var url = '/api/crop';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, crop, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteCrop(cropId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/crop/' + cropId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.delete(url, config).then(function success() {
            deferred.resolve();
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function assignCropToCustomer(customerId, cropId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/crop/' + cropId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, null, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function unassignCropFromCustomer(cropId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/crop/' + cropId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.delete(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function makeCropPublic(cropId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/public/crop/' + cropId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, null, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getTenantCrops(pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/tenant/crops?limit=' + pageLink.limit;
        if (angular.isDefined(pageLink.textSearch)) {
            url += '&textSearch=' + pageLink.textSearch;
        }
        if (angular.isDefined(pageLink.idOffset)) {
            url += '&idOffset=' + pageLink.idOffset;
        }
        if (angular.isDefined(pageLink.textOffset)) {
            url += '&textOffset=' + pageLink.textOffset;
        }
        if (angular.isDefined(type) && type.length) {
            url += '&type=' + type;
        }
        $http.get(url, config).then(function success(response) {
            if (applyCustomersInfo) {
                customerService.applyAssignedCustomersInfo(response.data.data).then(
                    function success(data) {
                        response.data.data = data;
                        deferred.resolve(response.data);
                    },
                    function fail() {
                        deferred.reject();
                    }
                );
            } else {
                deferred.resolve(response.data);
            }
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getCustomerCrops(customerId, pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/crops?limit=' + pageLink.limit;
        if (angular.isDefined(pageLink.textSearch)) {
            url += '&textSearch=' + pageLink.textSearch;
        }
        if (angular.isDefined(pageLink.idOffset)) {
            url += '&idOffset=' + pageLink.idOffset;
        }
        if (angular.isDefined(pageLink.textOffset)) {
            url += '&textOffset=' + pageLink.textOffset;
        }
        if (angular.isDefined(type) && type.length) {
            url += '&type=' + type;
        }
        $http.get(url, config).then(function success(response) {
            if (applyCustomersInfo) {
                customerService.applyAssignedCustomerInfo(response.data.data, customerId).then(
                    function success(data) {
                        response.data.data = data;
                        deferred.resolve(response.data);
                    },
                    function fail() {
                        deferred.reject();
                    }
                );
            } else {
                deferred.resolve(response.data);
            }
        }, function fail() {
            deferred.reject();
        });

        return deferred.promise;
    }

    function findByQuery(query, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/crops';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, query, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function fetchCropsByNameFilter(cropNameFilter, limit, applyCustomersInfo, config) {
        var deferred = $q.defer();
        var user = userService.getCurrentUser();
        var promise;
        var pageLink = {limit: limit, textSearch: cropNameFilter};
        if (user.authority === 'CUSTOMER_USER') {
            var customerId = user.customerId;
            promise = getCustomerCrops(customerId, pageLink, applyCustomersInfo, config);
        } else {
            promise = getTenantCrops(pageLink, applyCustomersInfo, config);
        }
        promise.then(
            function success(result) {
                if (result.data && result.data.length > 0) {
                    deferred.resolve(result.data);
                } else {
                    deferred.resolve(null);
                }
            },
            function fail() {
                deferred.resolve(null);
            }
        );
        return deferred.promise;
    }

    function getCropTypes(config) {
        var deferred = $q.defer();
        var url = '/api/crop/types';
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

}
