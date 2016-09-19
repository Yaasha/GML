package gml.waffles.gml;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class JidelnicekFragment extends Fragment {
    private WebView web;
    private View view;
    public JidelnicekFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.jidelnicek, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        web = (WebView) view.findViewById(R.id.jidelnicek);

        if(GML.isConnected(getActivity().getApplicationContext())) web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        else
        {
            web.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            Toast.makeText(getActivity().getApplicationContext(), "Nepodařilo se aktualizovat stránku", Toast.LENGTH_SHORT).show();
        }
        web.loadUrl("http://www.gml.cz/intranet/vypis-celeho-tydne-jidelnicek.htm");
    }
}
