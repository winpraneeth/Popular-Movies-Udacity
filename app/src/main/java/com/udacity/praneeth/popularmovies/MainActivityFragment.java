package com.udacity.praneeth.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.udacity.praneeth.popularmovies.adapter.MovieAdapter;
import com.udacity.praneeth.popularmovies.data.MovieContract.MovieEntry;
import com.udacity.praneeth.popularmovies.sync.MoviesSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private MovieAdapter mMovieAdapter;
    private static final int FORECAST_LOADER = 0;
    public static GridView gridView;
    private boolean mTwoPane;

    public static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_IS_ADULT,
            MovieEntry.COLUMN_BACK_DROP_PATH,
            MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
            MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_IS_VIDEO,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_STATUS,
            MovieEntry.COLUMN_DATE
    };

    public static final int COL_MOVIE_PK_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_IS_ADULT = 2;
    public static final int COL_BACK_DROP_PATH = 3;
    public static final int COL_ORIGINAL_LANGUAGE = 4;
    public static final int COL_ORIGINAL_TITLE = 5;
    public static final int COL_OVERVIEW = 6;
    public static final int COL_RELEASE_DATE = 7;
    public static final int COL_POSTER_PATH = 8;
    public static final int COL_POPULARITY = 9;
    public static final int COL_TITLE = 10;
    public static final int COL_IS_VIDEO = 11;
    public static final int COL_VOTE_AVERAGE = 12;
    public static final int COL_VOTE_COUNT = 13;
    public static final int COL_RUNTIME = 14;
    public static final int COL_STATUS = 15;
    public static final int COL_DATE = 16;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        gridView = (GridView) rootView.findViewById(R.id.movie_fragment_grid_view);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                String posterPath = cursor.getString(COL_POSTER_PATH);
                if (cursor != null) {

                    String data[] = {posterPath, cursor.getString(COL_RELEASE_DATE).toString(), Double.toString(cursor.getDouble(COL_VOTE_AVERAGE)), cursor.getString(COL_OVERVIEW),
                            cursor.getString(COL_ORIGINAL_TITLE), Integer.toString(cursor.getInt(COL_MOVIE_ID)), Boolean.toString(mTwoPane)};
                    ((Callback) getActivity()).onItemSelected(data);
                }
            }

        });

        return rootView;
    }

    public interface Callback {
        /**
         * MainActivityFragmentCallback for when an item has been selected.
         */
        void onItemSelected(String[] data);
    }

    public void setTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }

    public void dataAfterSortSelected() {

        MoviesSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOption = prefs.getString(getString(R.string.key_sort), getString(R.string.sort_value_popular));

        String sortOrder = MovieEntry.COLUMN_POPULARITY + " DESC";

        if (sortOption.equals("@string/vote_average")) {
            sortOrder = MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }

        Uri movieForIdUri = MovieEntry.buildMovieUri();

        return new CursorLoader(getActivity(),
                movieForIdUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
