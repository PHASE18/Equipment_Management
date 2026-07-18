package com.equipment.management.service.impl;

import com.equipment.management.entity.DeviceAttachment;
import com.equipment.management.mapper.DeviceAttachmentMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.DeviceAttachmentService;
import org.springframework.stereotype.Service;

@Service
/** 设备附件元数据服务实现。 */
public class DeviceAttachmentServiceImpl extends BaseCrudServiceImpl<DeviceAttachmentMapper, DeviceAttachment>
        implements DeviceAttachmentService {
}
