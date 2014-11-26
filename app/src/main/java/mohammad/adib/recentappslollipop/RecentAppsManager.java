package mohammad.adib.recentappslollipop;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
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
 */
public class RecentAppsManager {

    private List<RecentAppPollListener> mListeners;
    private int mInterval = 10000;
    private Handler mHandler;
    private Runnable mRunnable;
    private boolean mExcludeHome;
    private boolean mExcludeSelf;
    private boolean mExcludeDuplicates;
    private boolean mExcludeUnwanted;

    public RecentAppsManager() {
        mListeners = new ArrayList<RecentAppPollListener>();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mListeners.isEmpty()) {
                    List<RecentApp> recentAppPackages = getRecentApps();
                    for (RecentAppPollListener listener : mListeners)
                        if (listener != null)
                            listener.onRecentAppsPolled(recentAppPackages);
                    mHandler.postDelayed(this, mInterval);
                }
            }
        };
    }

    private UsageEvents getUsageEvents() {
        final UsageStatsManager usageStatsManager = (UsageStatsManager) App.getInstance().getSystemService("usagestats");
        return usageStatsManager.queryEvents(System.currentTimeMillis() - 86400000, System.currentTimeMillis());
    }

    /**
     * Check if the user has given the app permission to
     * get usage statistics
     */
    public boolean isPermissionGiven() {
        return getUsageEvents().hasNextEvent();
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
     * Get a list of recently opened apps
     */
    public List<RecentApp> getRecentApps() {
        UsageEvents events = getUsageEvents();
        List<RecentApp> recentApps = new ArrayList<RecentApp>();
        // Get apps shown in launcher
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> installedApps = App.getInstance().getPackageManager().queryIntentActivities(mainIntent, 0);
        final List<String> installedPackages = new ArrayList<String>();
        for (ResolveInfo info : installedApps)
            installedPackages.add(info.activityInfo.packageName);
        if (mExcludeSelf)
            installedPackages.remove(App.getInstance().getPackageName());
        if (mExcludeHome)
            installedPackages.remove(App.getInstance().homePackageName);
        // Filter out unwanted apps
        while (events.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            events.getNextEvent(event);
            String pkg = event.getPackageName();
            RecentApp app = new RecentApp(event);
            if (installedPackages.contains(pkg) || !mExcludeUnwanted) {
                if (mExcludeDuplicates) {
                    for (RecentApp added : recentApps) {
                        if (added.getPackageName().equals(pkg)) {
                            recentApps.remove(added);
                            break;
                        }
                    }
                }
                recentApps.add(app);
            }
        }
        // Sort by most recent
        Collections.reverse(recentApps);
        return recentApps;
    }

    /**
     * Launch the settings app to ask the user
     * to grant permissions
     */
    public void launchPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().startActivity(intent);
    }

    /**
     * Exclude the homescreen
     */
    public void setExcludeHome(boolean excludeHome) {
        mExcludeHome = excludeHome;
    }

    /**
     * Exclude this app
     */
    public void setExcludeSelf(boolean excludeSelf) {
        mExcludeSelf = excludeSelf;
    }

    /**
     * Exclude duplicate events
     */
    public void setExcludeUnwanted(boolean excludeUnwanted) {
        mExcludeUnwanted = excludeUnwanted;
    }

    /**
     * Exclude duplicate events
     */
    public void setExcludeDuplicates(boolean excludeDuplicates) {
        mExcludeDuplicates = excludeDuplicates;
    }

    /**
     * A listener interface for polling
     */
    public interface RecentAppPollListener {
        public void onRecentAppsPolled(List<RecentApp> recentApps);
    }
}
