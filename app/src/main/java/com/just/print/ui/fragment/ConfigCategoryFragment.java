package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.just.print.R;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Menu;
import com.just.print.db.dao.CategoryDao;
import com.just.print.db.dao.MenuDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.db.expand.State;
import com.just.print.ui.holder.ConfigCategoryViewHolder;
import com.just.print.util.L;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.List;

/**
 * Created by wangx on 2016/11/2.
 */
public class ConfigCategoryFragment extends BaseFragment implements OnClickItemListener {
    private static final String TAG="ConfigCategoryFragment";
    @XViewByID(R.id.lvCategory)
    ListView lvCategory;
    XAdapter2<Category> categoryXAdapter;

    @Override
    public String toString() {
        return "类目管理";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.config_category_fragment;
    }

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        categoryXAdapter = new XAdapter2<Category>(getContext(), ConfigCategoryViewHolder.class);
        categoryXAdapter.setClickItemListener(this);
        lvCategory.setAdapter(categoryXAdapter);
        loadCategory();
    }

    private void loadCategory() {

        List<Category> list = DaoExpand.queryNotDeleteAllQuery(getDaoMaster().newSession().getCategoryDao()).orderAsc(CategoryDao.Properties.Cname).list();
        categoryXAdapter.setData(list);

        categoryXAdapter.notifyDataSetChanged();
        getEventBus().post(OrderIdentifierFragment.EVENT_ADD_MENU);
    }

    /**
     * 添加类别
     */
    @XClick({R.id.addCategory})
    private void addCategory(@XGetValueByView(fromId = R.id.tvCategory) String cname) {
        Category cat = new Category();
        cat.setCname(cname);
        cat.setState(State.def);
        getDaoMaster().newSession().getCategoryDao().insert(cat);
        cat.updateAndUpgrade();
        loadCategory();
    }

    @Override
    public void onClickItem(View view, int i) {
        L.d(TAG,"onClickItem");
        switch (view.getId()) {

            case android.R.id.content:

                break;
            /**
             * 删除菜品种类
             */
            case R.id.delCategory:
                Category cag = categoryXAdapter.get(i);
                MenuDao menuDao = getDaoMaster().newSession().getMenuDao();
                long cid = cag.getId();
                List<Menu> mList = menuDao._queryCategory_MenuList(cid);
                for(Menu menu:mList) {
                    /**
                     * 删除菜单-打印机映射
                     */
                    List<M2M_MenuPrint> m2mList = menu.getM2M_MenuPrintList();
                    for(M2M_MenuPrint m2m:m2mList) {
                        m2m.delete();
                    }

                    /**
                     * 删除菜单
                     */
                    menu.delete();
                }
                categoryXAdapter.get(i).logicDelete();

                loadCategory();
                break;
        }
    }
}
