package com.example.taxnoteandroid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.ActivityMainBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DefaultDataInstaller.installDefaultUserAndCategories(this);

//        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setBottomNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (binding.pager.getCurrentItem()) {
            case 0:
                menu.findItem(R.id.csv).setVisible(false);
                break;
            case 1:
                menu.findItem(R.id.csv).setVisible(true);
                break;
            case 2:
                menu.findItem(R.id.csv).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.csv:
                EntryDataManager entryDataManager = new EntryDataManager(this);
                List<Entry> entries = entryDataManager.findAll(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String join(CharSequence delimiter, List<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
//        for (Object token: tokens) {
//            if (firstTime) {
//                firstTime = false;
//            } else {
//                sb.append(delimiter);
//            }
//            sb.append(token);
//        }
        return sb.toString();
    }

    //--------------------------------------------------------------//
    //    -- Bottom Navigation --
    //--------------------------------------------------------------//

    private void setBottomNavigation() {

        // Set pager
//        final ViewPager viewPager   = (ViewPager) findViewById(R.id.pager);
        TabPagerAdapter adapter     = new TabPagerAdapter(getSupportFragmentManager());
//        viewPager.setAdapter(adapter);
//        viewPager.beginFakeDrag();
        binding.pager.setAdapter(adapter);
        binding.pager.beginFakeDrag();
        binding.pager.setOffscreenPageLimit(adapter.getCount());

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab1:
//                        viewPager.setCurrentItem(0);
                        binding.pager.setCurrentItem(0);
                        break;
                    case R.id.tab2:
                        //                        viewPager.setCurrentItem(1);
                        binding.pager.setCurrentItem(1);
                        break;
                    case R.id.tab3:
                        //                        viewPager.setCurrentItem(2);
                        binding.pager.setCurrentItem(2);
                        break;
                }
                invalidateOptionsMenu();
                return true;
            }
        });
    }

    class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EntryTabFragment.newInstance();
                case 1:
                    return HistoryTabFragment.newInstance();
                case 2:
                    return SettingsTabFragment.newInstance();
            }
            return EntryTabFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.Entry);
                case 1:
                    return getString(R.string.History);
                case 2:
                    return getString(R.string.Settings);
            }
            return super.getPageTitle(position);
        }
    }
}
