/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package authtest;

import com.eclipsesource.json.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import nfcjlib.core.util.Dump;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author sampoz
 */
public class Main {

    static String masterKey;
    static byte[] aid = new byte[]{0x00, 0x01, 0x01};
    static RestApi rest;
    static String baseURL ="http://127.0.0.1:8000/CardUsers/";
    static String detailURL = "http://127.0.0.1:8000/CardUsers/detail/";

    /**
     * Opens the main UI and reads the master key from file
     * @param args
     */
    public static void main(String[] args) {
        rest = new RestApi();
        masterKey = getPassword() + readFileKey();
        System.out.println(new String(masterKey));
        mainUI ui = new mainUI();
        ui.setVisible(true);
    }
    /**
     * Opens a dialog that queries password
     * @return 
     */

    private static String getPassword() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a password:");
        JPasswordField pass = new JPasswordField(10);
        panel.add(label);
        panel.add(pass);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "Give master password",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
        if (option == 0) // pressing OK button
        {
            char[] password = pass.getPassword();
            return new String(password);
        } else {
            return null;
        }
    }

    /**
     * Creates a new card, begins with format and then adds right app with right key.
     */
    public static void createNewCard() {
        try {
            KiltaAuth.formatCard();
            BASE64Encoder encoder = new BASE64Encoder();
            String name = getName();
            System.out.println(name);
            System.out.println(aid.toString());
            String uid = KiltaAuth.getUID();
            String DifferentiatedKey = aid[0] + aid[1] + aid[2] + uid + masterKey;
            System.out.println(DifferentiatedKey);
            byte[] diffAES = hashForAES(DifferentiatedKey);
            byte[] cryptedDiffKey = crypt(masterKey, diffAES);
            System.out.println("Crypted key was "+Dump.hex(cryptedDiffKey)+" with length "+cryptedDiffKey.length);
            String encodedBytes = encoder.encodeBuffer(cryptedDiffKey);
            System.out.println("Encoded string key was "+encodedBytes+" with length "+encodedBytes.length());
            String JSON = "{\"name\": \"" + name + "\" , \"cardId\":\"" + uid.substring(0, uid.length() - 1) + "\", \"appKey\":\"" + encodedBytes.substring(0, encodedBytes.length() - 1) + "\"}";
            System.out.println(JSON);
            String returnedJson;
            try {
                returnedJson = rest.sendPost(baseURL,JSON);
            } catch (Exception e) {
                System.out.println(e);
                return;
            }
            System.out.println(parseJSON(returnedJson, "id"));
            byte[] userID = padToLength(parseJSON(returnedJson, "id"));
            System.out.println("Userid is padded to byte array: "+ Dump.hex(userID , true));
            KiltaAuth.addApp(aid);
            System.out.println("Changing key to " + Dump.hex(diffAES) + " with length "+diffAES.length);
            KiltaAuth.changeKey(aid, diffAES);
            KiltaAuth.addUserIdToFileInApp(aid, userID, (byte) 0x01, diffAES);
        } catch (Exception e){
            System.err.println(e);
            rest.sendDelete(detailURL,KiltaAuth.getUID());
            System.out.println("Rolled back the changes");
               
        }
    }

    /**
     * Reads the card data and then authenticates it.
     * @return
     */
    public static String readCard() {
        String[] response = rest.sendGet(detailURL,KiltaAuth.getUID().toString());
        if (response[1].equals("200")) { // if get succeeds
            String queriedAppKey = parseJSON(response[0], "appKey");
            String queriedUserID = parseJSON(response[0], "id");
            String queriedUserName = parseJSON(response[0], "name");
            System.out.println("appkey was " + queriedAppKey);
            return authenticateCard(queriedAppKey, queriedUserID)+ " with name "+queriedUserName ;
        } else if (response[1].equals("404")) {
            System.out.println("Not found");
            Main.createNewCard();
            return "Created a new Card";
        }
        return "error";
    }
    /**
     * Authenticates the card by reading userID from it, compares it to given userId and returns authentication status as a String
     * @param appKey
     * @param userId
     * @return 
     */

    private static String authenticateCard(String appKey, String userId) {
        try{
        BASE64Decoder decoder = new BASE64Decoder();
        byte [] decodedKey=decoder.decodeBuffer(appKey.substring(1, appKey.length()-1));
        System.out.println("authentication card for reading a file with crypted and base64 endcoded key "+appKey+" with length of "+appKey.length());
        System.out.println("Opened base64 coding, key is now "+Dump.hex(decodedKey)+" and length is "+ 
            decodedKey.length);
        byte[] openedAppKey = decrypt(masterKey, decodedKey);
        System.out.println("Opened key encryption, opened key is "+ Dump.hex(openedAppKey));
        byte[] userIDinCard = KiltaAuth.readFileFromApp(aid,(byte)0x01,openedAppKey);  
            System.out.println("uncut data from card "+Dump.hex(userIDinCard));
            System.out.println("Read from card "+ Dump.hex(cutToLength(userIDinCard,4)));
            System.out.println("real user id was "+ Dump.hex(padToLength(userId)));
            if(testIfArraysMatch(cutToLength(userIDinCard, 4),padToLength(userId))){
            return "Authenticated user with id "+userId ;
        }
        } catch (Exception e){
            System.out.println(e);
        }
        return "can't authenticate user";
    }
    /**
     * Parses given key from a string of json
     * @param json
     * @param key
     * @return 
     */
    private static String parseJSON(String json, String key) {
        String appID = "";
        try {
            System.out.println(json);
            StringReader reader = new StringReader(json);
            JsonObject jsonObject = JsonObject.readFrom(reader);
            appID = jsonObject.get(key).toString();
            System.out.println(appID);
        } catch (Exception e) {
            System.out.println(e);
        }
        return appID;
    }
    /**
     * Opens a dialog and asks for username
     * @return 
     */

    private static String getName() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a Name for the owner of the new card:");
        JTextField name = new JTextField(13);
        panel.add(label);
        panel.add(name);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "Give name",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
        if (option == 0) // pressing OK button
        {
            return name.getText();
        } else {
            return "";
        }
    }
    /**
     * Hashes given key and returns first 16 bytes
     * @param diffKey
     * @return 
     */

    private static byte[] hashForAES(String diffKey) {
        byte[] hashedDiffKey = hash(diffKey);
        byte[] ret = new byte[16];
        for (int i = 0; i < 15; i++) {
            ret[i] = hashedDiffKey[i];
        }
        return ret;

    }
    /**
     * Reads the keyfile and returns the key in String
     * @return
     */
    public static String readFileKey() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("masterKey.txt"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            String ret = sb.toString();
            br.close();
            return ret;
        } catch (Exception e) {
            System.out.println("Could not read a file" + e);
        }
        return null;
    }
    /**
     * Decrypts given data with given password
     * @param password
     * @param cryptedData
     * @return 
     */
    private static byte[] decrypt(String password, byte[] cryptedData) {
        try {
            byte[] key = hash(password);
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] decryptedData = c.doFinal(cryptedData);
            return decryptedData;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
/**
 * Crypts given data with given password
 * @param password
 * @param data
 * @return 
 */
    private static byte[] crypt(String password, byte[] data) {
        try {
            byte[] key = hash(password);
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.ENCRYPT_MODE, k);
            byte[] encryptedData = c.doFinal(data);
            return encryptedData;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
    /**
     * Hashesh given string with sha-256
     * @param hashThis
     * @return 
     */
    private static byte[] hash(String hashThis) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(hashThis.getBytes("UTF-8"));
            return md.digest();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }
    /**
     * Pads the given (integer)String to 4 bytes
     * @param parseJSON
     * @return 
     */
    private static byte[] padToLength(String intToPad) {
      int offset=0;
      int value = Integer.parseInt(intToPad);
      byte[] data = new byte[4];
      data[offset] = (byte)((value >>> 24) & 0xFF);
      data[offset + 1] = (byte)((value >>> 16) & 0xFF);
      data[offset + 2] = (byte)((value >>> 8) & 0xFF);
      data[offset + 3] = (byte)((value >>> 0) & 0xFF);
      return data;

    }
    /**
     * Cuts given array to length i, returns the first 4 elements in array
     * @param userIDinCard
     * @param i
     * @return 
     */
    private static byte[] cutToLength(byte[] userIDinCard, int i) {
        byte[] ret= new byte[4];
        for (int j = 0; j < i; j++) {
            ret[j]=userIDinCard[j];        
        }
        return ret;
    }
    /** 
     * Compares two arrays, return true if every byte matches
     * @param arr1
     * @param arr2
     * @return 
     */
    private static boolean testIfArraysMatch(byte[] arr1, byte[] arr2) {
        if(arr1.length != arr2.length){ return false; }
        for (int i = 0; i < arr1.length; i++) {
            if( arr1[i] != arr2[i]){
                return false;
            }
        }
        return true;
    }
}
