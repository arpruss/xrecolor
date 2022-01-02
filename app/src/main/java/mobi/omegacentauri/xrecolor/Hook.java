package mobi.omegacentauri.xrecolor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
		Context systemContext = (Context) XposedHelpers.callMethod( XposedHelpers.callStaticMethod( XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader), "currentActivityThread"), "getSystemContext" );

		final boolean blackStatusbar = false;
		final boolean blackNavbar = true;
		final String packageName = lpparam.packageName;

		if (blackStatusbar) {
			findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
					"setStatusBarColor",
					int.class,
					new XC_MethodHook() {
					@SuppressLint("InlinedApi")
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

						XposedBridge.log("recoloring sb "+packageName);

					((Window)param.thisObject).addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
					((Window)param.thisObject).clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);

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

							((Window)param.thisObject).clearFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
							((Window)param.thisObject).addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
							((Window)param.thisObject).setNavigationBarColor(Color.BLACK);
						}
					});
		}
	}
}
