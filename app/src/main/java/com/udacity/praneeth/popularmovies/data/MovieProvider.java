package com.udacity.praneeth.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.udacity.praneeth.popularmovies.data.MovieContract.MovieEntry;
import com.udacity.praneeth.popularmovies.data.MovieContract.ReviewEntry;
import com.udacity.praneeth.popularmovies.data.MovieContract.TrailerEntry;

/**
 * Created by Praneeth on 4/18/2016.
 */
public class MovieProvider extends ContentProvider {

        // The URI Matcher used by this content provider.
        private static final UriMatcher sUriMatcher = buildUriMatcher();
        private MovieDbHelper mOpenHelper;

        // /movie
        static final int MOVIE = 100;
        // /trailer
        static final int TRAILER = 200;
        // /trailer/*
        static final int TRAILER_WITH_MOVIE_ID = 201;

        // /review
        static final int REVIEW = 300;
        // /review/*
        static final int REVIEW_WITH_MOVIE_ID = 301;

        private static final SQLiteQueryBuilder sTrailerByMovieIdQueryBuilder;
        private static final SQLiteQueryBuilder sReviewByMovieIdQueryBuilder;

        static{
            sTrailerByMovieIdQueryBuilder = new SQLiteQueryBuilder();
            sReviewByMovieIdQueryBuilder = new SQLiteQueryBuilder();

            //This is an inner join which looks like
            //trailer INNER JOIN movie    ON trailer.movie_id    = movie.movie_id
            //review  INNER JOIN movie    ON review.movie_id    = movie.movie_id
            sTrailerByMovieIdQueryBuilder.setTables(
                    TrailerEntry.TABLE_NAME + " INNER JOIN " +
                            MovieEntry.TABLE_NAME +
                            " ON " + TrailerEntry.TABLE_NAME +
                            "." + TrailerEntry.COLUMN_MOVIE_ID +
                            " = " + MovieEntry.TABLE_NAME +
                            "." + MovieEntry.COLUMN_MOVIE_ID
            );

            sReviewByMovieIdQueryBuilder.setTables(
                    ReviewEntry.TABLE_NAME + " INNER JOIN " +
                            MovieEntry.TABLE_NAME +
                            " ON " + ReviewEntry.TABLE_NAME +
                            "." + ReviewEntry.COLUMN_MOVIE_ID +
                            " = " + MovieEntry.TABLE_NAME +
                            "." + MovieEntry.COLUMN_MOVIE_ID
            );

        }

        //movie.movie_id = ?
        private static final String sMovieIdSelection =
                MovieEntry.TABLE_NAME+
                        "." + MovieEntry.COLUMN_MOVIE_ID + " = ? ";

        //trailer.movie_id = ?
        private static final String sMovieTrailerMovieIdSelection =
                TrailerEntry.TABLE_NAME+
                        "." + TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

        //review.movie_id = ?
        private static final String sMovieReviewMovieIdSelection =
                ReviewEntry.TABLE_NAME+
                        "." + ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

