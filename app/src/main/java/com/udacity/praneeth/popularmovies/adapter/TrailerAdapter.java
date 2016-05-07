package com.udacity.praneeth.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.udacity.praneeth.popularmovies.DetailActivityFragment;
import com.udacity.praneeth.popularmovies.R;

/**
 * Created by Praneeth on 4/22/2016.
 */
public class TrailerAdapter extends CursorAdapter {
    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.trailer_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String trailer = cursor.getString(DetailActivityFragment.COL_NAME);
        viewHolder.trailerTextView.setText(trailer);

    }

    public static class ViewHolder {
        public static TextView trailerTextView;

        public ViewHolder(View view) {
            trailerTextView = (TextView) view.findViewById(R.id.trailer_textView);
        }
    }


}
