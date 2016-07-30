package moe.haruue.wadb.ui.item;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class MenuItemModel {

    public final static int VIEW_TYPE_MENU_CLASS = 21355;
    public final static int VIEW_TYPE_MENU_ITEM = 51214;

    private int viewType;

    public int getViewType() {
        return viewType;
    }

    private String title;
    private String subTitle;
    private boolean hasCheckbox;
    private boolean isChecked;

    public MenuItemModel(String title) {
        viewType = VIEW_TYPE_MENU_CLASS;
        this.title = title;
    }

    public MenuItemModel(String title, String subTitle, boolean hasCheckbox) {
        viewType = VIEW_TYPE_MENU_ITEM;
        this.title = title;
        this.subTitle = subTitle;
        this.hasCheckbox = hasCheckbox;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public boolean isHasCheckbox() {
        return hasCheckbox;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
