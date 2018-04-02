

import uiRouter from 'angular-ui-router';
import thingsboardGrid from '../components/grid.directive';
import thingsboardApiUser from '../api/user.service';
import thingsboardApiParcel from '../api/parcel.service';
import thingsboardApiCustomer from '../api/customer.service';

import ParcelRoutes from './parcel.routes';
import {ParcelController, ParcelCardController} from './parcel.controller';
import AssignParcelToCustomerController from './assign-to-customer.controller';
import AddParcelsToCustomerController from './add-parcels-to-customer.controller';
import ParcelDirective from './parcel.directive';

export default angular.module('thingsboard.parcel', [
    uiRouter,
    thingsboardGrid,
    thingsboardApiUser,
    thingsboardApiParcel,
    thingsboardApiCustomer
])
    .config(ParcelRoutes)
    .controller('ParcelController', ParcelController)
    .controller('ParcelCardController', ParcelCardController)
    .controller('AssignParcelToCustomerController', AssignParcelToCustomerController)
    .controller('AddParcelsToCustomerController', AddParcelsToCustomerController)
    .directive('tbParcel', ParcelDirective)
    .name;

