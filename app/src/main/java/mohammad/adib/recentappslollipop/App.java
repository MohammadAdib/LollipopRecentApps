package mohammad.adib.recentappslollipop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class App extends android.app.Application {

    private static App instance;
    public String homePackageName;
    public PackageManager packageManager;

    @Override
    public void onCreate() {
        instance = this;
        packageManager = getPackageManager();
        //Get the launcher's package name
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo defaultLauncher = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        homePackageName = defaultLauncher.activityInfo.packageName;
    }

    public static App getInstance() {
        return instance;
    }
}
