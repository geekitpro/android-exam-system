package com.dream.ivpc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.util.Base64;

public class Base64Util {

	/**
     * <p>���ļ�ת��base64 �ַ���</p>
     * @param path �ļ�·��
     * @return
     * @throws Exception
     */
    public static String encodeBase64File(String path) throws Exception {
        File  file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return android.util.Base64.encodeToString(buffer, Base64.DEFAULT);
    }
    /**
     * <p>��base64�ַ����뱣���ļ�</p>
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void decoderBase64File(String base64Code,String targetPath) throws Exception {
        byte [] baseByte = android.util.Base64.decode(base64Code, Base64.DEFAULT);
        
//        byte[] buffer = new BASE64Decoder().decodeBuffer(base64Code);
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(baseByte);
        out.close();
    }
    /**
     * <p>��base64�ַ������ı��ļ�</p>
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void toFile(String base64Code,String targetPath) throws Exception {
        byte[] buffer = base64Code.getBytes();
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }

}
