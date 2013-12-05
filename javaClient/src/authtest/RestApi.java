/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package authtest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author sampoz
 */
public class RestApi {

    private final String USER_AGENT = "Mozilla/5.0";

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        RestApi http = new RestApi();
    }
    /**
     * Used to get account with card UID slug
     * @param baseURL 
     * @param slug
     * @return
     */
    public String[] sendGet(String baseURL,String slug) {
        try {
            String url = baseURL+slug;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            
            if(responseCode==404){
                return new String[] {"", Integer.toString(responseCode)}; 
            }
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new String[] {response.toString(),Integer.toString(responseCode)};
        } catch (Exception e) {
            System.out.println("Error happened " + e.toString());
        }
        return new String[]{};
    }
    /**
     * Used to delete account with id UID
     * @param detailURL 
     * @param UID
     * @return
     */
    public String[] sendDelete(String detailURL,String UID) {
        try {
            String url = detailURL+UID;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("DELETE");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            
            if(responseCode==404){
                return new String[] {"", Integer.toString(responseCode)}; 
            }
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new String[] {response.toString(),Integer.toString(responseCode)};
        } catch (Exception e) {
            System.out.println("Error happened " + e.toString());
        }
        return new String[]{};
    }

    // HTTP POST request
    /**
     * Used to send REST api call to certain url. In this scope this is used to create new accounts
     * @param baseURL 
     * @param body
     * @return
     * @throws Exception
     */
    public String sendPost(String baseURL ,String body) throws Exception {

        String url = baseURL;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + body);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        return response.toString();

    }
}
