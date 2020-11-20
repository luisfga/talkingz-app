package br.com.luisfga.talkingz.app.ui;

import android.content.Intent;
import android.os.Bundle;

import android.view.*;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import br.com.luisfga.talkingz.app.R;
import br.com.luisfga.talkingz.app.ui.contacts.ContactsFragment;
import br.com.luisfga.talkingz.app.ui.group.GroupsFragment;
import br.com.luisfga.talkingz.app.ui.profile.ProfileFragment;

public class TabsActivity extends OrchestraAbstractRootActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // remove title (to not show anything before splash screen
        setFullscreen(true);

        Intent splashScreenIntent = new Intent(getApplication(), SplashScreenActivity.class);
        startActivity(splashScreenIntent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        //ADAPTER
        TabFragmentPagerAdapter adapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ContactsFragment(), "Contatos");
        adapter.addFragment(new GroupsFragment(), "Grupos");
        adapter.addFragment(new ProfileFragment(), "Perfil");

        //ADAPTER >>> VIEWPAGER
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        //VIEWPAGER >>> TABLAYOUT
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        setFullscreen(false);
        getSupportActionBar().setElevation(0);//to hide divider-line between tabs and action bar
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_main_menu);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return true;
//    }

    class TabFragmentPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        TabFragmentPagerAdapter(FragmentManager manager) {
            super(manager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        Fragment getFragment(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}