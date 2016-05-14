package com.udacity.praneeth.popularmovies.task;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import com.udacity.praneeth.popularmovies.R;
import com.udacity.praneeth.popularmovies.Utility;
import com.udacity.praneeth.popularmovies.data.MovieContract;
import com.udacity.praneeth.popularmovies.data.MovieContract.MovieEntry;
import com.udacity.praneeth.popularmovies.data.MovieContract.TrailerEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Praneeth on 4/21/2016.
 */
public class FetchMovieTask {


    public final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private String mMovieIds[];
    private Context mContext;
    private Time mDayTime = new Time();
    private int mJulianStartDay = 0;
    private String mSortByParamValue = "popularity.desc";

    public FetchMovieTask() {
    }

    public void loadData(Context context) {
        mContext = context;
        mDayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        mJulianStartDay = Time.getJulianDay(System.currentTimeMillis(), mDayTime.gmtoff);
        deleteOldData();

        if (Utility.getSortOrder(mContext) != null) {
            mSortByParamValue = Utility.getSortOrder(mContext);
        }

        if (!mSortByParamValue.equals(context.getString(R.string.sort_value_favorite))) {
            mMovieIds = loadMovieData();
            if (mMovieIds == null || mMovieIds.length == 0) {
                return;
            }

            FetchMovieTrailersDataTask trailersDataTask = new FetchMovieTrailersDataTask();
            trailersDataTask.execute();
        }
    }


    private String[] loadMovieData() {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String app_key = "f9bfc7b7cb017b6d26c36014074e99e7";

        try {
            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
            ;
            final String SORT_BY_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(mSortByParamValue)
                    .appendQueryParameter(API_KEY_PARAM, app_key)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.

                return null;
            }

            return getMovieDataFromJson(buffer.toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error " + e.getMessage(), e);
            e.printStackTrace();

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    private String[] getMovieDataFromJson(String moviesJsonStr)
            throws JSONException {
        String[] movieIds = new String[0];

        final String OWM_RESULTS_ARRAY = "results";
        final String OWM_ADULT = "adult";
        final String OWN_BACKDROP_PATH = "backdrop_path";
        final String OWM_GENRE_IDS = "genre_ids";
        final String OWM_ID = "id";
        final String OWM_ORIGINAL_LANGUAGE = "original_language";
        final String OWM_ORIGINAL_TITLE = "original_title";
        final String OWM_OVERVIEW = "overview";
        final String OWM_RELEASE_DATE = "release_date";
        final String OWM_POSTER_PATH = "poster_path";
        final String OWM_POPULARITY = "popularity";
        final String OWM_TITLE = "title";
        final String OWM_VIDEO = "video";
        final String OWM_VOTE_AVERAGE = "vote_average";
        final String OWM_VOTE_COUNT = "vote_count";

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.


        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS_ARRAY);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());
            movieIds = new String[moviesArray.length()];

            String[] favoriteMovieIds = Utility.loadFavoriteMovieIds(mContext);
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movieJSONObject = moviesArray.getJSONObject(i);

                String movieId = movieJSONObject.getString(OWM_ID);

                if (!Utility.isMovieIdFavorite(favoriteMovieIds, movieId)) {
                    boolean isAdult = movieJSONObject.getBoolean(OWM_ADULT);
                    String backdropPath = movieJSONObject.getString(OWN_BACKDROP_PATH);
                    String originalLanguage = movieJSONObject.getString(OWM_ORIGINAL_LANGUAGE);
                    String originalTitle = movieJSONObject.getString(OWM_ORIGINAL_TITLE);
                    String overview = movieJSONObject.getString(OWM_OVERVIEW);
                    String releaseDate = movieJSONObject.getString(OWM_RELEASE_DATE);
                    String posterPath = movieJSONObject.getString(OWM_POSTER_PATH);
                    Double popularity = movieJSONObject.getDouble(OWM_POPULARITY);
                    String title = movieJSONObject.getString(OWM_TITLE);
                    boolean isVideo = movieJSONObject.getBoolean(OWM_VIDEO);
                    Double voteAverage = movieJSONObject.getDouble(OWM_VOTE_AVERAGE);
                    Integer voteCount = movieJSONObject.getInt(OWM_VOTE_COUNT);

                    ContentValues movieValues = new ContentValues();

                    movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
                    movieValues.put(MovieEntry.COLUMN_IS_ADULT, isAdult);
                    movieValues.put(MovieEntry.COLUMN_BACK_DROP_PATH, backdropPath);
                    movieValues.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, originalLanguage);
                    movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                    movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                    movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                    movieValues.put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
                    Log.d(LOG_TAG, "Movie Initial Data Loading Task Complete. " + posterPath + " rows inserted");
                    movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
                    movieValues.put(MovieEntry.COLUMN_TITLE, title);
                    movieValues.put(MovieEntry.COLUMN_IS_VIDEO, isVideo);
                    movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                    movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, voteCount);
                    movieValues.put(MovieEntry.COLUMN_DATE, mDayTime.setJulianDay(mJulianStartDay));

