
import farmsTemplate from './farms.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function FarmRoutes($stateProvider, types) {
    $stateProvider
        .state('home.farms', {
            url: '/farms',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: farmsTemplate,
                    controller: 'FarmController',
                    controllerAs: 'vm'
                }
            },
            data: {
                farmsType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.farm,
                pageTitle: 'farm.farms'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "farm.farms"}'
            }
        })
        .state('home.customers.farms', {
            url: '/:customerId/farms',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: farmsTemplate,
                    controllerAs: 'vm',
                    controller: 'FarmController'
                }
            },
            data: {
                farmsType: 'customer',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.farm,
                pageTitle: 'customer.farms'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "{{ vm.customerFarmsTitle }}", "translate": "false"}'
            }
        });

}