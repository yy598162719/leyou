package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.upload.controller.UploadController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Qin PengCheng
 * @date 2018/5/30
 */
@Service
public class UploadService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    FastFileStorageClient storageClient;
    //设置允许上传的图片的类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg","image/gif");

    public String upload(MultipartFile file) {
        //1.验证文件
        //验证文件的类型
        String contentType = file.getContentType();
        if (!suffixes.contains(contentType)) {
            logger.info("文件上传失败，类型不支持");
            return null;
        }
        //验证文件的内容
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                logger.info("文件上传失败，不是图片");
                return null;
            }
            //2.验证成功，保存文件
            //首先生成一个文件夹
            File dir = new File("D://upload");
            if (!dir.exists()) {
                dir.mkdirs();
            }
           /* //将图片保存在此文件夹中
            file.transferTo(new File(dir, file.getOriginalFilename()));
            //3.生成url
            //测试阶段，先生成一个假的url
            String url = "http://image.leyou.com/upload/" + file.getOriginalFilename();
            return url;*/

            // 2、将图片上传到FastDFS
            // 2.1、获取文件后缀名
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            // 2.2、上传
            StorePath storePath = this.storageClient.uploadFile(
                    file.getInputStream(), file.getSize(), extension, null);
            // 2.3、返回完整路径
            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }
}
