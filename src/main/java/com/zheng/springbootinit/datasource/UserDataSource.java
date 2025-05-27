package com.zheng.springbootinit.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.springbootinit.model.dto.user.UserQueryRequest;
import com.zheng.springbootinit.model.vo.UserVO;
import com.zheng.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserDataSource implements DataSource<UserVO>{

    @Resource
    private UserService userService;


    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long pageSize) {
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUserName(searchText);
        userQueryRequest.setCurrent((int)pageNum);
        userQueryRequest.setPageSize((int)pageSize);
        Page<UserVO> userVOPage = userService.listUserVoByPage(userQueryRequest);
        return userVOPage;
    }
}
