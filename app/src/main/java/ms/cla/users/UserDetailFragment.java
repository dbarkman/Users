package ms.cla.users;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import ms.cla.users.dummy.DummyContent;

/**
 * A fragment representing a single User detail screen.
 * This fragment is either contained in a {@link UserListActivity}
 * in two-pane mode (on tablets) or a {@link UserDetailActivity}
 * on handsets.
 */
public class UserDetailFragment extends Fragment {

    private static String tag = "UserDetailFragment";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.User mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserDetailFragment() {
    }

    View rootView;
    private ProgressDialog progress;
    private final String getUserPostsApiAction = "ms.cla.getAllUsers";

    private void fetchUserPostss(String id) {
        progress = ProgressDialog.show(getActivity(), null, "Fetching Users", true);
        RestTask task = new RestTask(getActivity(), getUserPostsApiAction, "posts?userId=" + id);
        task.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().registerReceiver(getUserPostsReceiver, new IntentFilter(getUserPostsApiAction));

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.id);
            }
            fetchUserPostss(mItem.details);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.user_detail, container, false);
        return rootView;
    }

    private BroadcastReceiver getUserPostsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
            String apiResult = intent.getStringExtra(RestTask.httpResponse);
            try {
                String posts = "";
                JSONArray postArray = (new JSONArray(apiResult));
                int postCount = postArray.length();
                for (int i = 0; i < postCount; i++) {
                    if (i > 0) posts += "\n";
                    String title = postArray.getJSONObject(i).getString("title");
                    String body = postArray.getJSONObject(i).getString("body");
                    posts += title + "\n  " + body + "\n";
                }
            ((TextView) rootView.findViewById(R.id.user_detail)).setText(posts);
            } catch (JSONException je) {
                Log.e(tag, "Couldn't parse JSON result: " + je.getMessage());
            }
//            Log.d(tag, apiResult);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(getUserPostsReceiver);
    }
}
