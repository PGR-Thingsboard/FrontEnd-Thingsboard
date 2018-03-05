

export default angular.module('thingsboard.api.cultivo', [])
    .factory('cultivoService', CultivoService)
    .name;

/*@ngInject*/
function CultivoService($http, $q, customerService, userService) {

    var service = {
        getCultivo: getCultivo,
        getCultivos: getCultivos,
        saveCultivo: saveCultivo,
        deleteCultivo: deleteCultivo,
        assignCultivoToCustomer: assignCultivoToCustomer,
        unassignCultivoFromCustomer: unassignCultivoFromCustomer,
        makeCultivoPublic: makeCultivoPublic,
        getTenantCultivos: getTenantCultivos,
        getCustomerCultivos: getCustomerCultivos,
        findByQuery: findByQuery,
        fetchCultivosByNameFilter: fetchCultivosByNameFilter,
        getCultivoTypes: getCultivoTypes
    }

    return service;

    function getCultivo(cultivoId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/cultivo/' + cultivoId;
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

    function getCultivos(cultivoIds, config) {
        var deferred = $q.defer();
        var ids = '';
        for (var i=0;i<cultivoIds.length;i++) {
            if (i>0) {
                ids += ',';
            }
            ids += cultivoIds[i];
        }
        var url = '/api/cultivos?cultivoIds=' + ids;
        $http.get(url, config).then(function success(response) {
            var cultivos = response.data;
            cultivos.sort(function (cultivo1, cultivo2) {
                var id1 =  cultivo1.id.id;
                var id2 =  cultivo2.id.id;
                var index1 = cultivoIds.indexOf(id1);
                var index2 = cultivoIds.indexOf(id2);
                return index1 - index2;
            });
            deferred.resolve(cultivos);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function saveCultivo(cultivo, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/cultivo';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, cultivo, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteCultivo(cultivoId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/cultivo/' + cultivoId;
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

    function assignCultivoToCustomer(customerId, cultivoId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/cultivo/' + cultivoId;
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

    function unassignCultivoFromCustomer(cultivoId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/cultivo/' + cultivoId;
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

    function makeCultivoPublic(cultivoId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/public/cultivo/' + cultivoId;
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

    function getTenantCultivos(pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/tenant/cultivos?limit=' + pageLink.limit;
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

    function getCustomerCultivos(customerId, pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/cultivos?limit=' + pageLink.limit;
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
        var url = '/api/cultivos';
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

    function fetchCultivosByNameFilter(cultivoNameFilter, limit, applyCustomersInfo, config) {
        var deferred = $q.defer();
        var user = userService.getCurrentUser();
        var promise;
        var pageLink = {limit: limit, textSearch: cultivoNameFilter};
        if (user.authority === 'CUSTOMER_USER') {
            var customerId = user.customerId;
            promise = getCustomerCultivos(customerId, pageLink, applyCustomersInfo, config);
        } else {
            promise = getTenantCultivos(pageLink, applyCustomersInfo, config);
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

    function getCultivoTypes(config) {
        var deferred = $q.defer();
        var url = '/api/cultivo/types';
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

}
