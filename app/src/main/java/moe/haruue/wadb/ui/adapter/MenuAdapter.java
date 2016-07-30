package moe.haruue.wadb.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import moe.haruue.wadb.R;
import moe.haruue.wadb.ui.item.MenuItemModel;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class MenuAdapter extends RecyclerArrayAdapter<MenuItemModel> {

    public MenuAdapter(Context context) {
        super(context);
    }

    @Override
    public int getViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case MenuItemModel.VIEW_TYPE_MENU_CLASS:
                return new MenuClassViewHolder(parent);
            case MenuItemModel.VIEW_TYPE_MENU_ITEM:
                return new MenuItemViewHolder(parent);
            default:
                return null;
        }
    }

    class MenuItemViewHolder extends BaseViewHolder<MenuItemModel> {

        private TextView titleView;
        private TextView subtitleView;
        private CheckBox checkBox;

        public MenuItemViewHolder(View itemView) {
            super((ViewGroup) itemView, R.layout.item_menu_item);
            titleView = $(R.id.menu_item_title);
            subtitleView = $(R.id.menu_item_subtitle);
            checkBox = $(R.id.menu_item_checkbox);
        }

        @Override
        public void setData(MenuItemModel data) {
            super.setData(data);
            titleView.setText(data.getTitle());
            subtitleView.setText(data.getSubTitle());
            checkBox.setVisibility(data.isHasCheckbox() ? View.VISIBLE : View.GONE);
            checkBox.setChecked(data.isChecked());
        }
    }

    class MenuClassViewHolder extends BaseViewHolder<MenuItemModel> {

        private TextView titleView;

        public MenuClassViewHolder(View itemView) {
            super((ViewGroup) itemView, R.layout.item_menu_class);
            titleView = $(R.id.menu_class_title);
        }

        @Override
        public void setData(MenuItemModel data) {
            super.setData(data);
            titleView.setText(data.getTitle());
        }
    }

}
