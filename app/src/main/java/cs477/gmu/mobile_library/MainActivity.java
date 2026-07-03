package cs477.gmu.mobile_library;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cs477.gmu.mobile_library.data.LoginDataSource;
import cs477.gmu.mobile_library.data.LoginRepository;
import cs477.gmu.mobile_library.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private LoginRepository loginRepository;
    private ViewGroup confettiContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginRepository = LoginRepository.getInstance(new LoginDataSource());
        if (!loginRepository.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        confettiContainer = findViewById(R.id.confettiContainer);

        setupTabs();
    }

    public ViewGroup getConfettiContainer() {
        return confettiContainer;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            loginRepository.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Books");
                            break;
                        case 1:
                            tab.setText("Reading");
                            break;
                        case 2:
                            tab.setText("Finished");
                            break;
                    }
                }
        ).attach();
    }
}