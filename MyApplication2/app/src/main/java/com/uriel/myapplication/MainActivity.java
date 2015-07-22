package com.uriel.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class MainActivity extends Activity {


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private Button b;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

   /*    if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);

        }
*/
      /* if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
      //  myWebView.getSettings().getUseWideViewPort();
       // myWebView.loadUrl("http://190.1.0.57/ismrse");
        String url ="http://190.1.0.57/ismrse";



        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.evaluateJavascript("javascript:document.getElementsById(login_password).value('coucou');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        Log.d("LogName", s); // Log is written, but s is always null
                    }
                });
            }

        });
        myWebView.loadUrl(url);


        //  Test button push
        b = (Button)  findViewById(R.id.button1);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    postData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(){

        // define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can add other intent for other actions
        Intent intent = new Intent(MainActivity.this,  NotificationCompat.class);
        Intent intent2 = new Intent(this,MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent2, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0

        Notification mNotification = new NotificationCompat.Builder(this)
                .setContentTitle("International School of Monaco")
                .setContentText("Here's an awesome news for you!")
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setSound(soundUri)
                .setSmallIcon(R.mipmap.ic_launcher) //ok
                // .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                // .addAction(0, "View", pIntent)
                // .addAction(0, "Remind", pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);

    }

    public void cancelNotification(int notificationId){

        if (Context.NOTIFICATION_SERVICE!=null) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
            nMgr.cancel(notificationId);
        }
    }
  @Override
  protected void onResume() {
      super.onResume();
      LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
              new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
  }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String implodeHashMap(HashMap<String, String> hash, String delimiter) {
        String out = "";
        boolean first = true;
        for (Map.Entry<String, String> entry : hash.entrySet()) {
            out += (first) ? "" : delimiter;
            out += entry.getKey() + "=" + entry.getValue();
            first = false;
        }
        return out;
    }

    private String postToURL(String urlstr, HashMap<String, String> params) {
        InputStream is = null;
        byte[] buffer = {};
        int maxlen = 0, current = 0;
        int chunksize = 1024, bytes = 0, progress = 0;
        String paramstr = "";

        try {
            URL url = new URL(urlstr);
            paramstr = implodeHashMap(params, "&");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setFixedLengthStreamingMode(paramstr.getBytes().length);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.connect();
            PrintWriter ospw = new PrintWriter(con.getOutputStream());
            ospw.print(paramstr);
            ospw.close();
            Log.d(MainActivity.class.getName(), "Posting to URL " + urlstr); // XXX debug
            maxlen = con.getContentLength();
            if (maxlen <= 0)
                maxlen = 1048576;
            buffer = new byte[maxlen];
            is = con.getInputStream();
            if (is == null) { // WTF?
                Log.w(MainActivity.class.getName(), "URL yielded null InputStream!");
                return new String();
            }
            while ((bytes = is.read(buffer, current, (maxlen - current > chunksize) ? chunksize : maxlen - current)) != -1) {
                current += bytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ByteArrayInputStream b = new ByteArrayInputStream(buffer);
        Scanner scanner = new Scanner(b);
        scanner.useDelimiter("\\Z");
        String data = "";
        if (scanner.hasNext())
            data = scanner.next();
        return data;
    }

    public void postData() throws IOException {
        HashMap p = new HashMap();
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = instanceID.getToken(getString(R.string.ismSenderId),
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

        p.put("id_user", "toto");
        p.put("token_user", "lala");
        String b = postToURL("http://190.1.0.57/ismrse/create_token.php", p);
        Log.w(MainActivity.class.getName(), b);


        /*
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://190.1.0.57/ismrse/create_token.php");

        try {
            // Add your data
            ContentValues nameValuePairs = new ContentValues();
            nameValuePairs.put("id_user", "toto");
            nameValuePairs.put("token_user", "toto");
           // httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httppost.setEntity(new StringEntity(nameValuePairs.toString(), "UTF-8"));
            System.out.println("Post*************************");
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            System.out.println(response.getStatusLine());
            System.out.println("Post*************************");

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
       /* InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost("http://190.1.0.57/ismrse/create_token.php");

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id_user", "toto");
            jsonObject.put("token_user", "toto");

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            System.out.println(httpPost.toString());
            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        System.out.println(result);

        System.out.println("Post*************************");*/
        // 11. return result
       // return result;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
