package gml.waffles.gml;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        //about preference
        Preference updateCheck = findPreference("about");
        updateCheck.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //textview containing informations
                final TextView text = new TextView(getActivity());
                String versionName = "neni k dispozici";
                try {
                    versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String version = "Verze: " + versionName;
                String link = "Nové aktualizace a více informací naleznete <a href='http://gmlapp.tk/'>zde</a>";
                text.setMovementMethod(LinkMovementMethod.getInstance());
                text.setPadding(30, 30, 30, 30);
                text.setTextSize(18);
                text.setText(Html.fromHtml(version + "<br><br>" + link));
                //popup dialog builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(text);
                builder.setTitle("O aplikaci"); //title of popup
                //OK button
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                //show popup
                builder.show();
                return true;
            }
        });
    }
}
