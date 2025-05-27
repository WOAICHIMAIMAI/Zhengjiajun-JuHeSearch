package com.zheng.springbootinit.model.vo;

import com.zheng.springbootinit.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UserVO> userList;

    private List<PostVO> postList;

    private List<Picture> pictureList;

    private List<?> dataList;
}
