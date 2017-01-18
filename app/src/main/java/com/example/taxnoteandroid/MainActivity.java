package com.example.taxnoteandroid;

import android.content.Intent;
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
import com.example.taxnoteandroid.databinding.ActivityMainBinding;
import com.example.taxnoteandroid.entryTab.EntryTabFragment;

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
                menu.findItem(R.id.data_export).setVisible(false);
                break;
            case 1:
                menu.findItem(R.id.data_export).setVisible(true);
                break;
            case 2:
                menu.findItem(R.id.data_export).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.data_export:
                Intent intent = new Intent(this, DataExportActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //--------------------------------------------------------------//
    //    -- Bottom Navigation --
    //--------------------------------------------------------------//

    private void setBottomNavigation() {

        // Set pager
        TabPagerAdapter adapter     = new TabPagerAdapter(getSupportFragmentManager());
        binding.pager.setAdapter(adapter);
        binding.pager.beginFakeDrag();
        binding.pager.setOffscreenPageLimit(adapter.getCount());
        setTitle("入力");

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.tab1:
                        binding.pager.setCurrentItem(0, false);
                        setTitle(getString(R.string.Entry));
                        break;
                    case R.id.tab2:
                        binding.pager.setCurrentItem(1, false);
                        setTitle(getString(R.string.History));
                        break;
                    case R.id.tab3:
                        binding.pager.setCurrentItem(2, false);
                        setTitle(getString(R.string.Settings));
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
