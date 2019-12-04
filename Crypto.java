package last;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 *
 * @author gil
 */
public class Crypto {
    public static int blockLen=4096;
    public static String algorithm="SHA-256";
    public static byte[] addOne(byte[] b) {
        if (b.length==0)//empty string
            return addOne("\u0000".getBytes());
        int ind=b.length-1;
        do {
            b[ind]+=1;
            ind-=1;
        }while (ind>=0 && b[ind]==0);
        return b;
    }
    public static byte[] randByteStr(int lenBytes) {
        byte[] b= new byte[lenBytes];
        Random r=new Random();
        r.nextBytes(b);
        return b;
    }
    /*public static byte[] intConcat(String k, int c)
    {
        String cat=k+c;
        String concat="";
        for (int i=0;i<blockLen-cat.length();i++)//pad with the proper amount of zeros beforehand
        {
            concat+="0";
        }
        //Perhaps, attempt a better solution, for example StringBuilder
        return (concat+cat).getBytes();
    }
    public static byte[] getDigest(String key, int ctr) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] buffer = intConcat(key, ctr);
        md.update(buffer);
        byte[] digest=md.digest();
        return digest;
    }*/
    public static byte[] concatByteArrays(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        baos.write(a);
        baos.write(b);
        return baos.toByteArray();
    }
    public static byte[] getDigest(byte[] key, byte[] ctr) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] buffer = concatByteArrays(key, ctr);
        md.update(buffer);
        byte[] digest=md.digest();
        return digest;
    }
    /*public static byte[] cryptoBlock(byte[] b, int block, String key, int ctr) throws NoSuchAlgorithmException{
        //parallelize
        byte[] digest=getDigest(key, ctr);
        for (int i=0;i<digest.length;i++) {
            digest[i]=(byte)(b[blockLen*block+i] ^ digest[i]);
        }
        return digest;
    }*/
    public static byte[] crypto(byte[] key, byte[] ctr, byte[] b) throws NoSuchAlgorithmException, IOException {
        byte[] digest=getDigest(key, ctr);
        int length=b.length;//blockLen perhaps
        System.out.println(b.length+" is the bytes length.");
        for (int i=0;i<b.length;i++) {
            if (i%digest.length==0)
                digest=getDigest(key, ctr);
            b[i]=(byte)(b[i] ^ digest[i%digest.length]);
            addOne(ctr);
        }
        return b;
    }
    /*public static byte[] crypto(byte[] b, String key, int ctr) throws NoSuchAlgorithmException {
        //discard unused bytes!
        for (int i=0;i<b.length/blockLen+1;i++)
        {
            cryptoBlock(b, i, key, ctr);
            updateCounter(ctr);
        }
        return b;
    }*/
    /*public static byte[] encryptFile(File file, String key, int ctr) throws NoSuchAlgorithmException, IOException {
        return crypto(Files.readAllBytes(file.toPath()), key, ctr);
    }
    public static File decryptFile(byte[] cipher, String path, String filename, String key, int ctr) throws NoSuchAlgorithmException {
        return bytes2File(crypto(cipher, key, ctr), path, filename);
    }
    public static File bytes2File(byte[] plain, String path, String filename) {
        File file=new File(path+filename);
        
        //NOT IMPLEMENTED
        
        return file;
    }*/
    public static byte[] encdecFile(byte[] key, byte[] ctr, String path) throws FileNotFoundException, IOException, NoSuchAlgorithmException
    {
        File f=new File(path);
        FileInputStream fis;
        byte[] b;
        if (f.exists())
        {
            fis=new FileInputStream(f);
            b=new byte[(int)f.length()];
            fis.read(b);
            fis.close();
            return crypto(key, ctr, b);
        }
        return new byte[0];
    }
}
