package com.leyou.search.feign;

import com.leyou.search.LeYouSearchApplication;
import com.leyou.search.client.CategoryClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/5
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeYouSearchApplication.class)
public class CategoryClientTest {


    @Autowired
    private CategoryClient categoryClient;

    @Test
    public void testQueryCategories() {
        ArrayList<Long> arryayList = new ArrayList<>();
        arryayList.add(1L);
        arryayList.add(2L);
        arryayList.add(3L);
        ResponseEntity<List<String>> resp = this.categoryClient.queryCategoryNamesBycids(arryayList);
        System.out.println("....................................................................................................................");
        System.out.println(resp);
        System.out.println(resp.getBody());
    }

}