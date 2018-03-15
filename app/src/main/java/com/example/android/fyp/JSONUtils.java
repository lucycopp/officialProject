package com.example.android.fyp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by Lucy on 28/10/2017.
 */

public class JSONUtils {
    public static final String LOG_TAG = JSONUtils.class.getSimpleName();

    //This method creates a new URL object from a given string
    public static URL makeURL(String iUrl){
        URL url = null;
        try {
            url = new URL(iUrl); //try to create the URL
        }
        catch (Exception e){
            Log.e(LOG_TAG, "Creating URL " + e); //Log the exception
        }
        return url;
    }

    public static String makeHTTPRequest(URL url) throws IOException
    {
        String response = "";
        if (url == null){  //if not a valid url
            return response;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection(); //try to open connection
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET"); //get request


            urlConnection.connect(); //make connection

            if (urlConnection.getResponseCode() == 200) { //successful connection
                inputStream = urlConnection.getInputStream();
                response = readFromStream(inputStream); //read the response
            } else {
                Log.e(LOG_TAG, "RESPONSE CODE " + urlConnection.getResponseCode());
            }
        }
            catch (IOException e){
                Log.e(LOG_TAG, e.toString()); //Log the exception
            }
            finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            } //disconnect connection
            if (inputStream != null) {
                inputStream.close();
            } //close the input stream
        }
        return response;
        }

        //Method to read from the input stream and create string
    public static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}


