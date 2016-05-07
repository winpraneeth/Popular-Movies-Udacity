package com.udacity.praneeth.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeIntents;
import com.squareup.picasso.Picasso;
import com.udacity.praneeth.popularmovies.adapter.ReviewAdapter;
import com.udacity.praneeth.popularmovies.adapter.TrailerAdapter;
import com.udacity.praneeth.popularmovies.data.MovieContract.ReviewEntry;
import com.udacity.praneeth.popularmovies.data.MovieContract.TrailerEntry;
import com.udacity.praneeth.popularmovies.sync.MoviesSyncAdapter;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private static final int TRAILER_LOADER = 0;
    private static final int REVIEW_LOADER = 1;


    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    public static final String[] TRAILER_COLUMNS = {
            TrailerEntry.TABLE_NAME + "." + TrailerEntry._ID,
            TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID,
            TrailerEntry.COLUMN_TRAILER_ID,
            TrailerEntry.COLUMN_ISO_369_1,
            TrailerEntry.COLUMN_KEY,
            TrailerEntry.COLUMN_NAME,
            TrailerEntry.COLUMN_SITE,
            TrailerEntry.COLUMN_SIZE,
            TrailerEntry.COLUMN_TYPE,
            TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_DATE
    };

    public static final int COL_MOVIE_PK_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TRAILER_ID = 2;
    public static final int COL_ISO_369_1 = 3;
    public static final int COL_KEY = 4;
    public static final int COL_NAME = 5;
    public static final int COL_SITE = 6;
    public static final int COL_SIZE = 7;
    public static final int COL_TYPE = 8;
    public static final int COL_DATE = 9;

    public static final String[] REVIEWS_COLUMNS = {
            ReviewEntry.TABLE_NAME + "." + ReviewEntry._ID,
            ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_ID,
            ReviewEntry.COLUMN_REVIEW_ID,
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT,
            ReviewEntry.COLUMN_URL,
            ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_DATE
    };

    public static final int COL_REVIEW_MOVIE_PK_ID = 0;
    public static final int COL_REVIEW_MOVIE_ID = 1;
    public static final int COL_REVIEW_ID = 2;
    public static final int COL_AUTHOR = 3;
    public static final int COL_CONTENT = 4;
    public static final int COL_URL = 5;
    public static final int COL_REVIEW_DATE = 6;

    //private ImageView mBackgroundImageView   = null;
    private TextView mMovieNameView = null;
    private ImageView mMovieImageView = null;
    private TextView mMovieYearView = null;
    private TextView mMovieRatingsView = null;
    private TextView mMovieOverviewView = null;
    private ListView mMovieTrailersListView = null;
    private ListView mMovieReviewsListView = null;

    private TextView mTrailersEmptyView = null;
    private TextView mReviewsEmptyView = null;
    private Button mFavoriteButton = null;
    private TextView mTrailersHeaderTextView = null;
    private TextView mReviewsHeaderTextView = null;
    private int movieId = 0;
    private TrailerAdapter mTrailerListAdapter;
    private ReviewAdapter mReviewListAdapter;
    private SharedPreferences mPrefs = null;
    public static String FAVORITE_MOVIE_IDS_SET_KEY = "movie_id_set_key";
    private Toast mFavoriteToast;
    private boolean mTwoPane;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();
        String data[] = null;
        if (arguments != null) {
            data = arguments.getStringArray(Intent.EXTRA_TEXT);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);

        mTrailerListAdapter = new TrailerAdapter(getActivity(), null, 0);
        mReviewListAdapter = new ReviewAdapter(getActivity(), null, 0);

        mMovieImageView = (ImageView) rootView.findViewById(R.id.movie_poster);
        mMovieNameView = (TextView) rootView.findViewById(R.id.movie_title);
        mMovieYearView = (TextView) rootView.findViewById(R.id.release_date);
        mMovieRatingsView = (TextView) rootView.findViewById(R.id.vote_average);
        mMovieTrailersListView = (ListView) rootView.findViewById(R.id.listview_trailers);
        mMovieReviewsListView = (ListView) rootView.findViewById(R.id.listview_reviews);
        mFavoriteButton = (Button) rootView.findViewById(R.id.favorite_button);
        mMovieOverviewView = (TextView) rootView.findViewById(R.id.movie_overview);
        // mTrailersEmptyView     = (TextView) rootView.findViewById(R.id.listview_trailers_empty);
        //  mReviewsEmptyView      = (TextView) rootView.findViewById(R.id.listview_reviews_empty);
        mTrailersHeaderTextView = (TextView) rootView.findViewById(R.id.detail_label_trailers);
        mReviewsHeaderTextView = (TextView) rootView.findViewById(R.id.detail_label_reviews);

        if (data != null) {

            String posterPath = data[0];
            String dateValue = data[1];
            String ratings = data[2];
            String overview = data[3];
            String mMovieName = data[4];
            String movieIdStr = data[5];
            Typeface rosarioRegular = Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf");

            Picasso.with(getContext()).load(Utility.getImageURL(posterPath)).into(mMovieImageView);

            mMovieYearView.setText(dateValue);
            mMovieYearView.setTypeface(rosarioRegular);

            mMovieOverviewView.setText(overview);
            mMovieOverviewView.setTypeface(rosarioRegular);

            mMovieRatingsView.setText(ratings);
            mMovieRatingsView.setTypeface(rosarioRegular);

            mMovieNameView.setText(mMovieName);
            mMovieNameView.setTypeface(rosarioRegular);
            movieId = Integer.valueOf(movieIdStr);

        }

        mMovieTrailersListView.setAdapter(mTrailerListAdapter);
        mMovieReviewsListView.setAdapter(mReviewListAdapter);
