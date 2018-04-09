/*
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* eslint-disable import/no-unresolved, import/default */

import assetFieldsetTemplate from './asset-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function AssetDirective($compile, $templateCache, toast, $translate,$mdDialog, types, assetService, customerService, $log) {
    
    var linker = function (scope, element) {
        var template = $templateCache.get(assetFieldsetTemplate);
        element.html(template);
        
        scope.stepsModel = [];


        scope.types = types;
        scope.isAssignedToCustomer = false;
        scope.isPublic = false;
        scope.assignedCustomer = null;

        scope.$watch('asset', function(newVal) {
            if (newVal) {
                if (scope.asset.customerId && scope.asset.customerId.id !== types.id.nullUid) {
                    scope.isAssignedToCustomer = true;
                    customerService.getShortCustomerInfo(scope.asset.customerId.id).then(
                        function success(customer) {
                            scope.assignedCustomer = customer;
                            scope.isPublic = customer.isPublic;
                        }
                    );
                } else {
                    scope.isAssignedToCustomer = false;
                    scope.isPublic = false;
                    scope.assignedCustomer = null;
                }
            }
        });

        scope.onAssetIdCopied = function() {
            toast.showSuccess($translate.instant('asset.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };

        scope.uploadimage = function(files){
            $log.log("entro");
            var reader = new FileReader();
            reader.onload = function(e){
                scope.$apply(function(){
                    $log.log("entro onload");
                    scope.stepsModel.push(e.target.result);
                });
            }
            reader.readAsDataURL(files[0]);
            scope.asset.fachada = files[0];
        };

        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            asset: '=',
            isEdit: '=',
            assetScope: '=',
            theForm: '=',
            onAssignToCustomer: '&',
            onMakePublic: '&',
            onUnassignFromCustomer: '&',
            onDeleteAsset: '&'
        }
    };
}
