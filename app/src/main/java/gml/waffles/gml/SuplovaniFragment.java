package gml.waffles.gml;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SuplovaniFragment extends Fragment {
    private WebView web;
    private View view;
    private SharedPreferences pref;
    //url variable for saving current url of web
    private String url = "";
    private String TAG = SuplovaniFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.suplovani, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //get xml data from internet (outside of main threat)
        class FetchData extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... params) {
                //define connection and reader
                HttpURLConnection connection = null;
                //create parser to contain xml
                XmlPullParser parser = Xml.newPullParser();
                //define string to contain resulting data
                String result = "";
                //connect and get data
                try {
                    //API request url
                    URL url = new URL("https://gmlbrno.edupage.org/eduapi?apikey=98C965812D0AB3258463&cmd=getbasedata");
                    //connect to url
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    //get input from connection and define buffer to create string
                    InputStream input = connection.getInputStream();
                    //return null if input is empty
                    if (input == null) return null;
                    //temporary converting inputstream to string TODO
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuffer buffer = new StringBuffer();
                    String line;
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    //set input as parse input
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(new StringReader(buffer.toString()));
                    //disconnect
                    connection.disconnect();
                    //fetch data from raw xml
                    int state = parser.getEventType();
                    while(state != XmlPullParser.END_DOCUMENT){
                        switch(state){
                            case XmlPullParser.START_TAG:
                                //writes list of tags with their values (except for first)
                                    result += parser.getName() + ": ";
                                    for(int a = 1; a < parser.getAttributeCount(); a++){
                                        result += parser.getAttributeValue(a) + ";";
                                    }
                                break;
                            case XmlPullParser.END_TAG:
                                //creates next line when tag ends
                                result += "\n";
                                break;
                            default:
                                break;
                        }
                        state = parser.next();
                    }

                } catch (ProtocolException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                //write string data to edittext view
                EditText txt = (EditText) view.findViewById(R.id.txt);
                txt.setText(s);
            }
        }
        //executes the FetchData class
        (new FetchData()).execute();
    }
}