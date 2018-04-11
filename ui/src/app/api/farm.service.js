

export default angular.module('thingsboard.api.farm', [])
    .factory('farmService', FarmService)
    .name;

/*@ngInject*/
function FarmService($http, $q, customerService, userService, $log) {

    var service = {
        getFarm: getFarm,
        getFarms: getFarms,
        getAllFarms : getAllFarms,
        saveFarm: saveFarm,
        getFarmClimatology: getFarmClimatology,
        deleteFarm: deleteFarm,
        assignFarmToCustomer: assignFarmToCustomer,
        unassignFarmFromCustomer: unassignFarmFromCustomer,
        makeFarmPublic: makeFarmPublic,
        getTenantFarms: getTenantFarms,
        getCustomerFarms: getCustomerFarms,
        findByQuery: findByQuery,
        fetchFarmsByNameFilter: fetchFarmsByNameFilter,
        getFarmTypes: getFarmTypes
    }

    return service;

    function getFarm(farmId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/farm/' + farmId;
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

    function getFarms(farmIds, config) {
        var deferred = $q.defer();
        var ids = '';
        for (var i=0;i<farmIds.length;i++) {
            if (i>0) {
                ids += ',';
            }
            ids += farmIds[i];
        }
        var url = '/api/farms?farmIds=' + ids;
        $http.get(url, config).then(function success(response) {
            var farms = response.data;
            farms.sort(function (farm1, farm2) {
                var id1 =  farm1.id.id;
                var id2 =  farm2.id.id;
                var index1 = farmIds.indexOf(id1);
                var index2 = farmIds.indexOf(id2);
                return index1 - index2;
            });
            deferred.resolve(farms);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getAllFarms(config) {
        var deferred = $q.defer();
        var farms;
        var url = '/api/Allfarms';
        $http.get(url,config).then(function success(response) {
            farms=response.data;
            deferred.resolve(farms);
        }, function fail() {
            deferred.reject();
        });
        $log.log(deferred.promise);
        return deferred.promise;
    }

    function getFarmClimatology(farmId,lon,lat,config) {
        $log.log(lon);
        $log.log(lat);
        var deferred = $q.defer();
        var farmClimatology;
        var url = '/api/farm/'+farmId+'/climatology/'+ lon+'/'+lat;
        $http.get(url,config).then(function success(response) {
            farmClimatology=response.data;
            deferred.resolve(farmClimatology);
        }, function fail() {
            deferred.reject();
        });
        $log.log(deferred.promise);
        return deferred.promise;
    }

    function saveFarm(farm, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/farm';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, farm, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteFarm(farmId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/farm/' + farmId;
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

    function assignFarmToCustomer(customerId, farmId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/farm/' + farmId;
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

    function unassignFarmFromCustomer(farmId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/farm/' + farmId;
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

    function makeFarmPublic(farmId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/public/farm/' + farmId;
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

    function getTenantFarms(pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/tenant/farms?limit=' + pageLink.limit;
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

    function getCustomerFarms(customerId, pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/farms?limit=' + pageLink.limit;
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
        var url = '/api/farms';
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

    function fetchFarmsByNameFilter(farmNameFilter, limit, applyCustomersInfo, config) {
        var deferred = $q.defer();
        var user = userService.getCurrentUser();
        var promise;
        var pageLink = {limit: limit, textSearch: farmNameFilter};
        if (user.authority === 'CUSTOMER_USER') {
            var customerId = user.customerId;
            promise = getCustomerFarms(customerId, pageLink, applyCustomersInfo, config);
        } else {
            promise = getTenantFarms(pageLink, applyCustomersInfo, config);
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

    function getFarmTypes(config) {
        var deferred = $q.defer();
        var url = '/api/farm/types';
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

}