        static UriMatcher buildUriMatcher() {
            // 1) The code passed into the constructor represents the code to return for the root
            // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
            final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            final String authority = MovieContract.CONTENT_AUTHORITY;

            // 2) Use the addURI function to match each of the types.  Use the constants from
            // WeatherContract to help define the types to the UriMatcher.
            sURIMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
            sURIMatcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILER);
            sURIMatcher.addURI(authority, MovieContract.PATH_TRAILER + "/#", TRAILER_WITH_MOVIE_ID);
            sURIMatcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEW);
            sURIMatcher.addURI(authority, MovieContract.PATH_REVIEW + "/#", REVIEW_WITH_MOVIE_ID);

            // 3) Return the new matcher!
            return sURIMatcher;
        }

        @Override
        public boolean onCreate() {
            mOpenHelper = new MovieDbHelper(getContext());
            return true;
        }

        @Override
        public String getType(Uri uri) {

            // Use the Uri Matcher to determine what kind of URI this is.
            final int match = sUriMatcher.match(uri);

            switch (match) {
                case MOVIE:
                    return MovieEntry.CONTENT_TYPE;
                case TRAILER:
                    return TrailerEntry.CONTENT_TYPE;
                case REVIEW:
                    return ReviewEntry.CONTENT_TYPE;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        private Cursor getTrailerByMovieId(Uri uri, String[] projection, String sortOrder) {
            String movieId = TrailerEntry.getMovieIdFromUri(uri);

            String[] selectionArgs = new String[]{movieId};
            String selection = sMovieIdSelection;

            return sTrailerByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        }

        private Cursor getReviewByMovieId(Uri uri, String[] projection, String sortOrder) {
            String movieId = ReviewEntry.getMovieIdFromUri(uri);

            String[] selectionArgs = new String[]{movieId};
            String selection = sMovieIdSelection;

            Cursor cursor = sReviewByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );

            return cursor;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                            String sortOrder) {
            // Here's the switch statement that, given a URI, will determine what kind of request it is,
            // and query the database accordingly.
            Cursor retCursor;
            switch (sUriMatcher.match(uri)) {
                // "movie"
                case MOVIE: {
                    retCursor = mOpenHelper.getReadableDatabase().query(
                            MovieEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                    break;
                }
                // "trailer"
                case TRAILER_WITH_MOVIE_ID: {
                    retCursor = getTrailerByMovieId(uri, projection, sortOrder);
                    break;
                }

                // "review"
                case REVIEW_WITH_MOVIE_ID: {
                    retCursor = getReviewByMovieId(uri, projection, sortOrder);
                    break;
                }

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
            return retCursor;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            Uri returnUri;

            switch (match) {
                case MOVIE: {
                    normalizeDate(values);
                    long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                    if ( _id > 0 )
                        returnUri = MovieEntry.buildMovieUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case TRAILER: {
                    normalizeDate(values);
                    long _id = db.insert(TrailerEntry.TABLE_NAME, null, values);
                    if ( _id > 0 )
                        returnUri = TrailerEntry.buildMovieTrailerUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
                case REVIEW: {
                   normalizeDate(values);
                    long _id = db.insert(ReviewEntry.TABLE_NAME, null, values);
                    if ( _id > 0 )
                        returnUri = ReviewEntry.buildMovieReviewsUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return returnUri;
        }

        @Override
        public int update(
                Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            // Student: This is a lot like the delete function.  We return the number of rows impacted
            // by the update.
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            int rowsUpdated;

            switch (match) {
                case MOVIE:
                   normalizeDate(values);
                    rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                case TRAILER:
                    rowsUpdated = db.update(TrailerEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                case REVIEW:
                    rowsUpdated = db.update(ReviewEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }

        @Override
        public int bulkInsert(Uri uri, ContentValues[] values) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case MOVIE:
                    db.beginTransaction();
                    int returnCount = 0;
                    try {
                        for (ContentValues value : values) {
                           normalizeDate(value);
                            long _id = db.insert(MovieEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                    getContext().getContentResolver().notifyChange(uri, null);
                    return returnCount;
                case TRAILER:
                    db.beginTransaction();
                    returnCount = 0;
                    try {
                        for (ContentValues value : values) {
                           normalizeDate(value);
                            long _id = db.insert(TrailerEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                    getContext().getContentResolver().notifyChange(uri, null);
                    return returnCount;
                case REVIEW:
                    db.beginTransaction();
                    returnCount = 0;
                    int valuesLength = values.length;
                    try {
                        for (ContentValues value : values) {
                            normalizeDate(value);
                            long _id = db.insert(ReviewEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                    getContext().getContentResolver().notifyChange(uri, null);
                    return returnCount;

                default:
                    return super.bulkInsert(uri, values);
            }
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {

            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);

            int rowsDeleted;
            // this makes delete all rows return the number of rows deleted
            if ( null == selection ) selection = "1";

            switch (match) {
                case MOVIE: {
                    rowsDeleted = db.delete(
                            MovieEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                case TRAILER: {
                    rowsDeleted = db.delete(
                            TrailerEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                case REVIEW: {
                    rowsDeleted = db.delete(
                            ReviewEntry.TABLE_NAME, selection, selectionArgs);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            // Student: A null value deletes all rows.  In my implementation of this, I only notified
            // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
            // is null.
            // Oh, and you should notify the listeners here.

            if (rowsDeleted != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            // Student: return the actual rows deleted
            return rowsDeleted;
        }

    private void normalizeDate(ContentValues values) {
        // normalize the date value

        if (values.containsKey(MovieEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(MovieEntry.COLUMN_DATE);
            values.put(MovieEntry.COLUMN_DATE, MovieContract.normalizeDate(dateValue));
        }
    }


        // You do not need to call this method. This is a method specifically to assist the testing
        // framework in running smoothly. You can read more at:
        // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
        @Override
        @TargetApi(11)
        public void shutdown() {
            mOpenHelper.close();
            super.shutdown();
        }


    }
