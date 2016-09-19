package gml.waffles.gml;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SuplovaniFragment extends Fragment {
    private WebView web;
    private View view;
    private SharedPreferences pref;
    //url variable for saving current url of web
    private String url = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.suplovani, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        //get data from settings
        /*boolean divide = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("suplovaniDivide", true);
            web = (WebView) view.findViewById(R.id.supl);
            web.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if(url.contains("gml.cz")) web.loadUrl(url); //prevents from opening other websites
                        return true;
                }
            });
            pref = getActivity().getPreferences(0);

            if(GML.isConnected(getActivity().getApplicationContext())) web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            else
            {
                web.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                Toast.makeText(getActivity().getApplicationContext(), "Nepodařilo se aktualizovat stránku",Toast.LENGTH_SHORT).show();
            }

        //if divide is enabled in settings
        if(divide){
            //load saved url
            String savedUrl = pref.getString("savedUrl", null);
            if(savedUrl != null) url = savedUrl;
            //if there is no saved url load current day
            else {
                //date for default url
                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                //condition for weekend > monday
                calendar.setFirstDayOfWeek(Calendar.SATURDAY);
                if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                }
                //load default url
                url = "http://www.gml.cz/zastupce/nove/subst_students" + date.format(calendar.getTime()) + ".htm";
            }
            web.loadUrl(url);
        }else web.loadUrl("http://www.gml.cz/zastupce/nove/"); //else load whole page


        //popup button
        final Button button = (Button) view.findViewById(R.id.popupSupl);

        //if enabled in settings
        if(divide) {
            //set button text
            button.setText(urlToDate(url));
            //popup
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //popup webview
                    WebView webView = new WebView(getActivity());
                    //check for internet connection and set cache mode accordingly
                    if (GML.isConnected(getActivity().getApplicationContext()))
                        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                    else webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    //load url
                    webView.loadUrl("http://www.gml.cz/zastupce/nove/subst_left.htm");
                    //popup builder
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(webView);
                    //popup dialog
                    final AlertDialog dialog = builder.create();
                    //show popup
                    dialog.show();
                    //get screen density for window size
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float density = displayMetrics.density;
                    //multiply density with days panel size according to http://www.gml.cz/zastupce/nove/
                    int width = Math.round(density * 150);
                    //set popup window size
                    dialog.getWindow().setLayout(width, view.getHeight());

                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String newUrl) {
                            url = newUrl;
                            web.loadUrl(url);
                            dialog.dismiss();
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("savedUrl", url);
                            editor.apply();

                            button.setText(urlToDate(url));
                            return true;
                        }
                    });
                }
            });
        }else button.setVisibility(View.INVISIBLE);*/



        }
    //convert suplementation url to date
    private String urlToDate(String url){
        //just in case the url wont be loaded
        if(url != null){
        String[] day = url.split("-");
        String t = day[2].replace("htm", "") + day[1] + ".";
        return t;
        }else return "Vybrat den";
    }
}
