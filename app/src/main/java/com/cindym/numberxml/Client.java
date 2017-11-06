package com.cindym.numberxml;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by cindymbonani on 2017/11/04.
 */

 class Client extends AsyncTask<Void, Void, Void> {

    private String requestAddress;
    private int serverPort;
    private String response = "";
    private TextView serverResponse;
    private String serResponse = null;
    private String fileName ="message.xml" ;
    private String responseFileName = "response.xml";
    private XmlPullParserFactory xmlFactoryObject;
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/client/" ;

    public String getResponse(){
        return this.response;
    }

    public void setResponse(String response){
        this.response = response;
    }

    public Client(String address, int port,TextView serverResponse) {
        requestAddress = address;
        serverPort = port;
        this.serverResponse=serverResponse;
        String data = "<request>\n" +
                "    <EventType>Authentication</EventType>\n" +
                "    <event>\n" +
                "        <UserPin>12345</UserPin>\n" +
                "        <DeviceId>12345</DeviceId>\n" +
                "        <DeviceSer>ABCDE</DeviceSer>\n" +
                "        <DeviceVer>ABCDE</DeviceVer>\n" +
                "        <TransType>Users</TransType>\n" +
                "    </event>\n" +
                "</request>";

        saveToFile(data,fileName);
    }
    @Override
    protected Void doInBackground(Void... params) {

        Socket socket = null;

        try {

            socket = new Socket(requestAddress, serverPort);

            //Wait for the server to accept connection before reading the xml file
            BufferedReader reader = new BufferedReader(new FileReader(path+fileName));

            String line;

            StringBuilder stringBuilder = new StringBuilder();

            while((line = reader.readLine())!=null){
                stringBuilder.append(line);
            }

            //Send data to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
            writer.println(stringBuilder.toString());
            writer.flush();

            BufferedReader serverReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            while ((serResponse = serverReader.readLine()) != null) {
                setResponse(serResponse);
                saveToFile(serResponse,responseFileName);
                response = serResponse;
            }

            try {
                xmlFactoryObject = XmlPullParserFactory.newInstance();

                XmlPullParser myparser = xmlFactoryObject.newPullParser();

                myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                myparser.setInput(socket.getInputStream(), null);

                parseXMLAndStoreIt(myparser);

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void readFile(String fileName){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path+fileName));
            String line;

            StringBuilder stringBuilder = new StringBuilder();

            while((line = reader.readLine())!=null){
                stringBuilder.append(line);
            }
            serverResponse.setText(stringBuilder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text=null;

        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();

                switch (event){
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;
                }
                event = myParser.next();
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        serverResponse.setText(getResponse());
        super.onPostExecute(aVoid);
    }

    private boolean saveToFile( String data,String fileName){
        try {
            new File(path  ).mkdir();
            File file = new File(path+ fileName);
            if (!file.exists()) {
                file.createNewFile();

                FileOutputStream fileOutputStream = new FileOutputStream(file,true);
                fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());
            }
            return true;
        }  catch(FileNotFoundException ex) {
            Log.d("FileNotFoundException", ex.getMessage());
        }  catch(IOException ex) {
            Log.d("IOException", ex.getMessage());
        }
        return  false;
    }

}
