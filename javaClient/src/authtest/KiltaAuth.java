/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package authtest;

import nfcjlib.core.DESFireEV1;
import nfcjlib.core.util.Dump;
import java.io.IOException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 
 * @author sampoz
 */
public class KiltaAuth {
    static final byte cs = (byte) 0x00;

    /**
     * Used to format card, expects the card to have default master key
     */
    public static void formatCard() {
        DESFireEV1 desfire = new DESFireEV1();
        desfire.connect();
        // select PICC-level, authenticate, format card
        desfire.selectApplication(new byte[]{0x00, 0x00, 0x00});
        desfire.authenticate(new byte[8], (byte) 0x00, DESFireEV1.KeyType.DES);
        desfire.formatPICC();
        desfire.disconnect();
    }

    private static void queryApps() {
        byte[] aid = new byte[]{0x09, 0x09, 0x09};
        byte fid = 0x01, cs = 0x0F, ar1 = 0x33, ar2 = 0x33;
        DESFireEV1 desfire = new DESFireEV1();

        desfire.connect();
        // select PICC-level, authenticate, format card
        desfire.selectApplication(new byte[]{0x00, 0x00, 0x00});
        desfire.authenticate(new byte[8], (byte) 0x00, DESFireEV1.KeyType.DES);
        desfire.getApplicationsIds();
        desfire.disconnect();
        System.out.println("QUERIED APPS");
    }

    /**
     * Used to add a new application to the card. Expects the card to have the default master key
     * @param aid Application id
     */
    public static void addApp(byte[] aid) {
        DESFireEV1 desfire = new DESFireEV1();
        desfire.connect();
        desfire.selectApplication(new byte[]{0x00, 0x00, 0x00});
        desfire.authenticate(new byte[8], (byte) 0x00, DESFireEV1.KeyType.DES);
        desfire.createApplication(aid, (byte) 0x0F, (byte) 0x85); // amks war 0x0F
        desfire.selectApplication(aid);
        desfire.disconnect();
        System.out.println("ADDED APP");
    }
/**
 * Adds file to app with application id aid, byte array file and id fileId 
 * @param aid
 * @param file
 * @param fileId 
 */
    private static void addFileToApp(byte[] aid, byte[] file, byte fileId) {
        byte ar1 = 0x33, ar2 = 0x33;
        DESFireEV1 desfire = new DESFireEV1();
        desfire.connect();
        desfire.selectApplication(aid);
        desfire.authenticate(new byte[16], (byte) 0x00, DESFireEV1.KeyType.AES);
        desfire.createStdDataFile(new byte[]{fileId, cs, ar1, ar2, 0x40, 0x00, 0x00});
        desfire.authenticate(new byte[16], (byte) 0x03, DESFireEV1.KeyType.AES);
        desfire.writeData(new byte[]{fileId, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00,
            file[0], file[1], file[2], file[3], file[4]});
        desfire.disconnect();
        System.out.println("ADDED FILE TO APP");
    }
    /**
     * Adds the userID file to app aid
     * @param aid
     * @param file
     * @param fileId 
     * @param appKey key used to authenticate 
     */
    public static void addUserIdToFileInApp(byte[] aid, byte[] file, byte fileId, byte[] appKey) {
        byte ar1 = 0x33, ar2 = 0x33;
        DESFireEV1 desfire = new DESFireEV1();
        desfire.connect();
        desfire.selectApplication(aid);
        desfire.authenticate(new byte[16], (byte) 0x00, DESFireEV1.KeyType.AES);
        desfire.createStdDataFile(new byte[]{fileId, cs, ar1, ar2, 0x40, 0x00, 0x00});
        System.out.println("Preparing to authenticate with key "+ appKey + " and keylength of "+ appKey.length);
        desfire.authenticate(appKey, (byte) 0x03, DESFireEV1.KeyType.AES);
        System.out.println("Preparing to write file "+file+" with length "+ file.length);
        desfire.writeData(new byte[]{fileId, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00,
            file[0], file[1], file[2], file[3], 0x00});
        desfire.disconnect();
        System.out.println("ADDED FILE TO APP");
 
    }

    /**
     * Reads file from app with appId aid
     * @param aid
     * @param fileId
     * @param key
     * @return returns the file as byte array
     */
    public static byte[] readFileFromApp(byte[] aid, byte fileId, byte[] key) {
        byte fid = 0x01, ar1 = 0x33, ar2 = 0x33;
        DESFireEV1 desfire = new DESFireEV1();
        desfire.connect();

        desfire.selectApplication(aid);
        desfire.authenticate(key, (byte) 0x03, DESFireEV1.KeyType.AES);
        byte[] ret = desfire.readData(new byte[]{
            fileId, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        });

        System.out.println("READ FILE FROM APP");
        if (ret != null) {
            System.out.println(Dump.hex(ret));
        }
        desfire.disconnect();
        return ret;
        
    }

    /**
     * Changes the key used to authenticate app with id aid, expects the card to have default master key 
     * @param aid
     * @param newKey
     */
    public static void changeKey(byte[] aid, byte[] newKey) {
        byte fid = 0x01, ar1 = 0x33, ar2 = 0x33;
        DESFireEV1 desfire = new DESFireEV1();
        desfire.connect();
        // select PICC-level, authenticate, format card
        desfire.selectApplication(aid);
        desfire.authenticate(new byte[16], (byte) 0x00, DESFireEV1.KeyType.AES);
        desfire.changeKey((byte) 0x03, DESFireEV1.KeyType.AES, newKey, new byte[16]);
        System.out.println("CHANGED KEY");
        desfire.disconnect();
    }
 
    /**
     * Reads the UID from card
     * @return returns the uid in BASE64 encoded string
     */
    public static String getUID() {
        DESFireEV1 desfire = new DESFireEV1();
        desfire.connect();
        desfire.selectApplication(new byte[]{0x00, 0x00, 0x00});
        desfire.authenticate(new byte[8], (byte) 0x00, DESFireEV1.KeyType.DES);
        byte[] UID = desfire.getCardUID();
        desfire.disconnect();
        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        String encodedBytes="";
        try {
            encodedBytes = encoder.encodeBuffer(UID);
            System.out.println("encodedBytes " + encodedBytes);
            byte[] decodedBytes = decoder.decodeBuffer(encodedBytes);
            System.out.println("decodedBytes " + new String(decodedBytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedBytes;
    }
}
