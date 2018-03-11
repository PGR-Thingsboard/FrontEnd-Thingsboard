
import cropsTemplate from './crops.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function CropRoutes($stateProvider, types) {
    $stateProvider
        .state('home.crops', {
            url: '/crops',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: cropsTemplate,
                    controller: 'CropController',
                    controllerAs: 'vm'
                }
            },
            data: {
                cropsType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.crop,
                pageTitle: 'crop.crops'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "crop.crops"}'
            }
        })
        .state('home.customers.crops', {
            url: '/:customerId/crops',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: cropsTemplate,
                    controllerAs: 'vm',
                    controller: 'CropController'
                }
            },
            data: {
                cropsType: 'customer',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.crop,
                pageTitle: 'customer.crops'
            },
            ncyBreadcrumb: {
                label: '{"icon": "domain", "label": "{{ vm.customerCropsTitle }}", "translate": "false"}'
            }
        });

}