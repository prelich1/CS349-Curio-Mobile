package prelich.curiomobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import prelich.curiomobile.dummy.ProjectContent;

public class ItemDetailActivity extends AppCompatActivity {

    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HTTPRequest request = new HTTPRequest();
                request.execute();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Up button
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class HTTPRequest extends AsyncTask<String, Integer, Void> {

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

            mDialog = new ProgressDialog(ItemDetailActivity.this);
            mDialog.setCancelable(false);
            mDialog.setMessage("Loading Projects");
            mDialog.show();
        }

        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            mDialog.dismiss();

            // Update the current fragment view
            List<android.support.v4.app.Fragment> currentFragments = getSupportFragmentManager().getFragments();
            android.support.v4.app.Fragment currentFragment = currentFragments.get(0);
            currentFragment.onCreate(null);

            // Unlock the orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }
}
