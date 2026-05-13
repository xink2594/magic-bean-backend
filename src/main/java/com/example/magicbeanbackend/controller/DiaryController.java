package com.example.magicbeanbackend.controller;

import com.example.magicbeanbackend.common.ApiResponse;
import com.example.magicbeanbackend.dto.*;
import com.example.magicbeanbackend.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 植物手记控制器
 * 提供植物日记与画廊相关接口
 */
@Validated
@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /**
     * 获取手记画廊列表（分页，只返回 id 和 imageUrl）
     * 用于 App 的"画廊"或"时间轴"页面展示
     * 只返回未删除的记录
     *
     * @param deviceId 设备 ID
     * @param page     页码（从 1 开始），默认 1
     * @param pageSize 每页条数，默认 20
     * @return 手记列表响应（含分页信息）
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<DiaryListResponse>> getDiaryList(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        DiaryListResponse response = diaryService.getDiaryList(deviceId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取手记详情
     * 用户点击某张照片后查看完整信息
     *
     * @param deviceId 设备 ID
     * @param id       记录主键 ID
     * @return 手记详情响应
     */
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<DiaryDetailResponse>> getDiaryDetail(
            @RequestParam String deviceId,
            @RequestParam Long id) {
        DiaryDetailResponse response = diaryService.getDiaryDetail(deviceId, id);
        if (response != null) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.ok(ApiResponse.fail(404, "未找到对应的日记记录"));
        }
    }

    /**
     * 保存/编辑手记内容
     * 用户在 App 画廊中点击某张由设备自动拍下的照片，为其补充文字心得
     *
     * @param request 包含 id 和 note 的请求体
     * @return 操作结果
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Void>> saveDiary(@Valid @RequestBody DiarySaveRequest request) {
        boolean success = diaryService.saveDiary(request.id(), request.note());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(null));
        } else {
            return ResponseEntity.ok(ApiResponse.fail(404, "未找到对应的日记记录"));
        }
    }

    /**
     * 软删除手记
     * 将 is_deleted 字段设置为 1
     *
     * @param request 包含 id 的请求体
     * @return 操作结果
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(@Valid @RequestBody DiaryDeleteRequest request) {
        boolean success = diaryService.deleteDiary(request.id());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(null));
        } else {
            return ResponseEntity.ok(ApiResponse.fail(404, "未找到对应的日记记录"));
        }
    }

    /**
     * 恢复已删除的手记
     * 将 is_deleted 字段设置为 0
     *
     * @param request 包含 id 的请求体
     * @return 操作结果
     */
    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<Void>> restoreDiary(@Valid @RequestBody DiaryDeleteRequest request) {
        boolean success = diaryService.restoreDiary(request.id());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(null));
        } else {
            return ResponseEntity.ok(ApiResponse.fail(404, "未找到对应的日记记录"));
        }
    }

    /**
     * 获取回收站列表（已删除的记录）
     *
     * @param deviceId 设备 ID
     * @param page     页码（从 1 开始），默认 1
     * @param pageSize 每页条数，默认 20
     * @return 手记列表响应（含分页信息）
     */
    @GetMapping("/trash")
    public ResponseEntity<ApiResponse<DiaryListResponse>> getTrashList(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        DiaryListResponse response = diaryService.getTrashList(deviceId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
