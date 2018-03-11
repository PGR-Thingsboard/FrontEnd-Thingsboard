

import uiRouter from 'angular-ui-router';
import thingsboardGrid from '../components/grid.directive';
import thingsboardApiUser from '../api/user.service';
import thingsboardApiFarm from '../api/farm.service';
import thingsboardApiCustomer from '../api/customer.service';

import FarmRoutes from './farm.routes';
import {FarmController, FarmCardController} from './farm.controller';
import AssignFarmToCustomerController from './assign-to-customer.controller';
import AddFarmsToCustomerController from './add-farms-to-customer.controller';
import FarmDirective from './farm.directive';

export default angular.module('thingsboard.farm', [
    uiRouter,
    thingsboardGrid,
    thingsboardApiUser,
    thingsboardApiFarm,
    thingsboardApiCustomer
])
    .config(FarmRoutes)
    .controller('FarmController', FarmController)
    .controller('FarmCardController', FarmCardController)
    .controller('AssignFarmToCustomerController', AssignFarmToCustomerController)
    .controller('AddFarmsToCustomerController', AddFarmsToCustomerController)
    .directive('tbFarm', FarmDirective)
    .name;


