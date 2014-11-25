package mohammad.adib.recentappslollipop;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Merges multiple data sets to get a list
 * of recent apps opened by the user on
 * Android Lollipop
 * <p/>
 * 1) List of installed apps that have a launcher icon
 * 2) List of recently opened apps (24 hr)
 * 3) TODO: List of currently running app processes
 */
public class RecentAppsManager {

    private Context mContext;
    private PackageManager mPackageManager;
    private List<RecentAppPollListener> mListeners;
    private int mInterval = 10000;
    private Handler mHandler;
    private Runnable mRunnable;
    private String homePackage;

    public RecentAppsManager(Context context) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
        mListeners = new ArrayList<RecentAppPollListener>();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mListeners.isEmpty()) {
                    List<String> recentAppPackages = getRecentApps();
                    for (RecentAppPollListener listener : mListeners)
                        if (listener != null)
                            listener.onRecentAppsPolled(recentAppPackages);
                    mHandler.postDelayed(this, mInterval);
                }
            }
        };
        //Get the launcher's package name
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo defaultLauncher = mPackageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        homePackage = defaultLauncher.activityInfo.packageName;
    }

    private UsageEvents getUsageEvents() {
        final UsageStatsManager usageStatsManager = (UsageStatsManager) mContext.getSystemService("usagestats");
        UsageEvents events = usageStatsManager.queryEvents(System.currentTimeMillis() - 86400000, System.currentTimeMillis());
        return events;
    }

    /**
     * Check if the user has given the app permission to
     * get usage statistics
     */
    public boolean isPermissionGiven() {
        return !getUsageEvents().hasNextEvent();
    }

    /**
     * Recent apps usage interval
     */
    public void setInterval(int interval) {
        mInterval = interval;
    }

    /**
     * Register a new listener to poll
     * for recent apps
     */
    public void registerRecentAppPollListener(RecentAppPollListener listener) {
        mListeners.add(listener);
        if (mListeners.size() == 1)
            mHandler.post(mRunnable);
    }

    public void unregisterRecentAppPollListener(RecentAppPollListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Get a list of recently opened
     * apps' package names
     */
    public List<String> getRecentApps() {
        UsageEvents events = getUsageEvents();
        List<String> recentPackageNames = new ArrayList<String>();
        // Get apps shown in launcher
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> installedApps = mPackageManager.queryIntentActivities(mainIntent, 0);
        final List<String> installedPackages = new ArrayList<String>();
        for (ResolveInfo info : installedApps)
            installedPackages.add(info.activityInfo.packageName);
        installedPackages.remove(mContext.getPackageName());
        // Filter out unwanted apps
        while (events.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            events.getNextEvent(event);
            String pkg = event.getPackageName();
            if (recentPackageNames.contains(pkg))
                recentPackageNames.remove(pkg);
            if (installedPackages.contains(pkg))
                recentPackageNames.add(pkg);
        }
        // Most recent first
        Collections.reverse(recentPackageNames);
        return recentPackageNames;
    }

    /**
     * Launch the settings app to ask the user
     * to grant permissions
     */
    public void launchPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * Get the name of an app from its
     * package name
     */
    public String getAppNameFromPackage(String pkg) {
        if (pkg.equals(homePackage))
            return "Home";
        ApplicationInfo ai;
        try {
            ai = mPackageManager.getApplicationInfo(pkg, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? mPackageManager.getApplicationLabel(ai) : "Unknown");
    }

    /**
     * A listener interface for polling
     */
    public interface RecentAppPollListener {
        public void onRecentAppsPolled(List<String> recentAppPackages);
    }
}
