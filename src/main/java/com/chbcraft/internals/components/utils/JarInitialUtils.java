package com.chbcraft.internals.components.utils;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JarInitialUtils {
    private JarInitialUtils(){}
    /**
     * 从Jar中读取文件写入磁盘中
     * @param jarFile 要读取的文件
     * @param fromFile jar包中的文件，jar包的绝对地址
     * @param toFile 要写出的位置
     */
    public static void readFileFromJarToCd(File jarFile,String fromFile,File toFile){
        try(JarFile original = new JarFile(jarFile)) {
            fromFile = fromFile.startsWith("/")?fromFile.substring(1):fromFile;
            fromFile = fromFile.endsWith("/")?fromFile.substring(0,fromFile.length()-1):fromFile;
            JarEntry entry = original.getJarEntry(fromFile);
            if(entry!=null)
                readFileFromJarToCd(original,entry,toFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void readFileFromJarToCd(JarFile jarFile,JarEntry fromEntry,File toFile){
        File toFileNow = transformNew(toFile,fromEntry.getName());
        try(BufferedInputStream inputStream = new BufferedInputStream(jarFile.getInputStream(fromEntry))) {
            boolean flag = true;
            if(!toFileNow.exists()){
                if(toFileNow.getParentFile().exists()||toFileNow.getParentFile().mkdirs()){
                    flag = toFileNow.createNewFile();
                }
            }
            if(flag){
                byte[] buff = new byte[1024];
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                int count;
                while((count=inputStream.read(buff))!=-1)
                    result.write(buff,0,count);
                buff = result.toByteArray();
                try(BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(toFileNow))){
                    output.write(buff);
                    output.flush();
                }
                finally {
                    result.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 从Jar中读取一个文件夹的文件写到磁盘中
     * @param jarFile 目标JarFile
     * @param fromFile 要写的文件夹，jar包的绝对地址
     * @param toFile 要写出的文件夹
     * @param isReturn 是否要递归复制所有的文件夹
     */
    public static void readDirectoryFromJarToCd(File jarFile,String fromFile,File toFile,boolean isReturn){
        if(jarFile.getName().endsWith(".jar")){
            try(JarFile original = new JarFile(jarFile)) {
                readDirectoryFromJarToCd(original,fromFile,toFile,isReturn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归复制jar中的文件夹中的所有内容
     * @param original 源Jar文件
     * @param fromFile 从jar包的什么文件夹开始复制
     * @param toFile 复制到磁盘中的什么位置
     * @param isReturn 是否要递归复制所有的文件夹
     */
    private static void readDirectoryFromJarToCd(JarFile original,String fromFile,File toFile,boolean isReturn){
        String reg = fromFile.replace("\\","/");
        if(!reg.endsWith("/"))
            reg = reg.concat("/");
        reg = reg.startsWith("/")?reg.substring(1):reg;
        String finalReg = reg;
        String regx1 = "^("+finalReg+")"+"[^/]*/$",regx2 = "^("+finalReg+")"+"[^/]+";
        List<JarEntry> targetEntries = original.stream().filter(jarEntry -> (
                (Pattern.matches(regx1,jarEntry.getName())||Pattern.matches(regx2,jarEntry.getName()))&&!jarEntry.getName().equals(finalReg))
        ).collect(Collectors.toList());
        Iterator<JarEntry> iterator = targetEntries.iterator();
        for(JarEntry entry : targetEntries) {
            if(!entry.isDirectory())
                readFileFromJarToCd(original,entry,toFile);
            else{
                File toFileNew = transformNew(toFile,entry.getName());
                if(isReturn)
                    readDirectoryFromJarToCd(original,entry.getName(),toFileNew,true);
                if(!toFileNew.exists())
                    toFileNew.mkdirs();
            }
        }
    }

    /**
     * 将entry的绝对路径和目标文件的位置重新组合出要存储的磁盘的绝对位置
     * @param one 磁盘的目标位置
     * @param name entry在jar中的绝对路径
     * @return 返回组合出的磁盘中的绝对路径
     */
    private static File transformNew(File one,String name){
        int index;
        if(name.endsWith("/")){
            index = name.length()-1;
            int count = 0;
            while(count<2){
                if(name.charAt(index--)=='/')
                    count++;
            }
            index++;
        }else
            index = name.lastIndexOf("/");
        name = name.substring(index+1).replace("/","");
        return new File(one,name);
    }
}
