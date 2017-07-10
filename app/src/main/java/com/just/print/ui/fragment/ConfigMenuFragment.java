package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.M2M_MenuPrint;
import com.just.print.db.bean.Menu;
import com.just.print.db.bean.Printer;
import com.just.print.db.dao.CategoryDao;
import com.just.print.db.dao.M2M_MenuPrintDao;
import com.just.print.db.dao.MenuDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.db.expand.State;
import com.just.print.ui.holder.AddPrintViewHolder;
import com.just.print.ui.holder.ConfigCategoryViewHolder;
import com.just.print.ui.holder.ConfigMenuViewHolder;
import com.stupid.method.adapter.IXOnItemClickListener;
import com.stupid.method.adapter.IXOnItemLongClickListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XGetValueByView;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.List;

import static android.content.DialogInterface.OnClickListener;

/**
 * Created by wangx on 2016/11/2.
 */
public class ConfigMenuFragment extends BaseFragment implements IXOnItemLongClickListener {

    XAdapter2<Category> categoryXAdapter;
    @XViewByID(R.id.lvCategory)
    ListView lvCategory;
    private Category mCategory;
    @XViewByID(R.id.tvMenuTitle)
    private TextView tvMenuTitle;
    @XViewByID(R.id.lvMenu)
    private ListView lvMenu;
    private XAdapter2<Menu> menuXAdapter;
    IXOnItemClickListener categoryAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            switch (view.getId()) {
                case android.R.id.content:
                    onChangeCategory(categoryXAdapter.get(i));
                    break;
                case R.id.delCategory:
                    /**
                     * 删除菜品种类
                     */
                    Category cag = categoryXAdapter.get(i);
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
                case R.id.delMenu:
                    //menuXAdapter.get(i).logicDelete();
                    /*
                    M2M_MenuPrintDao m2mDao = getDaoMaster().newSession().getM2M_MenuPrintDao();
                    List<M2M_MenuPrint> m2mList = m2mDao._queryMenu_M2M_MenuPrintList(menuXAdapter.get(i).getID());
                    for(M2M_MenuPrint tmp:m2mList){
                        m2mDao.delete(tmp);
                    }
                    */
                    /*
                    List<M2M_MenuPrint> m2mlist=
                        menuXAdapter.get(i).getM2M_MenuPrintList();
                    for (M2M_MenuPrint m2m:m2mlist){
                        m2m.delete();
                    }
                    */
                    M2M_MenuPrintDao m2mDao = getDaoMaster().newSession().getM2M_MenuPrintDao();
                    List<M2M_MenuPrint> m2mList = menuXAdapter.get(i).getM2M_MenuPrintList();

                    for(M2M_MenuPrint m2m:m2mList) {
                        m2m.delete();
                    }
                    menuDao.delete(menuXAdapter.get(i));
                    loadMenu();
                    break;
            }
        }
    };
    private void onChangeCategory(Category category) {
        mCategory = category;
        if (category != null) {
            tvMenuTitle.setText(category.getCname());
            loadMenu();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.config_menu_fragment;
    }

    MenuDao menuDao;


    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        categoryXAdapter = new XAdapter2<Category>(getContext(), ConfigCategoryViewHolder.class);
        categoryXAdapter.setClickItemListener(categoryAdapterClick);
        categoryXAdapter.setLongClickItemListener(this);
        lvCategory.setAdapter(categoryXAdapter);
        menuXAdapter = new XAdapter2<Menu>(getContext(), ConfigMenuViewHolder.class);
        menuXAdapter.setClickItemListener(categoryAdapterClick);
        lvMenu.setAdapter(menuXAdapter);
        menuDao = getDaoMaster().newSession().getMenuDao();
        loadCategory();
        loadMenu();
    }

    @XClick({R.id.showCategory})
    private void showCategory() {
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        findViewById(R.id.categoryLayout).setVisibility(View.VISIBLE);
    }

    @XViewByID(R.id.addPrintListView)
    ListView addPrintListView;

    @XClick({R.id.showMenu})
    private void showMenu() {
        findViewById(R.id.menuLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
        ((EditText)findViewById(R.id.etMenuID)).setText("");
        ((EditText)findViewById(R.id.etMenu)).setText("");
        addPrints = new ArrayList<Printer>(1);
        addPrintsAdapter = new XAdapter2<Printer>(getContext(), addPrints, AddPrintViewHolder.class);
        addPrintListView.setAdapter(addPrintsAdapter);
        addPrintsAdapter.setClickItemListener(new IXOnItemClickListener() {
            @Override
            public void onClickItem(View view, int i) {
                addPrints.remove(i);
                addPrintsAdapter.notifyDataSetChanged();
            }
        });
    }

    List<Printer> addPrints;
    XAdapter2<Printer> addPrintsAdapter;

    @XClick({R.id.addMenu})
    private void addMenu(@XGetValueByView(fromId = R.id.etMenuID) TextView mid,
                         @XGetValueByView(fromId = R.id.etMenu) TextView mname,
                         @XGetValueByView(fromId = R.id.selectPrint) TextView print
    ) {

        if (mCategory == null) {
            showToast("请选选择类别");
            return;
        }
        Menu menu = menuDao.load(mid.getText().toString());
        if (mid.getText().length() == 0 || mname.getText().length() == 0) {
            showToast("请输入" + (mid.getText().length() == 0 ? "编号" : "名称"));
            return;
        }
        if (addPrints == null || addPrints.size() == 0) {
            showToast("请选择打印机");
            return;
        }
        if (menu != null && menu.getState() == State.def) {

            showToast("该编号已存在");
            return;
        }
        /*
        if (menu != null && menu.getState() == State.delete){
            menu.setID(mid.getText().toString().trim());
            menu.setState(State.def);
            menu.setMname(mname.getText().toString().trim());
            menu.setCid(mCategory.getId());
            menu.updateAndUpgrade();
        }
        */
        menu = new Menu();
        menu.setID(mid.getText().toString().trim());
        menu.setState(State.def);
        menu.setMname(mname.getText().toString().trim());
        menu.setCid(mCategory.getId());
        menuDao.insert(menu);
        menu.updateAndUpgrade();
        M2M_MenuPrintDao m2mDao = getDaoMaster().newSession().getM2M_MenuPrintDao();
        for (Printer p : addPrints) {
            M2M_MenuPrint m2m = new M2M_MenuPrint();
            m2m.setMenu(menu);
            m2m.setPrint(p);
            m2mDao.insertOrReplace(m2m);
        }
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        loadMenu();

    }

    /**
     * 添加类别
     */
    @XClick({R.id.addCategory})
    private void addCategory(@XGetValueByView(fromId = R.id.tvCategory) TextView cname) {
        if (cname.getText().length() == 0) {
            showToast("请输入类别");
            return;
        }

        Category cat = null;
        if (mModifyCategoryTemp == null)
            cat = new Category();
        else cat = mModifyCategoryTemp;
        cat.setCname(cname.getText().toString().trim());
        cat.setState(State.def);
        getDaoMaster().newSession().getCategoryDao().insertOrReplace(cat);
        cat.updateAndUpgrade();
        loadCategory();
        cname.setText("");
        mModifyCategoryTemp = null;
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
    }

    @XClick({R.id.selectPrint})
    private void selectPrint(final TextView view) {
        final List<Printer> data = DaoExpand.queryNotDeleteAll(getDaoMaster().newSession().getPrinterDao());
        data.removeAll(addPrints);
        if (data.size() == 0) {
            showToast("请先添加打印机");
            return;
        }
        if (data.size() == 0) {
            showToast("没有可选的打印机了");
            return;
        }
        final CharSequence[] items = new CharSequence[data.size()];
        for (int i = 0, s = data.size(); i < s; i++) {
            Printer p = data.get(i);
            items[i] = p.getPname() + "_" + p.getIp();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("请选择打印机");
        builder.setSingleChoiceItems(items, -1, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addPrints.add(data.get(which));
                addPrintsAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @XClick({R.id.cancel, R.id.cancel1})
    private void cancel() {
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
    }

    private void loadCategory() {

        List<Category> list = DaoExpand.queryNotDeleteAllQuery(getDaoMaster().newSession().getCategoryDao()).orderAsc(CategoryDao.Properties.Cname).list();
        categoryXAdapter.setData(list);

         categoryXAdapter.notifyDataSetChanged();
        menuXAdapter.clear();
        if (categoryXAdapter.size() > 0)
            onChangeCategory(categoryXAdapter.get(0));
        else
            menuXAdapter.notifyDataSetChanged();
    }

    private void loadMenu() {
        if (mCategory != null) {
            List<Menu> menuList = DaoExpand.queryMenuByCategory(getDaoMaster().newSession().getMenuDao(), mCategory);
            menuXAdapter.setData(menuList);
            menuXAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public String toString() {
        return "菜单管理";
    }

    Category mModifyCategoryTemp;

    @Override
    public boolean onLongClickItem(View view, int i) {
        mModifyCategoryTemp = categoryXAdapter.get(i);
        showCategory();
        TextView tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvCategory.setText(mModifyCategoryTemp.getCname());

        return true;
    }
}
