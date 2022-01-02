package mobi.omegacentauri.xrecolor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.WindowManager.*;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements IXposedHookLoadPackage {
    static InputMethodService ims = null;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);

        final boolean blackStatusbar = prefs.getBoolean(Main.PREF_STAT_BAR, false);
        final boolean blackNavbar = prefs.getBoolean(Main.PREF_NAV_BAR, true);

        if (blackStatusbar) {
            findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                    "setStatusBarColor",
                    int.class,
                    new XC_MethodHook() {
                        @SuppressLint("InlinedApi")
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                            View decor = ((Window) param.thisObject).getDecorView();
                            int suiv = decor.getSystemUiVisibility();

                            if ((suiv & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0) {
                                decor.setSystemUiVisibility(suiv & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            }

                            ((Window) param.thisObject).addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            ((Window) param.thisObject).clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            ((Window) param.thisObject).getDecorView().setSystemUiVisibility(((Window) param.thisObject).getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

                            param.args[0] = Color.BLACK;

                        }

                    });
        }
        if (blackNavbar) {
            findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                    "setNavigationBarColor",
                    int.class,
                    new XC_MethodHook() {
                        @SuppressLint("InlinedApi")
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = Color.BLACK;
                        }
                    });
            findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                    "generateDecor",
                    int.class,
                    new XC_MethodHook() {

                        @SuppressLint("InlinedApi")
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            ((Window) param.thisObject).clearFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                            ((Window) param.thisObject).addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            ((Window) param.thisObject).setNavigationBarColor(Color.BLACK);
                            View decor = (View) param.getResult();
                            int suiv = decor.getSystemUiVisibility();
                            if ((suiv & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) != 0) {
                                decor.setSystemUiVisibility(suiv & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        }
                    });
        }
    }
}
