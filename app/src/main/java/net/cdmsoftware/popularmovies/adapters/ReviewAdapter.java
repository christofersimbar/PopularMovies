package net.cdmsoftware.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cdmsoftware.popularmovies.R;
import net.cdmsoftware.popularmovies.Utilities;
import net.cdmsoftware.popularmovies.models.Review;

import java.util.List;

import static android.R.attr.defaultHeight;
import static android.R.attr.maxHeight;

public class ReviewAdapter
        extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> mReviews;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final Button mReadMore;
        public final TextView mReviewAuthor;
        public final TextView mReviewContent;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mReadMore = (Button) view.findViewById(R.id.read_more);
            mReviewAuthor = (TextView) view.findViewById(R.id.review_author);
            mReviewContent = (TextView) view.findViewById(R.id.review_content);
        }

        public void toggleHeight(View view) {
            final int defaultHeight = view.getContext().getResources().getDimensionPixelSize(R.dimen.review_item_max_height);
            if (mReviewContent.getHeight() > defaultHeight) {
                mReviewContent.getLayoutParams().height = defaultHeight;
                mReadMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_keyboard_arrow_down_white_24dp);
            } else {
                mReviewContent.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                mReadMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_keyboard_arrow_up_white_24dp);
            }
        }

        ;
    }

    public ReviewAdapter(List<Review> items) {
        this.mReviews = items;
    }

    public void loadReviews(List<Review> items) {
        mReviews = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mReviewAuthor.setText(mReviews.get(position).getAuthor());
        holder.mReviewContent.setText(mReviews.get(position).getContent());

        holder.mReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.toggleHeight(view);
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.toggleHeight(view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mReviews != null) ? mReviews.size() : 0;
    }
}
