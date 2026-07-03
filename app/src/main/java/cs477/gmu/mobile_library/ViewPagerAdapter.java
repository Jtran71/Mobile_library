package cs477.gmu.mobile_library;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BooksFragment();
            case 1:
                return new ReadingFragment();
            case 2:
                return new FinishedFragment();
            default:
                return new BooksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}