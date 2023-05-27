package com.chbcraft.net.util;

import com.chbcraft.internals.components.MessageBox;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class ImageUtil {
    private ImageUtil(){}

    public static final int MB = 1024*1024;

    public static final int KB = 1024;

    public static void saveFileToCache(File tar, ByteArrayOutputStream out){
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(tar);
            out.writeTo(fo);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fo!=null){
                    fo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩图片
     * @param res 图片源文件
     * @return 返回图片
     * @throws IOException 异常
     */
    public static ByteArrayOutputStream compressPic(File res) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        BasicFileAttributes attrs = Files.readAttributes(res.toPath(),BasicFileAttributes.class);
        BufferedImage bufImg;
        long size = attrs.size();
        if(size<20*KB){
            bufImg = Thumbnails.of(res).scale(0.5f).outputQuality(1f).outputFormat("jpg").asBufferedImage();
        }else if(size<100*KB){
            bufImg = Thumbnails.of(res).scale(0.2f).outputQuality(0.2f).outputFormat("jpg").asBufferedImage();
        }else if(size<500*KB){
            bufImg = Thumbnails.of(res).scale(0.2f).outputQuality(0.1f).outputFormat("jpg").asBufferedImage();
        }else if(size<MB){
            bufImg = Thumbnails.of(res).scale(0.05f).outputQuality(0.1f).outputFormat("jpg").asBufferedImage();
        }else if(size<10*MB){
            bufImg = Thumbnails.of(res).scale(0.04f).outputQuality(0.05f).outputFormat("jpg").asBufferedImage();
        }else{
            bufImg = Thumbnails.of(res).scale(0.01f).outputQuality(0.05f).outputFormat("jpg").asBufferedImage();
        }
        ImageIO.write(bufImg, res.getName().substring(res.getName().lastIndexOf(".") + 1), bs);
        return bs;
    }
}
