

import uiRouter from 'angular-ui-router';
import thingsboardGrid from '../components/grid.directive';
import thingsboardApiUser from '../api/user.service';
import thingsboardApiCultivo from '../api/cultivo.service';
import thingsboardApiCustomer from '../api/customer.service';

import CultivoRoutes from './cultivo.routes';
import {CultivoController, CultivoCardController} from './cultivo.controller';
import AssignCultivoToCustomerController from './assign-to-customer.controller';
import AddCultivosToCustomerController from './add-cultivos-to-customer.controller';
import CultivoDirective from './cultivo.directive';

export default angular.module('thingsboard.cultivo', [
    uiRouter,
    thingsboardGrid,
    thingsboardApiUser,
    thingsboardApiCultivo,
    thingsboardApiCustomer
])
    .config(CultivoRoutes)
    .controller('CultivoController', CultivoController)
    .controller('CultivoCardController', CultivoCardController)
    .controller('AssignCultivoToCustomerController', AssignCultivoToCustomerController)
    .controller('AddCultivosToCustomerController', AddCultivosToCustomerController)
    .directive('tbCultivo', CultivoDirective)
    .name;

