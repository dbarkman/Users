package ms.cla.users;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;

import ms.cla.users.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Users. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link UserDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class UserListActivity extends AppCompatActivity {

    private static String tag = "UserListActivity";
    private View recyclerView;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private ProgressDialog progress;
    private final String getAllUsersApiAction = "ms.cla.getAllUsers";

    private void fetchUsers() {
        progress = ProgressDialog.show(this, null, "Fetching Users", true);
        RestTask task = new RestTask(this, getAllUsersApiAction, "users");
        task.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recyclerView = findViewById(R.id.user_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        registerReceiver(getAllUsersReceiver, new IntentFilter(getAllUsersApiAction));
        fetchUsers();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.User> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.User> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    intent.putExtra(UserDetailFragment.ARG_ITEM_ID, holder.mItem.details);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.User mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    private BroadcastReceiver getAllUsersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
            DummyContent.User user;
            DummyContent.ITEMS.clear();
            DummyContent.ITEM_MAP.clear();
            String apiResult = intent.getStringExtra(RestTask.httpResponse);
            try {
                JSONArray userArray = (new JSONArray(apiResult));
                int userCount = userArray.length();
                for (int i = 0; i < userCount; i++) {
                    String username = userArray.getJSONObject(i).getString("username");
                    String street = userArray.getJSONObject(i).getJSONObject("address").getString("street");
                    String suite = userArray.getJSONObject(i).getJSONObject("address").getString("suite");
                    String city = userArray.getJSONObject(i).getJSONObject("address").getString("city");
                    String zipcode = userArray.getJSONObject(i).getJSONObject("address").getString("zipcode");
                    String address = street + ", " + suite + "\n" + city + ", " + zipcode;
                    String id = userArray.getJSONObject(i).getString("id");
                    user = new DummyContent.User(username, address, id);
                    DummyContent.ITEMS.add(user);
                    DummyContent.ITEM_MAP.put(id, user);
                }
                setupRecyclerView((RecyclerView) recyclerView);
            } catch (JSONException je) {
                Log.e(tag, "Couldn't parse JSON result: " + je.getMessage());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(getAllUsersReceiver);
    }
}
