package gml.waffles.gml;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Method;

public class PlanFragment extends Fragment {
    private View view;
    private WebView web;
    //current search
    private String search;
    //for saving
    private SharedPreferences pref;
    //delay for double tap (in milliseconds)
    private static final long DoubleClickDelay = 300;
    //for double tap zoom
    private long lastClick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plan, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pref = getActivity().getPreferences(0);
        //get data from settings
        final boolean buttonEnabled = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("planButton", true);
        search = pref.getString("search", "");
        //webview
        web = (WebView) view.findViewById(R.id.planWeb);
        //pinch zoom
        web.getSettings().setBuiltInZoomControls(true);
        //hide zoom buttons
        web.getSettings().setDisplayZoomControls(false);
        //set resource file
        web.loadUrl("file:///android_asset/plan-gml.svg");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //set initial scale
                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        while (web.zoomOut()) ;
                    }
                };
                handler.postDelayed(r, 400);
                super.onPageFinished(view, url);
                //if enabled in settings
                if (buttonEnabled) {
                    //highlight last searched
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        web.findAllAsync(search);
                    } else web.findAll(search);
                }
            }
        });
        web.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //disable viewpager swipe if zoomed
                if (web.canZoomOut()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                } else view.getParent().requestDisallowInterceptTouchEvent(false);
                //double tap zoom
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    long click = System.currentTimeMillis();
                    if (click - lastClick < DoubleClickDelay) //on double tap
                    {
                        if(web.zoomOut()) while(web.zoomOut());
                        else for(int a =0; a < 5; a++) web.zoomIn();
                    }
                    lastClick = click;
                }
                return false;
            }
        });


        //disable text selection
        web.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
            //button for popup
            ImageButton button = (ImageButton) view.findViewById(R.id.popupButton);

        //if button is enabled in settings
        if(buttonEnabled) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //popup for class finding
                    final EditText textField = new EditText(getActivity());
                    textField.setText(search); //set text as last search
                    //popup dialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(textField);
                    builder.setTitle("Hledat"); //title of popup
                    //OK button
                    builder.setPositiveButton("Hledat", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            search = textField.getText().toString(); //set last search
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                web.findAllAsync(search); //highlight
                            } else {
                                web.findAll(search);
                            }
                            //save search data
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("search", search);
                            editor.commit();
                        }
                    });
                    //CANCEL button
                    builder.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            search = ""; //clear last search
                            web.clearMatches(); //clear highlight
                        }
                    });
                    //show popup
                    builder.show();
                }
            });
        }else button.setVisibility(View.INVISIBLE);

    }
}