//
//              if(mTrailerListAdapter.getCount() <= 0) {
//            mTrailersEmptyView.setText("No Trailers Available");
//            mMovieTrailersListView.setEmptyView(mTrailersEmptyView);
//        }

        //        if(mReviewListAdapter.getCount() <= 0) {
//            mReviewsEmptyView.setText("No Reviews Available");
//            mMovieReviewsListView.setEmptyView(mReviewsEmptyView);
//
//        }


        mFavoriteButton.setOnClickListener(this);
        if (isMovieFavorite()) {
            mFavoriteButton.setText("Remove from favorite");
        }

        mMovieTrailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                String movieId = cursor.getString(COL_KEY);

                Intent intent = YouTubeIntents.createPlayVideoIntent(
                        getActivity(), movieId);

                startActivity(intent);

            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case TRAILER_LOADER:
                Uri trackUri = TrailerEntry.buildMovieTrailerUri(movieId);

                return new CursorLoader(getActivity(),
                        trackUri,
                        TRAILER_COLUMNS,
                        null,
                        null,
                        null);

            case REVIEW_LOADER:
                Uri reviewUri = ReviewEntry.buildMovieReviewsUri(movieId);

                return new CursorLoader(getActivity(),
                        reviewUri,
                        REVIEWS_COLUMNS,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case TRAILER_LOADER:
                mTrailerListAdapter.swapCursor(data);
                Utility.setDynamicHeight(mMovieTrailersListView);

                break;
            case REVIEW_LOADER:
                mReviewListAdapter.swapCursor(data);
                Utility.setDynamicHeight(mMovieReviewsListView);
                setFocusUp();
                break;
        }

    }

    private void setFocusUp() {
        if (mMovieImageView != null) {
            mMovieImageView.setFocusable(true);
            mMovieImageView.setFocusableInTouchMode(true);
            mMovieImageView.requestFocus();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TRAILER_LOADER:
                mTrailerListAdapter.swapCursor(null);
                break;
            case REVIEW_LOADER:
                mReviewListAdapter.swapCursor(null);
                break;
        }
    }

    public void setTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }

    private void hideUIViews() {
        mMovieImageView.setVisibility(View.INVISIBLE);
        mMovieYearView.setVisibility(View.INVISIBLE);
        mMovieTrailersListView.setVisibility(View.INVISIBLE);
        mMovieTrailersListView.setAdapter(null);
        mMovieReviewsListView.setVisibility(View.INVISIBLE);
        mMovieReviewsListView.setAdapter(null);
        mMovieOverviewView.setVisibility(View.INVISIBLE);
        if (mMovieNameView != null) {
            mMovieNameView.setVisibility(View.INVISIBLE);
        }
        mMovieRatingsView.setVisibility(View.INVISIBLE);
        mFavoriteButton.setVisibility(View.INVISIBLE);
        mTrailersHeaderTextView.setVisibility(View.INVISIBLE);
        mReviewsHeaderTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (isAdded()) {
            if (key.equals(getString(R.string.key_sort))) {
                hideUIViews();
            }
        }
    }

    @Override
    public void onClick(View v) {
        boolean isAlreadyFavorite = isMovieFavorite();

        String labelId;
        String favoriteButtonText;
        Set<String> favoriteMovieIdsSet = null;

        if (mPrefs.contains(FAVORITE_MOVIE_IDS_SET_KEY)) {
            favoriteMovieIdsSet = mPrefs.getStringSet(FAVORITE_MOVIE_IDS_SET_KEY, null);
        }

        if (favoriteMovieIdsSet == null) {
            favoriteMovieIdsSet = new LinkedHashSet<>();
        }

        if (isAlreadyFavorite) {
            favoriteMovieIdsSet.remove(Integer.toString(movieId));
            labelId = "Removed from favorites";
            favoriteButtonText = "Add to favorite";
            MoviesSyncAdapter.syncImmediately(getActivity());

        } else {
            favoriteMovieIdsSet.add(Integer.toString(movieId));
            final SharedPreferences.Editor prefsEdit = mPrefs.edit();
            prefsEdit.putStringSet(FAVORITE_MOVIE_IDS_SET_KEY, favoriteMovieIdsSet);
            prefsEdit.commit();

            labelId = "Added to favorites";
            favoriteButtonText = "Remove from favorite";
        }

        if (mFavoriteToast != null) {
            mFavoriteToast.cancel();
        }

        mFavoriteToast = Toast.makeText(getContext(), labelId, Toast.LENGTH_SHORT);

        mFavoriteToast.show();
        mFavoriteButton.setText(favoriteButtonText);

    }

    private boolean isMovieFavorite() {
        boolean result = false;
        Set<String> favoriteMovieIdsSet = null;

        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }

        if (mPrefs.contains(FAVORITE_MOVIE_IDS_SET_KEY)) {
            favoriteMovieIdsSet = mPrefs.getStringSet(FAVORITE_MOVIE_IDS_SET_KEY, null);
        }

        if (favoriteMovieIdsSet != null) {
            Iterator<String> favIterator = favoriteMovieIdsSet.iterator();

            while (favIterator.hasNext()) {
                String favMovieId = favIterator.next();
                if (favMovieId.equals(Integer.toString(movieId))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}

