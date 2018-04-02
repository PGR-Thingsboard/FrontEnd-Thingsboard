

export default angular.module('thingsboard.api.parcel', [])
    .factory('parcelService', ParcelService)
    .name;

/*@ngInject*/
function ParcelService($http, $q, customerService, userService, $log) {

    var service = {
        getParcel: getParcel,
        getParcels: getParcels,
        getAllparcels: getAllparcels,
        saveParcel: saveParcel,
        deleteParcel: deleteParcel,
        assignParcelToCustomer: assignParcelToCustomer,
        unassignParcelFromCustomer: unassignParcelFromCustomer,
        makeParcelPublic: makeParcelPublic,
        getTenantParcels: getTenantParcels,
        getCustomerParcels: getCustomerParcels,
        findByQuery: findByQuery,
        fetchParcelsByNameFilter: fetchParcelsByNameFilter,
        getParcelTypes: getParcelTypes
    }

    return service;

    function getParcel(parcelId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/parcel/' + parcelId;
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

    function getParcels(parcelIds, config) {
        var deferred = $q.defer();
        var ids = '';
        for (var i=0;i<parcelIds.length;i++) {
            if (i>0) {
                ids += ',';
            }
            ids += parcelIds[i];
        }
        var url = '/api/parcels?parcelIds=' + ids;
        $http.get(url, config).then(function success(response) {
            var parcels = response.data;
            parcels.sort(function (parcel1, parcel2) {
                var id1 =  parcel1.id.id;
                var id2 =  parcel2.id.id;
                var index1 = parcelIds.indexOf(id1);
                var index2 = parcelIds.indexOf(id2);
                return index1 - index2;
            });
            deferred.resolve(parcels);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getAllparcels(config) {
        var deferred = $q.defer();
        var parcels;
        var url = '/api/Allparcels';
        $http.get(url,config).then(function success(response) {
            parcels=response.data;
            deferred.resolve(parcels);
        }, function fail() {
            deferred.reject();
        });
        $log.log(deferred.promise);
        return deferred.promise;
    }

    function saveParcel(parcel, ignoreErrors, config) {
        $log.log(parcel);
        var deferred = $q.defer();
        var url = '/api/parcel';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, parcel, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteParcel(parcelId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/parcel/' + parcelId;
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

    function assignParcelToCustomer(customerId, parcelId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/parcel/' + parcelId;
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

    function unassignParcelFromCustomer(parcelId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/parcel/' + parcelId;
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

    function makeParcelPublic(parcelId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/public/parcel/' + parcelId;
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

    function getTenantParcels(pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/tenant/parcels?limit=' + pageLink.limit;
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

    function getCustomerParcels(customerId, pageLink, applyCustomersInfo, config, type) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/parcels?limit=' + pageLink.limit;
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
        var url = '/api/parcels';
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

    function fetchParcelsByNameFilter(parcelNameFilter, limit, applyCustomersInfo, config) {
        var deferred = $q.defer();
        var user = userService.getCurrentUser();
        var promise;
        var pageLink = {limit: limit, textSearch: parcelNameFilter};
        if (user.authority === 'CUSTOMER_USER') {
            var customerId = user.customerId;
            promise = getCustomerParcels(customerId, pageLink, applyCustomersInfo, config);
        } else {
            promise = getTenantParcels(pageLink, applyCustomersInfo, config);
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

    function getParcelTypes(config) {
        var deferred = $q.defer();
        var url = '/api/parcel/types';
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

}
