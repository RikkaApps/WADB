package moe.haruue.wadb.util;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalPaddingDecoration extends RecyclerView.ItemDecoration {

    private int mPadding;

    public VerticalPaddingDecoration(Context context) {
        this(context, 8);
    }

    public VerticalPaddingDecoration(Context context, int paddingDp) {
        this.mPadding = Math.round(paddingDp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getAdapter() == null) {
            return;
        }
        int position = parent.getChildAdapterPosition(view);
        int count = parent.getAdapter().getItemCount();
        if (position == 0) {
            outRect.top = mPadding;
        } else if (position == count - 1) {
            outRect.bottom = mPadding;
        }
    }
}

