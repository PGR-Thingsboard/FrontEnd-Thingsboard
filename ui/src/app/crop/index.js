

import uiRouter from 'angular-ui-router';
import thingsboardGrid from '../components/grid.directive';
import thingsboardApiUser from '../api/user.service';
import thingsboardApiCrop from '../api/crop.service';
import thingsboardApiCustomer from '../api/customer.service';

import CropRoutes from './crop.routes';
import {CropController, CropCardController} from './crop.controller';
import AssignCropToCustomerController from './assign-to-customer.controller';
import AddCropsToCustomerController from './add-crops-to-customer.controller';
import CropDirective from './crop.directive';

export default angular.module('thingsboard.crop', [
    uiRouter,
    thingsboardGrid,
    thingsboardApiUser,
    thingsboardApiCrop,
    thingsboardApiCustomer
])
    .config(CropRoutes)
    .controller('CropController', CropController)
    .controller('CropCardController', CropCardController)
    .controller('AssignCropToCustomerController', AssignCropToCustomerController)
    .controller('AddCropsToCustomerController', AddCropsToCustomerController)
    .directive('tbCrop', CropDirective)
    .name;

