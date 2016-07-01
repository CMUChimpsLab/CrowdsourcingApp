package com.dhchoi.crowdsourcingapp.crowdactivity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.fragments.CrowdActivityFragment;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CrowdActivityItem} and makes a call to the
 * specified {@link CrowdActivityFragment's OnActivityListFragmentInteractionListener}.
 */
public class CrowdActivityListViewAdapter extends RecyclerView.Adapter<CrowdActivityListViewAdapter.ViewHolder> {

    private final List<CrowdActivityItem> mItems;
    //private final CrowdActivityFragment.OnActivityListFragmentInteractionListener mListener;

    public CrowdActivityListViewAdapter(List<CrowdActivityItem> items) {
        mItems = items;
        //mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_crowd_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mIdView.setText(mItems.get(position).id);
        holder.mContentView.setText(mItems.get(position).content);
        holder.mDetailsView.setText(mItems.get(position).details);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onActivityListFragmentInteraction(holder.mItem);
//                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CrowdActivityItem mItem;
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mDetailsView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            mDetailsView = (TextView) view.findViewById(R.id.details);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
