package com.example.controller;

import com.example.config.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RequestMapping("/common")
@RestController
public class CommonController {

    @Value("${opinion-service.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String>  upload(MultipartFile file) {
        //这里的file是一个临时文件，需要转存到指定位置
        String originalFilename = file.getOriginalFilename();
        // System.out.println(originalFilename);
        String s = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName= UUID.randomUUID().toString()+s;
        //System.out.println(fileName);
        //这里还要创建目录
        File dir=new File(basePath);

        if(!dir.exists()){
            dir.mkdirs();//没有就创建
        }
        try {
            file.transferTo(new File(basePath+fileName));
            //System.out.println(basePath+fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);//这里要返回文件名称
    }

    //文件下载，将文件图片发送到前端进行展示
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));

            ServletOutputStream outputStream=response.getOutputStream();

            //设置文件写回格式
            response.setContentType("image/jpeg");
            //然后写回页面
            int len=0;
            byte[] bytes=new byte[1024];
            while((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //写完之后关闭流
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
