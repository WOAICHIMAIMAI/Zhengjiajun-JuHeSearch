package com.zheng.springbootinit.controller;

import com.zheng.springbootinit.common.BaseResponse;
import com.zheng.springbootinit.common.ResultUtils;
import com.zheng.springbootinit.manager.SearchFacade;
import com.zheng.springbootinit.model.dto.search.SearchRequest;
import com.zheng.springbootinit.model.vo.SearchVO;
import com.zheng.springbootinit.service.PictureService;
import com.zheng.springbootinit.service.PostService;
import com.zheng.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private PostService postService;

    @Resource
    private SearchFacade searchFacade;

    @PostMapping("/all")
    public BaseResponse<SearchVO>searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request){
        return ResultUtils.success(searchFacade.searchAll(searchRequest, request));
    }
}
