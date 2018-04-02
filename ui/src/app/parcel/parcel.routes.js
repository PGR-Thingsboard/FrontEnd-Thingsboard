
import parcelsTemplate from './parcels.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function ParcelRoutes($stateProvider, types) {
    $stateProvider
        .state('home.parcels', {
            url: '/parcels',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: parcelsTemplate,
                    controller: 'ParcelController',
                    controllerAs: 'vm'
                }
            },
            data: {
                parcelsType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.parcel,
                pageTitle: 'parcel.parcels'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "parcel.parcels"}'
            }
        })
        .state('home.customers.parcels', {
            url: '/:customerId/parcels',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: parcelsTemplate,
                    controllerAs: 'vm',
                    controller: 'ParcelController'
                }
            },
            data: {
                parcelsType: 'customer',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.parcel,
                pageTitle: 'customer.parcels'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "{{ vm.customerParcelsTitle }}", "translate": "false"}'
            }
        });

}