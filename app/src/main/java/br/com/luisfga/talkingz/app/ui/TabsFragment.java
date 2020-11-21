package br.com.luisfga.talkingz.app.ui;

import android.os.Bundle;

import android.view.*;
import androidx.annotation.Nullable;
import br.com.luisfga.talkingz.app.MainActivity;
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

public class TabsFragment extends OrchestraAbstractRootFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //ADAPTER
        TabFragmentPagerAdapter adapter = new TabFragmentPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new ContactsFragment(), "Contatos");
        adapter.addFragment(new GroupsFragment(), "Grupos");

        //ADAPTER >>> VIEWPAGER
        ViewPager viewPager = getView().findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        //VIEWPAGER >>> TABLAYOUT
        TabLayout tabLayout = getView().findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }

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