package xyz.dradge.radalla.tabs;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabAdapter extends FragmentStateAdapter {

    @Override
    public int getItemCount() {
        return 3;
    }

    public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new StationViewFragment();
            case 1:
                return new RouteViewFragment();
            case 2:
                return new TrainViewFragment();
        }
        return new RouteViewFragment();
    }
}