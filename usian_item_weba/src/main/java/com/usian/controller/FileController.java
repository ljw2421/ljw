package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("file")
public class FileController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @RequestMapping("upload")
    public Result upload(MultipartFile file){
        if (file != null && file.getSize() >0){
            String filename = file.getOriginalFilename();
            filename = filename.substring(filename.lastIndexOf("."));
            filename = UUID.randomUUID() + filename;
            File f = new File("F:\\xm\\image\\" + filename);
            try {
                file.transferTo(f);
                return Result.ok("http://image.usian.com/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
                return Result.error("图片上传失败");
            }
        }
        return Result.error("图片上传失败");
    }

}
