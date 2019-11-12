
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gil
 */
public class Protocol /*what the server should do*/{
    private static final int WAITING = 0;
    private static final int UPLOADING = 1;
    private static final int DOWNLOADING = 2;
    private static final int RESET = 60;
    private static final int AWAIT_RESET = 61;
    private static final int CLOSING = 70;
    private int state=WAITING;
    private Map<Integer, int[]> sessions = new HashMap<>(); //each SID contains an array with: ctr, key
    private String privKey="rytu7nybtg";
    public String pubKey="dfgerhtgr";
    public String servDec(String str) {
        return str;
    }
    public boolean acceptUInput() {
        return state==WAITING;
    }
    public int generateSID()/*make it 32 bit*/ {
        Random r=new Random();
        int sid=5+r.nextInt(10);
        while (sessions.containsKey(sid))
            sid=5+r.nextInt(10);
        return sid;
    }
    public int generateCTR()/*make it 64 bit*/ {
        Random r=new Random();
        return 500+r.nextInt(150);
    }
    public String enc(String str, int key, int ctr) {
        return str;
    }
    public String dec(String str, int key, int ctr) {
        return str;
    }
    public String processInput(String input) {
        String output="";
        if (state==WAITING) {
            String[] msg = input.split("\\s+");
            String cid, fileName;
            int key, sid, ctr;
            switch(msg[0]) {
                case "REQKEY":
                    output="KEY "+pubKey+" END";
                    break;
                case "INIT":
                    //INIT SessID timeout 32byte K encrypted with server key
                    key=Integer.parseInt(servDec(msg[1]));
                    sid=generateSID();
                    ctr=generateCTR();
                    int[] arr={ctr, key};
                    sessions.put(sid, arr);
                    output="ENCR "+ctr+" "+ctr+" "+enc("CONF", key, ctr);
                    break;
                case "UPLD":
                    fileName=msg[1];
                    cid = msg[2];
                    break;
                case "DATA":
                    cid=msg[1];
                    ctr=sessions.get(cid)[0];
                    key=sessions.get(cid)[1];
                    dec(msg[2], key, ctr);
                    //msg[2] is what was sent...so process it!
                case "DWNL":
                    //find file in map by name, encrypt and send packets
                    break;
                case "RRST":
                    //RRST SessID CTR timeout
                    break;
                case "SYNC":
                    //SYNC SessID CTR max(newCTR, CTR)
                    break;
                case "FCLS":
                    //FCLS reasontoclose
                    //close and report reason
                    break;
                default:
                    break;
            }
        }
        else if (state==UPLOADING) {
            
        }
        return output;
    }
}
