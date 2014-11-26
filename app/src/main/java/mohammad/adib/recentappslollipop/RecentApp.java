package mohammad.adib.recentappslollipop;

import android.app.usage.UsageEvents;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class RecentApp {

    private UsageEvents.Event mEvent;

    public RecentApp(UsageEvents.Event event) {
        mEvent = event;
    }

    public String getAppName() {
        return getAppNameFromPackage(getPackageName());
    }

    public String getPackageName() {
        return mEvent.getPackageName();
    }

    public String getClassName() {
        return mEvent.getClassName();
    }

    public UsageEvents.Event getEvent() {
        return mEvent;
    }

    /**
     * Get the name of an app from its
     * package name. Returns Home if its the launcher
     */
    private String getAppNameFromPackage(String pkg) {
        if (pkg.equals(App.getInstance().homePackageName))
            return "Home";
        ApplicationInfo ai;
        try {
            ai = App.getInstance().getPackageManager().getApplicationInfo(pkg, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? App.getInstance().getPackageManager().getApplicationLabel(ai) : "Unknown");
    }

    /**
     * Launch this app
     */
    public void launch() {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(getPackageName(), getClassName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            App.getInstance().startActivity(intent);
        } catch (Exception e) {
            //Fallback
            Intent intent = App.getInstance().getPackageManager().getLaunchIntentForPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            App.getInstance().startActivity(intent);
        }
    }

}
