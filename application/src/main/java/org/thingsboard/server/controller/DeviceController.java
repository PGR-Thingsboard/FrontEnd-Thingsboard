/**
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
package org.thingsboard.server.controller;

import com.fasterxml.jackson.databind.node.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.audit.ActionStatus;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.parcel.Parcel;
import org.thingsboard.server.common.data.device.DeviceSearchQuery;
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.kv.BasicKvEntry;
import org.thingsboard.server.common.data.kv.BasicTsKvEntry;
import org.thingsboard.server.common.data.kv.StringDataEntry;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.data.widget.WidgetType;
import org.thingsboard.server.common.data.widget.WidgetsBundle;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.exception.ThingsboardErrorCode;
import org.thingsboard.server.exception.ThingsboardException;
import org.thingsboard.server.service.security.model.SecurityUser;
import springfox.documentation.spring.web.json.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DeviceController extends BaseController {

    public static final String DEVICE_ID = "deviceId";

    @Autowired
    private TimeseriesService tsService;

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/device/{deviceId}", method = RequestMethod.GET)
    @ResponseBody
    public Device getDeviceById(@PathVariable(DEVICE_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(DEVICE_ID, strDeviceId);
        try {
            DeviceId deviceId = new DeviceId(toUUID(strDeviceId));
            return checkDeviceId(deviceId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/device", method = RequestMethod.POST)
    @ResponseBody
    public Device saveDevice(@RequestBody Device device) throws ThingsboardException {
        try {
            device.setTenantId(getCurrentUser().getTenantId());
            if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
                if (device.getId() == null || device.getId().isNullUid() ||
                    device.getCustomerId() == null || device.getCustomerId().isNullUid()) {
                    throw new ThingsboardException("You don't have permission to perform this operation!",
                            ThingsboardErrorCode.PERMISSION_DENIED);
                } else {
                    checkCustomerId(device.getCustomerId());
                }
            }
            Device savedDevice = checkNotNull(deviceService.saveDevice(device));
            SpatialDevice spatialDevice = new SpatialDevice(savedDevice.getId().getId().toString(),savedDevice.getParcelId(),device.getLocation());
            mongoService.getMongodbDevice().save(spatialDevice);
            //-----------------------Agregando valor a la fuerza en base de datos
            StringDataEntry value = new StringDataEntry("prueba","3");
            long millis = System.currentTimeMillis();
            BasicTsKvEntry tsKvEntry = new BasicTsKvEntry(millis, value);
            tsService.save(savedDevice.getId(),tsKvEntry);
            //----------------------------------------------------------------
            /*ObjectMapper mapper = new ObjectMapper();
            ParcelId parcelId = new ParcelId(UUID.fromString(savedDevice.getParcelId()));
            Parcel parcel = parcelService.findParcelById(parcelId);
            FarmId farmId = new FarmId(UUID.fromString(parcel.getFarmId()));
            Farm farm = farmService.findFarmById(farmId);
            DashboardId dashboardId = new DashboardId(UUID.fromString(farm.getDashboardId()));
            Dashboard dashboard = dashboardService.findDashboardById(dashboardId);
            System.out.println("Obtuvo el dashboard desde el dispositivo");
            JsonNode widgets = null;
            if(dashboard.getConfiguration().has("widgets")){
                System.out.println("Sacó el arreglo de widgets del dashboard");
                widgets = dashboard.getConfiguration().get("widgets");
            }


            WidgetType w = new WidgetType();
            w.setName(savedDevice.getName());
            w.setTenantId(dashboard.getTenantId());
            w.setBundleAlias("digital_gauges");
            w.setDescriptor(mapper.readTree("{\"k1\":\"v1\"}"));
            WidgetsBundle wb = new WidgetsBundle();
            wb.setTenantId(dashboard.getTenantId());
            wb.setAlias("digital_gauges");
            System.out.println(savedDevice.getName());
            //WidgetType ws = widgetTypeService.saveWidgetType(w);
            //WidgetsBundle wbs = widgetsBundleService.saveWidgetsBundle(wb);
            String widgetJson = "{ "+'"'+savedDevice.getId().getId().toString()+'"'+": { \"isSystemType\": true, \"bundleAlias\": \"digital_gauges\", \"typeAlias\": \"digital_thermometer\", \"type\": \"latest\", \"title\": \"New widget\", \"sizeX\": 3, \"sizeY\": 3, \"config\": { \"datasources\": [ { \"type\": \"entity\", \"dataKeys\": [ { \"name\": \"prueba\", \"type\": \"timeseries\", \"label\": \"prueba\", \"color\": \"#2196f3\", \"settings\": {}, \"_hash\": 0.6329654774211 } ], \"entityAliasId\": "+'"'+savedDevice.getId().getId().toString()+'"'+" } ], \"timewindow\": { \"realtime\": { \"timewindowMs\": 60000 } }, \"showTitle\": false, \"backgroundColor\": \"#000000\", \"color\": \"rgba(255, 254, 254, 0.87)\", \"padding\": \"0px\", \"settings\": { \"maxValue\": 60, \"donutStartAngle\": 90, \"showValue\": true, \"showMinMax\": true, \"gaugeWidthScale\": 1, \"levelColors\": [ \"#304ffe\", \"#7e57c2\", \"#ff4081\", \"#d32f2f\" ], \"titleFont\": { \"family\": \"Roboto\", \"size\": 12, \"style\": \"normal\", \"weight\": \"500\" }, \"labelFont\": { \"family\": \"Roboto\", \"size\": 8, \"style\": \"normal\", \"weight\": \"500\" }, \"valueFont\": { \"family\": \"Segment7Standard\", \"style\": \"normal\", \"weight\": \"500\", \"size\": 18 }, \"minMaxFont\": { \"family\": \"Segment7Standard\", \"size\": 12, \"style\": \"normal\", \"weight\": \"500\" }, \"dashThickness\": 1.5, \"minValue\": -60, \"gaugeColor\": \"#333333\", \"neonGlowBrightness\": 35, \"gaugeType\": \"donut\", \"animation\": true, \"animationDuration\": 500, \"animationRule\": \"linear\", \"timestampFormat\": \"yyyy-MM-dd HH:mm:ss\" }, \"title\": \"New Digital thermometer\", \"dropShadow\": true, \"enableFullscreen\": true, \"titleStyle\": { \"fontSize\": \"16px\", \"fontWeight\": 400 }, \"widgetStyle\": {}, \"useDashboardTimewindow\": true, \"showLegend\": false, \"actions\": {} }, \"id\": \"cdb3310f-b429-3419-9a1b-c0b6fdf43643\" }, \"aliasesInfo\": { \"datasourceAliases\": { \"0\": { \"alias\": "+'"'+savedDevice.getName()+'"'+", \"filter\": { \"type\": \"singleEntity\", \"resolveMultiple\": false, \"singleEntity\": { \"entityType\": \"DEVICE\", \"id\": "+'"'+savedDevice.getId().getId().toString()+'"'+" } } } }, \"targetDeviceAliases\": {} }, \"originalSize\": { \"sizeX\": 3, \"sizeY\": 3 }, \"originalColumns\": 24 }";
            JsonNode newWidget = mapper.readTree(widgetJson);
            System.out.println("Convirtió en JsonNode el widget que se creo de un string");
            ((ObjectNode) widgets).put("widgets",newWidget);
            System.out.println("Agrego el widget al arreglo de widgets");
            ObjectNode dashboardTotal = (ObjectNode) mapper.readTree(dashboard.getConfiguration().toString());
            System.out.println("Paso el dashboard JsonNode a ObjectNode");
            dashboardTotal.put("widgets",widgets);
            System.out.println("Reemplazó el arreglo de widgets");
            JsonNode dashboardFin =  dashboardTotal;
            dashboard.setConfiguration(dashboardFin);
            System.out.println("Se reemplaza la configuración del dashboard");
            dashboardService.saveDashboard(dashboard);
            System.out.println("Se actualizó el dashboard");
            */
            actorService
                    .onDeviceNameOrTypeUpdate(
                            savedDevice.getTenantId(),
                            savedDevice.getId(),
                            savedDevice.getName(),
                            savedDevice.getType());

            logEntityAction(savedDevice.getId(), savedDevice,
                    savedDevice.getCustomerId(),
                    device.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return savedDevice;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DEVICE), device,
                    null, device.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/device/{deviceId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteDevice(@PathVariable(DEVICE_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(DEVICE_ID, strDeviceId);
        try {
            DeviceId deviceId = new DeviceId(toUUID(strDeviceId));
            Device device = checkDeviceId(deviceId);
            deviceService.deleteDevice(deviceId);

            logEntityAction(deviceId, device,
                    device.getCustomerId(),
                    ActionType.DELETED, null, strDeviceId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DEVICE),
                    null,
                    null,
                    ActionType.DELETED, e, strDeviceId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/device/{deviceId}", method = RequestMethod.POST)
    @ResponseBody
    public Device assignDeviceToCustomer(@PathVariable("customerId") String strCustomerId,
                                         @PathVariable(DEVICE_ID) String strDeviceId) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        checkParameter(DEVICE_ID, strDeviceId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            DeviceId deviceId = new DeviceId(toUUID(strDeviceId));
            checkDeviceId(deviceId);

            Device savedDevice = checkNotNull(deviceService.assignDeviceToCustomer(deviceId, customerId));

            logEntityAction(deviceId, savedDevice,
                    savedDevice.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strDeviceId, strCustomerId, customer.getName());

            return savedDevice;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DEVICE), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strDeviceId, strCustomerId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/device/{deviceId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Device unassignDeviceFromCustomer(@PathVariable(DEVICE_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(DEVICE_ID, strDeviceId);
        try {
            DeviceId deviceId = new DeviceId(toUUID(strDeviceId));
            Device device = checkDeviceId(deviceId);
            if (device.getCustomerId() == null || device.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
                throw new IncorrectParameterException("Device isn't assigned to any customer!");
            }
            Customer customer = checkCustomerId(device.getCustomerId());

            Device savedDevice = checkNotNull(deviceService.unassignDeviceFromCustomer(deviceId));

            logEntityAction(deviceId, device,
                    device.getCustomerId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strDeviceId, customer.getId().toString(), customer.getName());

            return savedDevice;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DEVICE), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strDeviceId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/device/{deviceId}", method = RequestMethod.POST)
    @ResponseBody
    public Device assignDeviceToPublicCustomer(@PathVariable(DEVICE_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(DEVICE_ID, strDeviceId);
        try {
            DeviceId deviceId = new DeviceId(toUUID(strDeviceId));
            Device device = checkDeviceId(deviceId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(device.getTenantId());
            Device savedDevice = checkNotNull(deviceService.assignDeviceToCustomer(deviceId, publicCustomer.getId()));

            logEntityAction(deviceId, savedDevice,
                    savedDevice.getCustomerId(),
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strDeviceId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedDevice;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DEVICE), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strDeviceId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/device/{deviceId}/credentials", method = RequestMethod.GET)
    @ResponseBody
    public DeviceCredentials getDeviceCredentialsByDeviceId(@PathVariable(DEVICE_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(DEVICE_ID, strDeviceId);
        try {
            DeviceId deviceId = new DeviceId(toUUID(strDeviceId));
            Device device = checkDeviceId(deviceId);
            DeviceCredentials deviceCredentials = checkNotNull(deviceCredentialsService.findDeviceCredentialsByDeviceId(deviceId));
            logEntityAction(deviceId, device,
                    device.getCustomerId(),
                    ActionType.CREDENTIALS_READ, null, strDeviceId);
            return deviceCredentials;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DEVICE), null,
                    null,
                    ActionType.CREDENTIALS_READ, e, strDeviceId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/device/credentials", method = RequestMethod.POST)
    @ResponseBody
    public DeviceCredentials saveDeviceCredentials(@RequestBody DeviceCredentials deviceCredentials) throws ThingsboardException {
        checkNotNull(deviceCredentials);
        try {
            Device device = checkDeviceId(deviceCredentials.getDeviceId());
            DeviceCredentials result = checkNotNull(deviceCredentialsService.updateDeviceCredentials(deviceCredentials));
            actorService.onCredentialsUpdate(getCurrentUser().getTenantId(), deviceCredentials.getDeviceId());
            logEntityAction(device.getId(), device,
                    device.getCustomerId(),
                    ActionType.CREDENTIALS_UPDATED, null, deviceCredentials);
            return result;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.DEVICE), null,
                    null,
                    ActionType.CREDENTIALS_UPDATED, e, deviceCredentials);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/devices", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Device> getTenantDevices(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length() > 0) {
                return checkNotNull(deviceService.findDevicesByTenantIdAndType(tenantId, type, pageLink));
            } else {
                return checkNotNull(deviceService.findDevicesByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/devices", params = {"deviceName"}, method = RequestMethod.GET)
    @ResponseBody
    public Device getTenantDevice(
            @RequestParam String deviceName) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(deviceService.findDeviceByTenantIdAndName(tenantId, deviceName));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/devices", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Device> getCustomerDevices(
            @PathVariable("customerId") String strCustomerId,
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            checkCustomerId(customerId);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length() > 0) {
                return checkNotNull(deviceService.findDevicesByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
            } else {
                return checkNotNull(deviceService.findDevicesByTenantIdAndCustomerId(tenantId, customerId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/devices", params = {"deviceIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Device> getDevicesByIds(
            @RequestParam("deviceIds") String[] strDeviceIds) throws ThingsboardException {
        checkArrayParameter("deviceIds", strDeviceIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<DeviceId> deviceIds = new ArrayList<>();
            for (String strDeviceId : strDeviceIds) {
                deviceIds.add(new DeviceId(toUUID(strDeviceId)));
            }
            ListenableFuture<List<Device>> devices;
            if (customerId == null || customerId.isNullUid()) {
                devices = deviceService.findDevicesByTenantIdAndIdsAsync(tenantId, deviceIds);
            } else {
                devices = deviceService.findDevicesByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, deviceIds);
            }
            return checkNotNull(devices.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/devices", method = RequestMethod.POST)
    @ResponseBody
    public List<Device> findByQuery(@RequestBody DeviceSearchQuery query) throws ThingsboardException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getDeviceTypes());
        checkEntityId(query.getParameters().getEntityId());
        try {
            List<Device> devices = checkNotNull(deviceService.findDevicesByQuery(query).get());
            devices = devices.stream().filter(device -> {
                try {
                    checkDevice(device);
                    return true;
                } catch (ThingsboardException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            return devices;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/device/types", method = RequestMethod.GET)
    @ResponseBody
    public List<EntitySubtype> getDeviceTypes() throws ThingsboardException {
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            ListenableFuture<List<EntitySubtype>> deviceTypes = deviceService.findDeviceTypesByTenantId(tenantId);
            return checkNotNull(deviceTypes.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
