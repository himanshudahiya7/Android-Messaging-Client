package com.aashishkumar.androidproject.connections;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aashishkumar.androidproject.R;
import com.aashishkumar.androidproject.connections.SearchResultFragment.OnSearchListFragmentInteractionListener;
import com.aashishkumar.androidproject.models.Connection;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Connection} and makes a call to the
 * specified {@link OnSearchListFragmentInteractionListener}.
 *
 * @author Robert Bohlman
 * @author Hien Doan
 */
public class MySearchResultRecyclerViewAdapter extends RecyclerView.Adapter<MySearchResultRecyclerViewAdapter.ViewHolder> {

    private final List<Connection> mValues;
    private final OnSearchListFragmentInteractionListener mListener;

    public MySearchResultRecyclerViewAdapter(List<Connection> items, OnSearchListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_searchresult, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mUsername.setText(mValues.get(position).getUsername());
        holder.mFullName.setText(mValues.get(position).getFullName());

//        if (mValues.get(position).getVerified() == 0) {
//            holder.mUsername.setTextColor(Color.GRAY);
//        } else {
//            holder.mUsername.setTextColor(Color.BLUE);
//        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSearchListFragmentInteraction(holder.mItem);
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
        public final TextView mUsername;
        public final TextView mFullName;
        public Connection mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsername = (TextView) view.findViewById(R.id.username_searchresult_frag);
            mFullName = (TextView) view.findViewById(R.id.fullname_searchresult_frag);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsername.getText() + "'";
        }
    }
}
