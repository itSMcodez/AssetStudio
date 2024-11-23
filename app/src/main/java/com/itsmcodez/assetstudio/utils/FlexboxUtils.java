package com.itsmcodez.assetstudio.utils;

import android.view.ViewTreeObserver;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.FlexLine;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.List;

/**
 * @author itSMcodez
 */
public class FlexboxUtils {

    public static <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>>
            ViewTreeObserver.OnGlobalLayoutListener
                    createGlobalLayoutListenerToDistributeFlexboxItemsEvenly(
                            final AdapterProvider<T> adapterProvider,
                            final LayoutManagerProvider layoutManagerProvider,
                            final FillDiff<T> fillDiff) {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                T adapter = adapterProvider.provide();
                if (adapter == null) {
                    return;
                }

                FlexboxLayoutManager layoutManager = layoutManagerProvider.provide();
                if (layoutManager == null) {
                    return;
                }

                List<FlexLine> flexLines = layoutManager.getFlexLinesInternal();
                int columns = flexLines.isEmpty() ? 0 : flexLines.get(0).getItemCount();
                if (columns == 0) {
                    return;
                }

                int itemCount = adapter.getItemCount();
                int rows = (int) Math.ceil((float) itemCount / columns);
                if (itemCount % columns == 0) {
                    return;
                }

                int diff = rows * columns - itemCount;
                if (diff <= 0) {
                    return;
                }

                fillDiff.fill(adapter, diff);
            }
        };
    }

    public interface AdapterProvider<T> {
        T provide();
    }

    public interface LayoutManagerProvider {
        FlexboxLayoutManager provide();
    }

    public interface FillDiff<T> {
        void fill(T adapter, int diff);
    }
}
