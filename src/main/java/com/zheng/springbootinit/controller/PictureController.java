package com.zheng.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.springbootinit.common.BaseResponse;
import com.zheng.springbootinit.common.ErrorCode;
import com.zheng.springbootinit.common.ResultUtils;
import com.zheng.springbootinit.exception.ThrowUtils;
import com.zheng.springbootinit.model.dto.picture.PictureQueryRequest;
import com.zheng.springbootinit.model.entity.Picture;
import com.zheng.springbootinit.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    /**
     * 分页获取列表（封装类）
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                        HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        String searchText = pictureQueryRequest.getSearchText();
        Page<Picture> page = pictureService.searchPicture(searchText, current, size);
        return ResultUtils.success(page);
    }



}
