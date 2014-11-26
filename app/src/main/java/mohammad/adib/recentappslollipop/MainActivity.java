package mohammad.adib.recentappslollipop;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RecentAppsManager mManager;
    private RecentAppsManager.RecentAppPollListener mListener;
    private List<String> mRecentAppNames;
    private List<RecentApp> mRecentApps;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecentApps = new ArrayList<RecentApp>();
        mRecentAppNames = new ArrayList<String>();
        mManager = new RecentAppsManager();
        if (!mManager.isPermissionGiven()) {
            mManager.launchPermissionSettings();
            Toast.makeText(this, "Please enable " + getString(R.string.app_name) + " to access your recent apps", Toast.LENGTH_LONG).show();
        }
        mManager.setExcludeDuplicates(true);
        mManager.setExcludeSelf(true);
        mAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                mRecentAppNames);
        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mRecentApps.get(position).launch();
            }
        });
        setTitle("Lollipop Recent Apps");
    }

    @Override
    public void onResume() {
        super.onResume();
        mManager.setInterval(2000);
        mListener = new RecentAppsManager.RecentAppPollListener() {
            @Override
            public void onRecentAppsPolled(List<RecentApp> recentApps) {
                mRecentApps = recentApps;
                mRecentAppNames.clear();
                for (RecentApp app : mRecentApps)
                        mRecentAppNames.add(app.getAppName());
                mAdapter.notifyDataSetChanged();
            }
        };
        mManager.registerRecentAppPollListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mManager.unregisterRecentAppPollListener(mListener);
    }

}
