package com.taoxier.smartdochub.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.common.result.PageResult;
import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.document.model.entity.AsyncTask;
import com.taoxier.smartdochub.document.service.AsyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理-任务管理")
@RestController
@RequestMapping("/api/admin/task")
@RequiredArgsConstructor
@Slf4j
public class AdminTaskController {

    private final AsyncTaskService asyncTaskService;

    @GetMapping("/page")
    @Operation(summary = "分页查询任务列表")
    @PreAuthorize("@ss.hasPerm('doc:task:query')")
    public PageResult<AsyncTask> queryTaskPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status) {

        Page<AsyncTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AsyncTask> wrapper = new LambdaQueryWrapper<>();

        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq(AsyncTask::getTaskType, taskType);
        }

        if (status != null && !status.isEmpty()) {
            wrapper.eq(AsyncTask::getStatus, status);
        }

        wrapper.orderByDesc(AsyncTask::getCreateTime);
        IPage<AsyncTask> taskPage = asyncTaskService.page(page, wrapper);
        return PageResult.success(taskPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情")
    @PreAuthorize("@ss.hasPerm('doc:task:query')")
    public Result<AsyncTask> getTask(@PathVariable Long id) {
        AsyncTask task = asyncTaskService.getById(id);
        return Result.success(task);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    @PreAuthorize("@ss.hasPerm('doc:task:delete')")
    public Result<Void> deleteTask(@PathVariable Long id) {
        asyncTaskService.removeById(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除任务")
    @PreAuthorize("@ss.hasPerm('doc:task:delete')")
    public Result<Void> batchDeleteTasks(@RequestBody List<Long> ids) {
        asyncTaskService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取任务统计数据")
    @PreAuthorize("@ss.hasPerm('doc:task:query')")
    public Result<Object> getTaskStats() {
        long totalTasks = asyncTaskService.count();
        long pendingTasks = asyncTaskService.count(new LambdaQueryWrapper<AsyncTask>()
                .eq(AsyncTask::getStatus, "PENDING"));
        long runningTasks = asyncTaskService.count(new LambdaQueryWrapper<AsyncTask>()
                .eq(AsyncTask::getStatus, "RUNNING"));
        long completedTasks = asyncTaskService.count(new LambdaQueryWrapper<AsyncTask>()
                .eq(AsyncTask::getStatus, "COMPLETED"));
        long failedTasks = asyncTaskService.count(new LambdaQueryWrapper<AsyncTask>()
                .eq(AsyncTask::getStatus, "FAILED"));

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalTasks", totalTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("runningTasks", runningTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("failedTasks", failedTasks);

        return Result.success(stats);
    }
}
