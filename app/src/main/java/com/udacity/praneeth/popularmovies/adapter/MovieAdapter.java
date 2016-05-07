package com.udacity.praneeth.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.praneeth.popularmovies.MainActivityFragment;
import com.udacity.praneeth.popularmovies.R;
import com.udacity.praneeth.popularmovies.Utility;

/**
 * Created by Praneeth on 4/21/2016.
 */
public class MovieAdapter extends CursorAdapter {
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.movie_imageview);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.imageview_fragment_main, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String posterPath = cursor.getString(MainActivityFragment.COL_POSTER_PATH);
        String moviePoster = Utility.getImageURL(posterPath);

        Picasso.with(context).load(moviePoster).into(viewHolder.imageView);

    }
}