                    cVVector.add(movieValues);
                    movieIds[i] = movieId;
                }

            }// end of for

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "Movie Initial Data Loading Task Complete. " + inserted + " rows inserted");


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();

        }

        return movieIds;
    }

    public class FetchMovieTrailersDataTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchMovieTrailersDataTask.class.getSimpleName();


        @Override
        protected Void doInBackground(Void[] params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            int inserted = 0;
            String app_key = "f9bfc7b7cb017b6d26c36014074e99e7";

            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_PARAM = "api_key";
                final String VIDEOS = "videos";

                for (int i = 0; i < mMovieIds.length; i++) {

                    Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                            .appendPath(mMovieIds[i])
                            .appendPath(VIDEOS)
                            .appendQueryParameter(API_KEY_PARAM, app_key)
                            .build();

                    URL url = new URL(builtUri.toString());

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = null;
                    try {
                        inputStream = urlConnection.getInputStream();
                    } catch (Exception e) {
                    }
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        continue;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.

                        continue;
                    }

                    inserted = inserted + getTrailersDataFromJson(buffer.toString());
                    Log.d(LOG_TAG, "Trailer for movie " + mMovieIds + " are " + getTrailersDataFromJson(buffer.toString()));

                }//end of for

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error " + e.getMessage(), e);
                e.printStackTrace();

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            Log.d(LOG_TAG, "Movie Trailer Data Loading Task Complete. " + inserted + " rows inserted");
            return null;
        }

        private int getTrailersDataFromJson(String moviesJsonStr)
                throws JSONException {

            final String OWM_MOVIE_ID = "id";
            final String OWM_RESULTS_ARRAY = "results";
            final String OWM_TRAILER_ID = "id";
            final String OWM_ISO_639_1 = "iso_639_1";
            final String OWM_KEY = "key";
            final String OWM_NAME = "name";
            final String OWM_SITE = "site";
            final String OWM_SIZE = "size";
            final String OWM_TYPE = "type";
            int inserted = 0;

            try {
                JSONObject moviesJson = new JSONObject(moviesJsonStr);
                Integer movieId = moviesJson.getInt(OWM_MOVIE_ID);

                JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS_ARRAY);
                Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject movieJSONObject = moviesArray.getJSONObject(i);

                    String trailerId = movieJSONObject.getString(OWM_TRAILER_ID);

                    String ISO_639_1 = movieJSONObject.getString(OWM_ISO_639_1);
                    String key = movieJSONObject.getString(OWM_KEY);
                    String name = movieJSONObject.getString(OWM_NAME);

                    String site = movieJSONObject.getString(OWM_SITE);
                    String size = movieJSONObject.getString(OWM_SIZE);
                    String type = movieJSONObject.getString(OWM_TYPE);


                    ContentValues trailerValues = new ContentValues();

                    trailerValues.put(TrailerEntry.COLUMN_MOVIE_ID, movieId);
                    trailerValues.put(TrailerEntry.COLUMN_TRAILER_ID, trailerId);
                    trailerValues.put(TrailerEntry.COLUMN_ISO_369_1, ISO_639_1);
                    trailerValues.put(TrailerEntry.COLUMN_KEY, key);
                    trailerValues.put(TrailerEntry.COLUMN_NAME, name);
                    trailerValues.put(TrailerEntry.COLUMN_SITE, site);
                    trailerValues.put(TrailerEntry.COLUMN_SIZE, size);
                    trailerValues.put(TrailerEntry.COLUMN_TYPE, type);
                    trailerValues.put(MovieEntry.COLUMN_DATE, mDayTime.setJulianDay(mJulianStartDay));

                    cVVector.add(trailerValues);
                }

                // add to database
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    inserted = mContext.getContentResolver().bulkInsert(TrailerEntry.CONTENT_URI, cvArray);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            }
            return inserted;
        }

        @Override
        protected void onPostExecute(Void params) {
            FetchMovieReviewsDataTask reviewsDataTask = new FetchMovieReviewsDataTask();
            reviewsDataTask.execute();
        }
    }// end of Trailers Task

    public class FetchMovieReviewsDataTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchMovieReviewsDataTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void[] params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            int inserted = 0;
            String app_key = "f9bfc7b7cb017b6d26c36014074e99e7";

            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_PARAM = "api_key";
                final String REVIEWS = "reviews";

                for (int i = 0; i < mMovieIds.length; i++) {

                    Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                            .appendPath(mMovieIds[i])
                            .appendPath(REVIEWS)
                            .appendQueryParameter(API_KEY_PARAM, app_key)
                            .build();

                    URL url = new URL(builtUri.toString());

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = null;
                    try {
                        inputStream = urlConnection.getInputStream();
                    } catch (Exception e) {
                    }

                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        continue;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        continue;
                    }

                    inserted = inserted + getReviewsDataFromJson(buffer.toString());

                }//end of for

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error " + e.getMessage(), e);
                e.printStackTrace();

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            Log.d(LOG_TAG, "Movie Reviews Data Loading Task Complete. " + inserted + " rows inserted");
            return null;
        }


        private int getReviewsDataFromJson(String moviesJsonStr)
                throws JSONException {

            final String OWM_MOVIE_ID = "id";
            final String OWM_RESULTS_ARRAY = "results";
            final String OWM_REVIEW_ID = "id";
            final String OWM_AUTHOR = "author";
            final String OWM_CONTENT = "content";
            final String OWM_URL = "url";
            int inserted = 0;

            try {
                JSONObject moviesJson = new JSONObject(moviesJsonStr);
                Integer movieId = moviesJson.getInt(OWM_MOVIE_ID);

                JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS_ARRAY);
                Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject movieJSONObject = moviesArray.getJSONObject(i);

                    String reviewId = movieJSONObject.getString(OWM_REVIEW_ID);
                    String author = movieJSONObject.getString(OWM_AUTHOR);
                    String content = movieJSONObject.getString(OWM_CONTENT);
                    String url = movieJSONObject.getString(OWM_URL);


                    ContentValues reviewValues = new ContentValues();

                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, reviewId);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, author);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, content);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_URL, url);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_DATE, mDayTime.setJulianDay(mJulianStartDay));

                    cVVector.add(reviewValues);
                }

                // add to database
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    inserted = mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return inserted;
        }
    }

    private void deleteOldData() {

        String[] favoriteMovieIds = Utility.loadFavoriteMovieIds(mContext);
        String criteriaString = null;

        if (favoriteMovieIds != null && favoriteMovieIds.length > 0) {
            String favoriteMovieIdsString = Utility.argsArrayToString(favoriteMovieIds);
            criteriaString = MovieEntry.COLUMN_MOVIE_ID + " NOT IN (" + favoriteMovieIdsString + ")";
        }

        // delete old data so we don't build up an endless history
        mContext.getContentResolver().delete(MovieContract.ReviewEntry.CONTENT_URI,
                criteriaString,
                null);

        mContext.getContentResolver().delete(TrailerEntry.CONTENT_URI,
                criteriaString,
                null);

        mContext.getContentResolver().delete(MovieEntry.CONTENT_URI,
                criteriaString,
                null);

    }
}


