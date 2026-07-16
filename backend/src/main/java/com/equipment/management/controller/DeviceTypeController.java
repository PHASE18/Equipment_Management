package com.equipment.management.controller;

import com.equipment.management.annotation.CrudPermission;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.constant.DictTypeConstants;
import com.equipment.management.common.controller.TypedDictCrudController;
import com.equipment.management.service.DictService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@CrudPermission(module = "system:device-type")
@RequestMapping("/api/device-type")
public class DeviceTypeController extends TypedDictCrudController {

    public DeviceTypeController(DictService dictService) {
        super(dictService);
    }

    @Override
    protected String dictType() {
        return DictTypeConstants.DEVICE_TYPE;
    }
}
