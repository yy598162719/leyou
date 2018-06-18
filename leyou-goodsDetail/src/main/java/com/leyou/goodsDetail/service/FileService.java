package com.leyou.goodsDetail.service;

import com.leyou.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.persistence.Id;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * @author Qin PengCheng
 * @date 2018/6/10
 */
@Service
public class FileService {

    @Autowired
    private GoodsDetailService goodsDetailService;

    @Autowired
    private TemplateEngine templateEngine;

    private Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${ly.thymeleaf.destPath}")
    private String destPath ;

    public void createHtml(Long id) {
        //创建一个上下文对象
        Context context = new Context();
        //将数据塞入上下文
        Map<String, Object> goodsDetails = this.goodsDetailService.getGoodsDetails(id);
        context.setVariables(goodsDetails);
        //准备一个输出流对象,关联一个临时文件
        File temp = new File(id + ".html");
        //创建一个目标文件
        File dest = this.createPath(id);
        //创建一个临时文件的文件夹
        File bak = new File(id + "_bak.html");
        try( PrintWriter printWriter = new PrintWriter(temp, "utf-8")) {
            templateEngine.process("item", context, printWriter);
            //如果目标文件存在，则先把目标文件转移i到临时文件，然后尝试覆盖
            if (dest.exists()) {
                dest.renameTo(bak);
            }
            //用新文件将就文件覆盖
            FileCopyUtils.copy(temp,dest);
            //删除成功将备份删除
            bak.delete();
        } catch (Exception e) {
            //发生异常，还原旧文件
            e.printStackTrace();
              logger.error("创建静态页面失败");
            bak.renameTo(dest);
            throw new RuntimeException(e);
        } finally {
            if (temp.exists()) {
                temp.delete();
            }
        }
    }

    /**
     * 根据id创建一个文件
     *
     * @param id
     * @return
     */
    public File createPath(Long id) {
        if (id == null) {
            return null;
        }
        //创建路径
        File dest = new File(this.destPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        return new File(dest, id + ".html");
    }

    /**
     * 异步创建页面
     *
     * @param id
     */
    public void asynCreateHtml(Long id) {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    createHtml(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 删除的方法
     * @param id
     */
    public void deleteHtml(Long id) {
        File file = new File(destPath + id + ".html");
        file.deleteOnExit();
    }
}
