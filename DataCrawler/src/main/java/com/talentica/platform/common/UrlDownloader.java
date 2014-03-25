package com.talentica.platform.common;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by AmitG on 21-03-2014.
 */
public class UrlDownloader {
    final int size = 1024;

    public void fileUrl(String fAddress, String localFileName, String destinationDir) {
        OutputStream outputStream = null;
        URLConnection urlConnection = null;

        InputStream is = null;
        try {
            URL Url;
            byte[] buf;
            int ByteRead, ByteWritten = 0;
            Url = new URL(fAddress);
            outputStream = new BufferedOutputStream(new FileOutputStream(destinationDir + "\\" + localFileName));

            urlConnection = Url.openConnection();
            is = urlConnection.getInputStream();
            buf = new byte[size];
            while ((ByteRead = is.read(buf)) != -1) {
                outputStream.write(buf, 0, ByteRead);
                ByteWritten += ByteRead;
            }
            System.out.println("Downloaded Successfully.");
            System.out.println("File name:\"" + localFileName + "\"\nNo ofbytes :" + ByteWritten);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
                if(outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void fileDownload(String fAddress, String destinationDir) {
        int slashIndex = fAddress.lastIndexOf('/');
        int periodIndex = fAddress.lastIndexOf('.');

        String fileName = fAddress.substring(slashIndex + 1);

        if (periodIndex >= 1 && slashIndex >= 0
                && slashIndex < fAddress.length() - 1) {
            fileUrl(fAddress, fileName, destinationDir);
        } else {
            System.err.println("path or file name.");
        }
    }
}

