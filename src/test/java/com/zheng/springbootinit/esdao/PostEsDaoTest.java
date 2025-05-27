package com.zheng.springbootinit.esdao;

import com.zheng.springbootinit.model.dto.post.PostEsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;

@SpringBootTest
public class PostEsDaoTest {
    @Resource
    private PostEsDao postEsDao;

    @Test
    void testAdd(){
        PostEsDTO postEsDTO = new PostEsDTO();
        postEsDTO.setId(1L);
        postEsDTO.setTitle("鱼皮是狗");
        postEsDTO.setContent("鱼皮的知识星球");
        postEsDTO.setTags(Arrays.asList("Java", "Python"));
        postEsDTO.setUserId(1L);
        postEsDTO.setCreateTime(new Date());
        postEsDTO.setUpdateTime(new Date());
        postEsDTO.setIsDelete(0);
        postEsDao.save(postEsDTO);

    }
}
