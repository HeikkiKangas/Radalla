package xyz.dradge.radalla;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

import xyz.dradge.radalla.tabs.TabAdapter;

public class NewMainActivity extends AppCompatActivity {
    private final List<String> TAB_NAMES = Arrays.asList("Station", "Route", "Train");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TabAdapter tabAdapter = new TabAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(tabAdapter);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> tab.setText(TAB_NAMES.get(position))
        ).attach();
    }
}
