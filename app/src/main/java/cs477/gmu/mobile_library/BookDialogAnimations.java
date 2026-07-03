package cs477.gmu.mobile_library;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class BookDialogAnimations {

    public static void animateDialogSlideIn(Context context, View view) {
        Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);
        view.startAnimation(slideIn);
    }

    public static void animateBouncePop(View view) {
        if (view == null) return;
        
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);
        
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    public static void animateCollapseRemove(View view, Runnable onAnimationEnd) {
        if (view == null) {
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
            return;
        }
        
        final View viewRef = view;
        
        view.animate().cancel();
        
        view.setTranslationY(0f);
        view.setAlpha(1f);
        
        view.animate()
                .translationY(40f)
                .alpha(0f)
                .setDuration(750)
                .withEndAction(() -> {
                    if (viewRef != null && viewRef.getParent() != null) {
                        viewRef.setTranslationY(0f);
                        viewRef.setAlpha(1f);
                        viewRef.clearAnimation();
                    }
                    if (onAnimationEnd != null) {
                        onAnimationEnd.run();
                    }
                })
                .start();
    }

    public static void showConfetti(Context context) {
        if (!(context instanceof AppCompatActivity)) return;
        
        AppCompatActivity activity = (AppCompatActivity) context;
        ViewGroup container = null;
        
        if (activity instanceof MainActivity) {
            container = ((MainActivity) activity).getConfettiContainer();
        }
        
        if (container == null) return;
        
        Random random = new Random();
        int[] colors = {
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#FFA07A"),
            Color.parseColor("#98D8C8"),
            Color.parseColor("#F7DC6F"),
            Color.parseColor("#BB8FCE"),
            Color.parseColor("#85C1E2")
        };
        
        int particleCount = 50;
        int width = container.getWidth();
        int height = container.getHeight();
        
        if (width == 0 || height == 0) {
            container.post(() -> showConfetti(context));
            return;
        }
        
        for (int i = 0; i < particleCount; i++) {
            View particle = new View(context);
            int size = 20 + random.nextInt(30);
            int color = colors[random.nextInt(colors.length)];
            
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = random.nextInt(width);
            params.topMargin = -size;
            particle.setLayoutParams(params);
            particle.setBackgroundColor(color);
            particle.setAlpha(0.9f);
            
            container.addView(particle);
            
            float endX = (random.nextFloat() - 0.5f) * width * 0.8f;
            float endY = height + size;
            float rotation = random.nextFloat() * 720f - 360f;
            
            particle.animate()
                    .translationX(endX)
                    .translationY(endY)
                    .rotation(rotation)
                    .alpha(0f)
                    .setDuration(2000 + random.nextInt(1000))
                    .setStartDelay(random.nextInt(200))
                    .withEndAction(() -> {
                        if (particle.getParent() != null) {
                            ((ViewGroup) particle.getParent()).removeView(particle);
                        }
                    })
                    .start();
        }
    }

    public static void showProgressPopup(Context context, int pagesRead, int totalPages) {
        if (!(context instanceof AppCompatActivity)) return;

        AppCompatActivity activity = (AppCompatActivity) context;
        ViewGroup container = null;

        if (activity instanceof MainActivity) {
            container = ((MainActivity) activity).getConfettiContainer();
        }

        if (container == null) return;

        int width = container.getWidth();
        int height = container.getHeight();

        if (width == 0 || height == 0) {
            container.post(() -> showProgressPopup(context, pagesRead, totalPages));
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_progress, null);

        ProgressBar progressBar = popupView.findViewById(R.id.progressBar);
        TextView tvProgressText = popupView.findViewById(R.id.tvProgressText);

        int progress = totalPages > 0 ? (int) ((pagesRead * 100.0) / totalPages) : 0;
        progressBar.setProgress(0);
        tvProgressText.setText(pagesRead + " / " + totalPages + " pages");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.CENTER;
        popupView.setLayoutParams(params);

        popupView.setAlpha(0f);
        popupView.setTranslationY(50f);
        container.addView(popupView);

        popupView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    android.animation.ObjectAnimator progressAnimator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", 0, progress);
                    progressAnimator.setDuration(800);
                    progressAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());
                    progressAnimator.start();
                    
                    popupView.postDelayed(() -> {
                        popupView.animate()
                                .alpha(0f)
                                .translationY(-50f)
                                .setDuration(300)
                                .withEndAction(() -> {
                                    if (popupView.getParent() != null) {
                                        ((ViewGroup) popupView.getParent()).removeView(popupView);
                                    }
                                })
                                .start();
                    }, 2000);
                })
                .start();
    }
}

