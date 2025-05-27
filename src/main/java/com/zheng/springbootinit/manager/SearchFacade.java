package com.zheng.springbootinit.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.springbootinit.common.ErrorCode;
import com.zheng.springbootinit.datasource.*;
import com.zheng.springbootinit.exception.BusinessException;
import com.zheng.springbootinit.exception.ThrowUtils;
import com.zheng.springbootinit.model.dto.post.PostQueryRequest;
import com.zheng.springbootinit.model.dto.search.SearchRequest;
import com.zheng.springbootinit.model.dto.user.UserQueryRequest;
import com.zheng.springbootinit.model.entity.Picture;
import com.zheng.springbootinit.model.enums.SearchTypeEnum;
import com.zheng.springbootinit.model.vo.PostVO;
import com.zheng.springbootinit.model.vo.SearchVO;
import com.zheng.springbootinit.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class SearchFacade {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchVO searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request){
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);
        String searchText = searchRequest.getSearchText();
        long current = searchRequest.getCurrent();
        long pageSize = searchRequest.getPageSize();
        if(searchTypeEnum == null){
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                UserQueryRequest userQueryRequest = new UserQueryRequest();
                userQueryRequest.setUserName(searchText);
                Page<UserVO> userVOPage = userDataSource.doSearch(searchText, current, pageSize);
                return userVOPage;
            });

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                PostQueryRequest postQueryRequest = new PostQueryRequest();
                postQueryRequest.setSearchText(searchText);
                Page<PostVO> postVOPage = postDataSource.doSearch(searchText, current, pageSize);
                return postVOPage;
            });

            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
                Page<Picture> picturePage = pictureDataSource.doSearch(searchText, 1, 10);
                return picturePage;
            });

            CompletableFuture.allOf(userTask, postTask, pictureTask).join();

            SearchVO searchVO = new SearchVO();

            try{
                Page<UserVO> userVOPage = userTask.get();
                Page<Picture> picturePage = pictureTask.get();
                Page<PostVO> postVOPage = postTask.get();

                searchVO.setPictureList(picturePage.getRecords());
                searchVO.setUserList(userVOPage.getRecords());
                searchVO.setPostList(postVOPage.getRecords());
                return searchVO;
            }catch (Exception ex){
                log.debug("系统异常！");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,  "系统错误");
            }

        }else{
            SearchVO searchVO = new SearchVO();
            DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
            Page<?> page = dataSource.doSearch(searchText, current, pageSize);
            searchVO.setDataList(page.getRecords());
            return searchVO;

        }


    }
}
