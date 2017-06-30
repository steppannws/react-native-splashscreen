package com.remobile.splashscreen;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.IOException;
import java.lang.NullPointerException;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.common.MapBuilder;

import java.util.Map;


public class RCTSplashScreen extends ReactContextBaseJavaModule {
    private static Dialog splashDialog;
    private ImageView splashImageView;

    private Activity activity;
    private boolean translucent;

    public RCTSplashScreen(ReactApplicationContext reactContext,  Activity activity, boolean translucent) {
        super(reactContext);
        this.activity = activity;
        this.translucent = translucent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        showSplashScreen();
    }

    @Override
    public String getName() {
        return "SplashScreen";
    }

    protected Activity getActivity() {
        return activity;
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
        return MapBuilder.<String, Object>of("translucent", translucent);
    }

    @ReactMethod
    public void hide() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                removeSplashScreen();
            }
        }, 500);
    }


    private void removeSplashScreen() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (splashDialog != null && splashDialog.isShowing()) {
                        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setDuration(1000);
                        View view = ((ViewGroup)splashDialog.getWindow().getDecorView()).getChildAt(0);
                        view.startAnimation(fadeOut);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                splashDialog.dismiss();
                                splashDialog = null;
                                splashImageView = null;
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }
                }
            });
        } catch (Exception ex) {}
    }

    private int getSplashId() {
        int drawableId = 0;
        try {
            drawableId = getActivity().getResources().getIdentifier("splash", "drawable", getActivity().getClass().getPackage().getName());
            if (drawableId == 0 && getActivity() != null) {
                drawableId = getActivity().getResources().getIdentifier("splash", "drawable", getActivity().getPackageName());
            }
        } catch (Exception ex) {
            drawableId = 0;
        }

        return drawableId;
    }

    private void showSplashScreen() {
        final int drawableId = getSplashId();
        if ((splashDialog != null && splashDialog.isShowing())||(drawableId == 0)) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                // Get reference to display
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Context context = getActivity();

                // Use an ImageView to render the image because of its flexible scaling options.
                splashImageView = new ImageView(context);
                splashImageView.setImageResource(drawableId);
                LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                splashImageView.setLayoutParams(layoutParams);
                splashImageView.setMinimumHeight(display.getHeight());
                splashImageView.setMinimumWidth(display.getWidth());
                splashImageView.setBackgroundColor(Color.BLACK);
                splashImageView.setScaleType(ImageView.ScaleType.FIT_XY);

                // Create and show the dialog
                splashDialog = new Dialog(context, translucent ? android.R.style.Theme_Translucent_NoTitleBar_Fullscreen : android.R.style.Theme_Translucent_NoTitleBar);
                // check to see if the splash screen should be full screen
                if ((getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                    splashDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                splashDialog.setContentView(splashImageView);
                splashDialog.setCancelable(false);
                if (activity != null && !activity.isFinishing()) {
                    try {
                        splashDialog.show();
                    } catch (Exception ex) {}
                }
            }
        });
    }
}
