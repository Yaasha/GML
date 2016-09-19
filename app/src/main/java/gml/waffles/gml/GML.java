package gml.waffles.gml;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class GML extends Activity {
    public static final int[] ICON = {R.drawable.ic_suplovani,R.drawable.ic_rozvrh,R.drawable.ic_plan,R.drawable.ic_jidelnicek,R.drawable.ic_settings};
    //double click exit application
    private long lastClicked;
    private static final long ExitDelay = 2000;
    //for saving and loading data
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gml);
        pref = getPreferences(0);

        //viepager for showing content
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        //crate adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        //add fragments
        adapter.addFragment(new SuplovaniFragment());
        adapter.addFragment(new RozvrhFragment());
        adapter.addFragment(new PlanFragment());
        adapter.addFragment(new JidelnicekFragment());
        adapter.addFragment(new SettingsFragment());
        //set adapter to viewpager
        viewPager.setAdapter(adapter);
        //tabs actionbar
        final TabLayout tabs = (TabLayout) findViewById(R.id.tabLayout);
        tabs.setupWithViewPager(viewPager);
        //set tabs icons
        for(int i = 0; i < tabs.getTabCount(); i++){
            tabs.getTabAt(i).setIcon(ICON[i]);
        }
        //select last selected tab and set its opacity
        int lastIndex = pref.getInt("save", 0);
        viewPager.setCurrentItem(lastIndex);
        setOpacity(tabs,lastIndex);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //change opacity of selected item
                setOpacity(tabs,position);
                //save framgment index when selected
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("save", position);
                editor.apply();
            }
        });

    }
    //change opacity of tabs
    private void setOpacity(TabLayout tabs, int index){
        //change opacity of selected item
        for(int i = 0; i < tabs.getTabCount(); i++){
            //reset opacity
            tabs.getTabAt(i).getIcon().setAlpha(150);
        }
        //opacity of selected tab
        tabs.getTabAt(index).getIcon().setAlpha(255);
    }
    //custom class for pager adapter
    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        //list of fragments
        private final List<Fragment> fragments = new ArrayList<>();

        public ViewPagerAdapter(android.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment){
            fragments.add(fragment);
        }
    }

    @Override
    public void onBackPressed() {
        long clicked = System.currentTimeMillis();
        if(clicked - lastClicked < ExitDelay) super.onBackPressed();
        else
        {
            lastClicked = clicked;
            Toast.makeText(this, "Pro ukončení aplikace stiskněte tlačítko zpět",Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnected (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null && netInfos.isConnected())
                return true;
        }
        return false;
    }
}