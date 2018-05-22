package practicaltest02.eim.systems.cs.pub.ro.practicaltest02.Network;

/**
 * Created by student on 22.05.2018.
 */
import android.provider.DocumentsContract;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import practicaltest02.eim.systems.cs.pub.ro.practicaltest02.Utilities.Constants;
import practicaltest02.eim.systems.cs.pub.ro.practicaltest02.Utilities.Utilities;

public class ComunicationThread extends Thread{
    private ServerThread serverThread;
    private Socket socket;

    public ComunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String city = bufferedReader.readLine();
            System.out.println("[------------------]"+city);

            String data = serverThread.getData();

            String weatherForecastInformation = null;

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
            HttpClient httpClient = new DefaultHttpClient();

            String str = "https://autocomplete.wunderground.com/aq?query=" + city;
            HttpGet httpGet = new HttpGet(str);
                //UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                //httpPost.setEntity(urlEncodedFormEntity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpGet, responseHandler);
                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }
                String dataNew = null;
                StringBuffer strBuffer = new StringBuffer();
                //System.out.println(pageSourceCode);
                String[] token = pageSourceCode.split("name");
                for (int i=1; i< token.length; i++){
                    String[] tok = token[i].split(",");
                    //System.out.println("-----"+tok[0].replace("\"",""));
                    strBuffer.append(tok[0].replace("\"",""));
                    strBuffer.append("\n");
                    dataNew = dataNew + "\n"+ tok[0].replace("\"","");
                }

                System.out.println("=+++++++++ "+dataNew);

            String result = dataNew;
            //String result = strBuffer.toString();

            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (Exception exception) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + exception.getMessage());
            if (Constants.DEBUG) {
                exception.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
