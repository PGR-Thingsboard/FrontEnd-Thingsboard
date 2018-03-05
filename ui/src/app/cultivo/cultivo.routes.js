
import cultivosTemplate from './cultivos.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function CultivoRoutes($stateProvider, types) {
    $stateProvider
        .state('home.cultivos', {
            url: '/cultivos',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: cultivosTemplate,
                    controller: 'CultivoController',
                    controllerAs: 'vm'
                }
            },
            data: {
                cultivosType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.cultivo,
                pageTitle: 'cultivo.cultivos'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "cultivo.cultivos"}'
            }
        })
        .state('home.customers.cultivos', {
            url: '/:customerId/cultivos',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: cultivosTemplate,
                    controllerAs: 'vm',
                    controller: 'CultivoController'
                }
            },
            data: {
                cultivosType: 'customer',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.cultivo,
                pageTitle: 'customer.cultivos'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "{{ vm.customerCultivosTitle }}", "translate": "false"}'
            }
        });

}