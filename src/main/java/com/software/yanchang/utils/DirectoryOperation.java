package com.software.yanchang.utils;

import java.io.File;

/**
 * 文件类的操作
 */
public class DirectoryOperation {
    /**
     * 删除文件夹以及子文件
     * @param srcDirectory
     */
    public static void deleteDirectoryOperation(File srcDirectory) {
        if(srcDirectory.isDirectory()) {
            File[] files = srcDirectory.listFiles();
//            递归删除目录下的子目录下的文件
            for(int i = 0; i < files.length; i++) {
                deleteDirectoryOperation(files[i]);
            }
        }
        srcDirectory.delete();
    }

    public static void deleteAllFileOperation(File srcDirectory) {
        if(srcDirectory.isDirectory()) {
            File[] files = srcDirectory.listFiles();
//            递归删除目录下的子目录下的文件
            for(int i = 0; i < files.length; i++) {
                deleteDirectoryOperation(files[i]);
            }
        }
    }


    /**
     * 删除文件
     * @param file
     */
    public static void deleteFileOperation(File file) {
        if(file.exists() && file.isFile()) {
            file.delete();
        }
    }
}
