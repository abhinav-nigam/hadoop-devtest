package com.talentica.platform.util;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

import java.io.*;
import java.util.zip.GZIPInputStream;


/**
 * Created by AmitG on 21-03-2014.
 */
public class Untargz {

    /**
     * This method is used for extracting files with .tar.gz extension
     *
     * @param strSourceFile  - Full path to source file
     * @param destinationDir -  Full path to destination Dir
     * @throws IOException
     */
    public void untar(String strSourceFile, String destinationDir) throws IOException {
        InputStream in = this.getInputStream(strSourceFile);
        System.out.println("Reading TarInputStream... ");
        TarInputStream tin = new TarInputStream(in);
        TarEntry tarEntry = tin.getNextEntry();
        if (new File(destinationDir).exists()) {
            while (tarEntry != null) {
                File destPath = new File(destinationDir + File.separatorChar + tarEntry.getName());
                System.out.println("Processing " + destPath.getAbsoluteFile());
                if (!tarEntry.isDirectory()) {
                    destPath.getParentFile().mkdirs();
                    FileOutputStream fout = new FileOutputStream(destPath);
                    tin.copyEntryContents(fout);
                    fout.close();
                } else {
                    destPath.mkdir();
                }
                tarEntry = tin.getNextEntry();
            }
            tin.close();
        } else {
            System.out.println("That destination directory doesn't exist! " + destinationDir);
        }
    }

    private InputStream getInputStream(String tarFileName) throws IOException {

        if (tarFileName.substring(tarFileName.lastIndexOf(".") + 1, tarFileName.lastIndexOf(".") + 3).equalsIgnoreCase("gz")) {
            System.out.println("Creating an GZIPInputStream for the file");
            return new GZIPInputStream(new FileInputStream(new File(tarFileName)));

        } else {
            System.out.println("Creating an InputStream for the file");
            return new FileInputStream(new File(tarFileName));
        }
    }
}
