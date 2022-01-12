package mobi.omegacentauri.xrecolor;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.WindowManager.*;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements IXposedHookLoadPackage {
    static InputMethodService ims = null;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        XSharedPreferences prefs = new XSharedPreferences(Options.class.getPackage().getName(), Options.PREFS);

        final boolean blackStatusbar = prefs.getBoolean(Options.PREF_STAT_BAR, false);
        final String navBar = prefs.getString(Options.PREF_NAV_BAR, "black");
        final boolean forceStatusbar = prefs.getBoolean(Options.PREF_FORCE_STAT_BAR, false);

        if (blackStatusbar || navBar.equals("match")) {
            findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                    "setStatusBarColor",
                    int.class,
                    new XC_MethodHook() {
                        @SuppressLint("InlinedApi")
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Window w = (Window)param.thisObject;
                            if (blackStatusbar) {
                                View decor = w.getDecorView();
                                int suiv = decor.getSystemUiVisibility();

                                if ((suiv & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0) {
                                    decor.setSystemUiVisibility(suiv & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                                }

                                w.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                w.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
                                w.getDecorView().setSystemUiVisibility(((Window) param.thisObject).getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                                param.args[0] = Color.BLUE;
                            }

                            if (navBar.equals("match")) {
                                w.setNavigationBarColor((Integer) param.args[0]);
                            }
                        }
                    });
        }

        if (forceStatusbar || !navBar.equals("default")) {
            if (navBar.equals("50") || navBar.equals("black"))
                findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                        "setNavigationBarColor",
                        int.class,
                        new XC_MethodHook() {
                            @SuppressLint("InlinedApi")
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                param.args[0] = navBar.equals("50") ? 0x80 << 24 : Color.BLACK;
                            }
                        });

            findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                    "generateDecor",
                    int.class,
                    new XC_MethodHook() {
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (forceStatusbar) {
                                Window w = (Window) param.thisObject;

                                w.clearFlags(LayoutParams.FLAG_FULLSCREEN);
                                w.addFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                            }
                        }

                        @SuppressLint("InlinedApi")
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!navBar.equals("default")) {
                                boolean translucent;
                                int color;
                                boolean dark;

                                Window w = (Window) param.thisObject;

                                if (navBar.equals("50")) {
                                    translucent = true;
                                    color = 0x80 << 24;
                                    dark = true;
                                } else if (navBar.equals("black")) {
                                    translucent = false;
                                    color = Color.BLACK;
                                    dark = true;
                                } else /* match */ {
                                    color = w.getStatusBarColor();
                                    translucent = (w.getAttributes().flags & LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;
                                    if (!translucent) {
                                        if (Color.luminance(color) < 0.5) {
                                            color = Color.BLACK;
                                            dark = true;
                                        } else {
                                            color = Color.WHITE;
                                            dark = false;
                                        }
                                    } else {
                                        dark = true;
                                    }
                                }

                                if (translucent) {
                                    w.addFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                                    w.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                } else {
                                    w.clearFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                                    w.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                }
                                w.setNavigationBarColor(color);

                                View decor = (View) param.getResult();
                                int suiv = decor.getSystemUiVisibility();

                                if (dark) {
                                    if ((suiv & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) != 0) {
                                        decor.setSystemUiVisibility(suiv & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                                    }
                                } else {
                                    if ((suiv & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) == 0) {
                                        decor.setSystemUiVisibility(suiv | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                                    }
                                }
                            }
                        }
                    });
        }
    }
}