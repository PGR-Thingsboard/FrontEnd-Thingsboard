
import fincasTemplate from './fincas.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function FincaRoutes($stateProvider, types) {
    $stateProvider
        .state('home.fincas', {
            url: '/fincas',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: fincasTemplate,
                    controller: 'FincaController',
                    controllerAs: 'vm'
                }
            },
            data: {
                fincasType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.finca,
                pageTitle: 'finca.fincas'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "finca.fincas"}'
            }
        })
        .state('home.customers.fincas', {
            url: '/:customerId/fincas',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: fincasTemplate,
                    controllerAs: 'vm',
                    controller: 'FincaController'
                }
            },
            data: {
                fincasType: 'customer',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.finca,
                pageTitle: 'customer.fincas'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "{{ vm.customerFincasTitle }}", "translate": "false"}'
            }
        });

}