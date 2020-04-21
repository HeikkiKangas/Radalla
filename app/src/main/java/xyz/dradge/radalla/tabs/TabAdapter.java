package xyz.dradge.radalla.tabs;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import xyz.dradge.radalla.tabs.route.RouteViewFragment;
import xyz.dradge.radalla.tabs.station.StationViewFragment;
import xyz.dradge.radalla.tabs.settings.SettingsViewFragment;

/**
 * Adapter handling transitions from tab to another.
 */
public class TabAdapter extends FragmentStateAdapter {

    /**
     * Getter for tab count.
     * @return count of tabs.
     */
    @Override
    public int getItemCount() {
        return 3;
    }

    /**
     * Default constructor.
     * @param fragmentActivity
     */
    public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Returns a fragment at given position on tabs.
     * @param position which tab should be returned.
     * @return tab at given position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new StationViewFragment();
            case 1:
                return new RouteViewFragment();
            case 2:
                return new SettingsViewFragment();
        }
        return new StationViewFragment();
    }
}
