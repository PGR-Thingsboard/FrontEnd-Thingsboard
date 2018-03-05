

import uiRouter from 'angular-ui-router';
import thingsboardGrid from '../components/grid.directive';
import thingsboardApiUser from '../api/user.service';
import thingsboardApiFinca from '../api/finca.service';
import thingsboardApiCustomer from '../api/customer.service';

import FincaRoutes from './finca.routes';
import {FincaController, FincaCardController} from './finca.controller';
import AssignFincaToCustomerController from './assign-to-customer.controller';
import AddFincasToCustomerController from './add-fincas-to-customer.controller';
import FincaDirective from './finca.directive';

export default angular.module('thingsboard.finca', [
    uiRouter,
    thingsboardGrid,
    thingsboardApiUser,
    thingsboardApiFinca,
    thingsboardApiCustomer
])
    .config(FincaRoutes)
    .controller('FincaController', FincaController)
    .controller('FincaCardController', FincaCardController)
    .controller('AssignFincaToCustomerController', AssignFincaToCustomerController)
    .controller('AddFincasToCustomerController', AddFincasToCustomerController)
    .directive('tbFinca', FincaDirective)
    .name;


