package mohammad.adib.recentappslollipop;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RecentAppsManager mManager;
    private RecentAppsManager.RecentAppPollListener mListener;
    private List<String> mRecentApps;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecentApps = new ArrayList<String>();
        mManager = new RecentAppsManager(this);
        if (!mManager.isPermissionGiven()) {
            mManager.launchPermissionSettings();
        }
        mAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                mRecentApps);
        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        setTitle("Lollipop Recent Apps");
    }

    @Override
    public void onResume() {
        super.onResume();
        mManager.setInterval(2000);
        mListener = new RecentAppsManager.RecentAppPollListener() {
            @Override
            public void onRecentAppsPolled(List<String> recentAppPackages) {
                mRecentApps.clear();
                for (String pkg : recentAppPackages)
                        mRecentApps.add(mManager.getAppNameFromPackage(pkg));
                mAdapter.notifyDataSetChanged();
            }
        };
        mManager.registerRecentAppPollListener(mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManager.unregisterRecentAppPollListener(mListener);
    }

}
