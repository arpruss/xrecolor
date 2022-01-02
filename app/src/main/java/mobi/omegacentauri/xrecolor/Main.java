package mobi.omegacentauri.xrecolor;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Main extends Activity {
	Resources res;
	SharedPreferences prefs;
	static final String PREFS = "preferences";
//	public static final String PREF_ENTER = "enter";
	public static final String PREF_STAT_BAR = "statBar";
    public static final String PREF_NAV_BAR = "navBar";

	public static void saveIcon(Context c, String packageName) {
		deleteIcon(c, packageName);

		try {
			PackageManager pm = c.getPackageManager();
			ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
			Resources res = pm.getResourcesForApplication(app);
			Drawable icon = res.getDrawable(pm.getPackageInfo(packageName, 0).applicationInfo.icon);
//			Drawable icon = pm.getPackageInfo(packageName, 0).applicationInfo.loadIcon(c.getPackageManager());
			if (icon instanceof BitmapDrawable) {
				Bitmap bmp = ((BitmapDrawable)icon).getBitmap();
				File iconFile = getIconFile(c, packageName);
				FileOutputStream out = new FileOutputStream(iconFile);
				bmp.compress(CompressFormat.PNG, 100, out);
				out.close();
			}
		} catch (Exception e) {
			deleteIcon(c, packageName);
		}		
	}
	
	public static File getIconFile(Context c, String packageName) {
		return new File(c.getCacheDir(), 
				Uri.encode(packageName)+".png");
	}
	
	public static void deleteIcon(Context c, String packageName) {
		if (getIconFile(c, packageName).delete()) {
			Log.v("FastLaunch", "successful delete of "+packageName+" icon");
		}
	}

	private void message(String title, String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {} });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {} });
		alertDialog.show();

	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		prefs = getSharedPreferences(Main.PREFS, Context.MODE_WORLD_READABLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.apps);
        
        res = getResources();

        CheckBox statBarBlack = (CheckBox) findViewById(R.id.statbarblack);
		statBarBlack.setChecked(prefs.getBoolean(PREF_STAT_BAR, false));

		statBarBlack.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean(PREF_STAT_BAR, isChecked).apply();
			}
		});

        CheckBox navBarBlack = (CheckBox)findViewById(R.id.navbarblack);
        navBarBlack.setChecked(prefs.getBoolean(PREF_NAV_BAR, true));

        navBarBlack.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(PREF_NAV_BAR, isChecked).apply();
            }
        });

        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		w.setNavigationBarColor(Color.BLACK);
	}

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch(item.getItemId()) {
//    	case R.id.clear:
//    		clear();
//    		return true;
//    	case R.id.options:
//    		startActivity(new Intent(this, Options.class));
//    		return true;
//    	default:
//    		return false;
//    	}
//    }
//
//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//	    return true;
//	}

	@Override
    public void onResume() {
    	super.onResume();
    }

}

