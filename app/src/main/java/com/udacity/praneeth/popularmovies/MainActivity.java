package com.udacity.praneeth.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.udacity.praneeth.popularmovies.sync.MoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String MOVIE_DETAIL_FRAGMENT_TAG = "MDFTAG";
    private static final String MOVIE_LIST_FRAGMENT_TAG = "MLFTAG";
    private String mSortOrder;
    private boolean mTwoPane;
    private MainActivityFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSortOrder = Utility.getSortOrder(this);

        if (findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;

            mFragment = new MainActivityFragment();
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setTwoPane(mTwoPane);
            if (savedInstanceState == null) {

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, fragment)
                        .commit();
            } else {
                mFragment = (MainActivityFragment) getSupportFragmentManager()
                        .findFragmentByTag(MOVIE_LIST_FRAGMENT_TAG);
            }
        } else {
            mTwoPane = false;
        }
        if (mFragment != null) {
            mFragment.setTwoPane(mTwoPane);
        }
        MoviesSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String[] data) {

        if (mTwoPane) {

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setTwoPane(mTwoPane);
            Bundle args = new Bundle();
            args.putStringArray(Intent.EXTRA_TEXT, data);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().
                    replace(R.id.movie_detail_container, fragment, MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, data);
            startActivity(intent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        String sortOrder = Utility.getSortOrder(this);

        if (sortOrder != null && !sortOrder.equals(mSortOrder)) {
            if (null != mFragment) {
                mFragment.dataAfterSortSelected();
            } else {
                mFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
                mFragment.setTwoPane(mTwoPane);
                mFragment.dataAfterSortSelected();
            }
            mSortOrder = sortOrder;
        }
    }


}
