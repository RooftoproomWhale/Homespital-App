package com.kosmo.homespital.view;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int numbersOfFragment;

    public PagerAdapter(@NonNull FragmentManager fm, int numbersOfFragment) {
        super(fm,FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.numbersOfFragment = numbersOfFragment;

    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:return new HomeFragment();
            case 1:return new CoronaFragment();
            case 2:return new MapFragment();
            default:return new MedicineFragment();
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return numbersOfFragment;
    }
}
