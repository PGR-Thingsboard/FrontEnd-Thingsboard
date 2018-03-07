

export default angular.module('thingsboard.api.finca', [])
    .factory('fincaService', FincaService)
    .name;

/*@ngInject*/
function FincaService($http, $q, customerService, userService, $log) {

    var service = {
        getFinca: getFinca,
        getFincas: getFincas,
        getAllFincas : getAllFincas(),
        saveFinca: saveFinca,
        deleteFinca: deleteFinca,
        assignFincaToCustomer: assignFincaToCustomer,
        unassignFincaFromCustomer: unassignFincaFromCustomer,
        makeFincaPublic: makeFincaPublic,
        getTenantFincas: getTenantFincas,
        getCustomerFincas: getCustomerFincas,
        findByQuery: findByQuery,
        fetchFincasByNameFilter: fetchFincasByNameFilter,
        getFincaTypes: getFincaTypes
    }

    return service;

    function getFinca(fincaId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/finca/' + fincaId;
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

    function getFincas(fincaIds, config) {
        var deferred = $q.defer();
        var ids = '';
        for (var i=0;i<fincaIds.length;i++) {
            if (i>0) {
                ids += ',';
            }
            ids += fincaIds[i];
        }
        var url = '/api/fincas?fincaIds=' + ids;
        $http.get(url, config).then(function success(response) {
            var fincas = response.data;
            fincas.sort(function (finca1, finca2) {
                var id1 =  finca1.id.id;
                var id2 =  finca2.id.id;
                var index1 = fincaIds.indexOf(id1);
                var index2 = fincaIds.indexOf(id2);
                return index1 - index2;
            });
            deferred.resolve(fincas);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getAllFincas(config) {
        var deferred = $q.defer();
        var fincas;
        var url = '/api/Allfincas';
        $http.get(url,config).then(function success(response) {
            fincas = response.data;
            $log.log(fincas)
            deferred.resolve(fincas);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function saveFinca(finca, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/finca';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, finca, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteFinca(fincaId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/finca/' + fincaId;
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

    function assignFincaToCustomer(customerId, fincaId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/finca/' + fincaId;
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

    function unassignFincaFromCustomer(fincaId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/finca/' + fincaId;
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

    function makeFincaPublic(fincaId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/public/finca/' + fincaId;
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

    function getTenantFincas(pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/tenant/fincas?limit=' + pageLink.limit;
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

    function getCustomerFincas(customerId, pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/fincas?limit=' + pageLink.limit;
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
        var url = '/api/fincas';
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

    function fetchFincasByNameFilter(fincaNameFilter, limit, applyCustomersInfo, config) {
        var deferred = $q.defer();
        var user = userService.getCurrentUser();
        var promise;
        var pageLink = {limit: limit, textSearch: fincaNameFilter};
        if (user.authority === 'CUSTOMER_USER') {
            var customerId = user.customerId;
            promise = getCustomerFincas(customerId, pageLink, applyCustomersInfo, config);
        } else {
            promise = getTenantFincas(pageLink, applyCustomersInfo, config);
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

    function getFincaTypes(config) {
        var deferred = $q.defer();
        var url = '/api/finca/types';
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

}
