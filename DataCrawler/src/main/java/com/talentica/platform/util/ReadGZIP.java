package com.talentica.platform.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Created by AmitG on 21-03-2014.
 */
public class ReadGZIP {

    /**
     * This method is used for unzipping files with .filetype.gz (eg. .xml.gz)extensions
     *
     * @param strSourceFile  - full path to source file
     * @param destinationDir - full path of destination dir
     * @throws IOException
     */
    public static String unzipGZ(String strSourceFile, String destinationDir) throws IOException {
        File inputFile = new File(strSourceFile);
        FileInputStream fileInputStream = new FileInputStream(inputFile);
        System.out.println(inputFile.getName());
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        String inputFileName = inputFile.getName();

        String destinationFilePath = destinationDir + File.separatorChar + inputFileName.substring(0,inputFileName.lastIndexOf("."));
        File fout = new File(destinationFilePath);
        FileOutputStream fileOutputStream = new FileOutputStream(fout);
        for (int c = gzipInputStream.read(); c != -1; c = gzipInputStream.read()) {
            fileOutputStream.write(c);
        }
        fileOutputStream.close();
        return fout.getPath();
    }
}
