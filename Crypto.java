/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author gil
 */
public class Crypto {
    private int blockLen=32;
    String algorithm="SHA-256";
    public byte[] intConcat(String k, int c)
    {
        String cat=k+c;
        String concat="";
        for (int i=0;i<blockLen-cat.length();i++)//pad with the proper amount of zeros beforehand
        {
            concat+="0";
        }
        /*Perhaps, attempt a better solution, for example StringBuilder*/
        return (concat+cat).getBytes();
    }
    public byte[] getDigest(String key, int ctr) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] buffer = intConcat(key, ctr);
        md.update(buffer);
        byte[] digest=md.digest();
        return digest;
    }
    public void updateCounter(int ctr)
    {
        ctr+=1;
    }
    public byte[] cryptoBlock(byte[] b, int block, String key, int ctr) throws NoSuchAlgorithmException /*encryption/decryption of a block*/{
        //parallelize
        byte[] digest=getDigest(key, ctr);
        for (int i=0;i<digest.length;i++) {
            digest[i]=(byte)(b[blockLen*block+i] ^ digest[i]);
        }
        return digest;
    }
    public byte[] crypto(byte[] b, String key, int ctr) throws NoSuchAlgorithmException /*encryption/decryption of a byte array*/{
        //discard unused bytes!
        for (int i=0;i<b.length/blockLen+1;i++)
        {
            cryptoBlock(b, i, key, ctr);
            updateCounter(ctr);
        }
        return b;
    }
    public byte[] encryptFile(File file, String key, int ctr) throws NoSuchAlgorithmException, IOException {
        return crypto(Files.readAllBytes(file.toPath()), key, ctr);
    }
    public File decryptFile(byte[] cipher, String path, String filename, String key, int ctr) throws NoSuchAlgorithmException {
        return bytes2File(crypto(cipher, key, ctr), path, filename);
    }
    public File bytes2File(byte[] plain, String path, String filename) {
        File file=new File(path+filename);
        
        //NOT IMPLEMENTED
        
        return file;
    }
}
