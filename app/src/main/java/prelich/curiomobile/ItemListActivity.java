package prelich.curiomobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import prelich.curiomobile.dummy.ProjectContent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
///////////////////////////////////////////////////////////////////////////
// Code is based off of the Master-Detail template from Android Studio SDK
///////////////////////////////////////////////////////////////////////////
public class ItemListActivity extends AppCompatActivity {

    private boolean mTwoPane;  // Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
    ProgressDialog mDialog;
    static boolean mInitialized = false;    // Variable used so we do not send HTTP requests on screen orientation or activity change
    SearchView mSearchView;
    public static CharSequence mSearchString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mSearchView = new SearchView(this);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        mSearchView.setLayoutParams(params);
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchString = newText;
                for (int i=0; i < ProjectContent.ITEMS.size(); i++) {
                    ProjectContent.Project project = ProjectContent.ITEMS.get(i);
                    if(project.description.toLowerCase().contains(newText.toLowerCase()) || project.name.toLowerCase().contains(newText.toLowerCase()) ) {
                        project.show =  true;
                    }
                    else {
                        project.show =  false;
                    }
                }
                // Update the View
                View recyclerView = findViewById(R.id.item_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);

                return false;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(!mSearchString.toString().isEmpty()) {
                    for (int i=0; i < ProjectContent.ITEMS.size(); i++) {
                        ProjectContent.ITEMS.get(i).show =  true;
                    }

                    // Update the View
                    View recyclerView = findViewById(R.id.item_list);
                    assert recyclerView != null;
                    setupRecyclerView((RecyclerView) recyclerView);
                }
                return false;
            }
        });
        toolbar.addView(mSearchView);

        // Restore the search string
        if(!mSearchString.toString().isEmpty())
            mSearchView.setQuery(mSearchString, false);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HTTPRequest request = new HTTPRequest();
                request.execute();
            }
        });

        HTTPRequest request = new HTTPRequest();
        if(!mInitialized) {
            request.execute();
            mInitialized = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("MVC", "save state");
        mSearchString = mSearchView.getQuery();
        super.onSaveInstanceState(outState);
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(ProjectContent.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<ProjectContent.Project> mValues;

        public SimpleItemRecyclerViewAdapter(List<ProjectContent.Project> items) {
            mValues = new ArrayList<ProjectContent.Project>(items);
            // Hide projects that don't match search
            for (int i=0; i < mValues.size(); i++) {
                if (mValues.get(i).show == false) {
                    mValues.remove(i);
                    i--;
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).name);
            String description = mValues.get(position).short_description;
            holder.mContentView.setText(description.substring(0, Math.min(description.length(), 100)) + "...");
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
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
            public ProjectContent.Project mItem;

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

    class HTTPRequest extends AsyncTask<String, Integer, Void> {

        private JSONArray concatArray(JSONArray... arrs) throws JSONException {
            JSONArray result = new JSONArray();
            for (JSONArray arr : arrs) {
                for (int i = 0; i < arr.length(); i++) {
                    // Check if the item already exists
                    boolean exists = false;
                    for (int j = 0; j < result.length(); j++) {
                        if (arr.get(i).toString().equals(result.get(j).toString())) {
                            exists = true;
                        }
                    }
                    if (!exists)
                        result.put(arr.get(i));
                }
            }
            return result;
        }

        @Override
        protected Void doInBackground(String... params) {
            URL projectsURL = null;
            URL questionsURL = null;
            HttpURLConnection urlConnection = null;

            try {
                projectsURL = new URL("http://test.crowdcurio.com/api/project/");
                questionsURL = new URL("http://test.crowdcurio.com/api/curio/");

                String inputStr;
                StringBuilder responseStrBuilder = new StringBuilder();
                urlConnection = (HttpURLConnection) projectsURL.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                ProjectContent.projectsArray = new JSONArray(String.valueOf(responseStrBuilder));

                urlConnection.disconnect();
                responseStrBuilder = new StringBuilder();
                urlConnection = (HttpURLConnection) questionsURL.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
                streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                ProjectContent.questionsArray = new JSONArray(String.valueOf(responseStrBuilder));
                ProjectContent.updateItems();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Lock the orientation as we load the projects so the main activity is not destroyed
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

            mDialog = new ProgressDialog(ItemListActivity.this);
            mDialog.setCancelable(false);
            mDialog.setMessage("Loading Projects");
            mDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            // Update the View
            View recyclerView = findViewById(R.id.item_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);

            mDialog.dismiss();

            // Unlock the orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }
}
