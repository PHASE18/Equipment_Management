package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.ProjectBindRequest;
import com.equipment.management.dto.request.ProjectQuery;
import com.equipment.management.entity.Project;
import com.equipment.management.service.DeviceProjectService;
import com.equipment.management.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/project")
public class ProjectController extends BaseCrudController<ProjectService, Project> {

    private final DeviceProjectService deviceProjectService;

    public ProjectController(ProjectService projectService, DeviceProjectService deviceProjectService) {
        super(projectService);
        this.deviceProjectService = deviceProjectService;
    }

    @GetMapping("/list")
    public Result<PageResult<Project>> list(@Valid ProjectQuery query) {
        return Result.success(baseService.page(query));
    }

    @PostMapping("/bindDevice")
    public Result<Void> bindDevice(@Valid @RequestBody ProjectBindRequest request) {
        deviceProjectService.bind(request);
        return Result.success();
    }

    @DeleteMapping("/unbind")
    public Result<Void> unbindDevice(@Valid @RequestBody ProjectBindRequest request) {
        deviceProjectService.unbind(request);
        return Result.success();
    }

    @Override
    protected QueryWrapper<Project> buildQueryWrapper(PageQuery query) {
        QueryWrapper<Project> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("project_name", query.getKeyword())
                    .or().like("project_code", query.getKeyword()));
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
