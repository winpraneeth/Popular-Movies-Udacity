package com.udacity.praneeth.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by Praneeth on 4/21/2016.
 */
public class Utility {

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String IMAGE_SIZE = "w342";
    private static final String PATH_SEPARATOR = "/";

    public static Uri buildPosterUri(String poster_path) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String imageSize = "w154";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(imageSize)
                .appendEncodedPath(poster_path)
                .build();

        return builtUri;
    }

    public static void setDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount()));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }


    public static String getSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOption = prefs.getString(context.getString(R.string.key_sort), context.getString(R.string.sort_value_popular));

        return sortOption;
    }

    public static String getImageURL(String imagePath) {
        StringBuilder imageURL = new StringBuilder();

        imageURL.append(IMAGE_BASE_URL);
        imageURL.append(IMAGE_SIZE);
        imageURL.append(PATH_SEPARATOR);
        imageURL.append(imagePath);

        return imageURL.toString();
    }

    public static String argsArrayToString(String[] args) {
        StringBuilder argsBuilder = new StringBuilder();

        final int argsCount = args.length;
        for (int i = 0; i < argsCount; i++) {
            argsBuilder.append(args[i]);

            if (i < argsCount - 1) {
                argsBuilder.append(",");
            }
        }

        return argsBuilder.toString();
    }

    public static boolean isMovieIdFavorite(String[] favoriteMovieIds, String movieId) {
        boolean result = false;

        if (favoriteMovieIds == null || favoriteMovieIds.length == 0) return result;

        for (int i = 0; i < favoriteMovieIds.length; i++) {
            if (movieId.trim().equals(favoriteMovieIds[i].trim())) {
                result = true;
                break;
            }
        }

        return result;
    }

    public static String[] loadFavoriteMovieIds(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favoritMovieIdsSet = prefs.getStringSet(DetailActivityFragment.FAVORITE_MOVIE_IDS_SET_KEY, null);

        if (favoritMovieIdsSet != null) {
            String[] array = new String[favoritMovieIdsSet.size()];

            Iterator<String> movieIdsIter = favoritMovieIdsSet.iterator();

            int i = 0;
            while (movieIdsIter.hasNext()) {
                array[i] = movieIdsIter.next();
                i = i + 1;
            }
            return array;
        }

        return null;
    }

}
