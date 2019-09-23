package com.just.print_night.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.just.print_night.R;
import com.just.print_night.app.AppData;
import com.just.print_night.app.Applic;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.db.bean.Category;
import com.just.print_night.db.bean.M2M_MenuPrint;
import com.just.print_night.db.bean.Menu;
import com.just.print_night.db.bean.Printer;
import com.just.print_night.db.dao.CategoryDao;
import com.just.print_night.db.dao.M2M_MenuPrintDao;
import com.just.print_night.db.dao.MenuDao;
import com.just.print_night.db.expand.DaoExpand;
import com.just.print_night.db.expand.State;
import com.just.print_night.ui.holder.AddPrintViewHolder;
import com.just.print_night.ui.holder.ConfigCategoryViewHolder;
import com.just.print_night.ui.holder.ConfigMenuViewHolder;
import com.stupid.method.adapter.IXOnItemClickListener;
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
public class ConfigMenuFragment extends BaseFragment{//} implements IXOnItemLongClickListener {

    private static Menu modifyingMenu = null;

    private Category modifyingCategory = null;

    XAdapter2<Category> categoryXAdapter;

    @XViewByID(R.id.lvCategory)
    ListView lvCategory;

    private Category mCategory;

    @XViewByID(R.id.tvMenuTitle)
    private TextView tvMenuTitle;

    @XViewByID(R.id.lvMenu)
    private ListView lvMenu;

    MenuDao menuDao;

    private XAdapter2<Menu> menuXAdapter;
    IXOnItemClickListener categoryAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            switch (view.getId()) {
                case android.R.id.content:
                    onChangeCategory(categoryXAdapter.get(i));
                    break;

                case R.id.modifyCategory:
                    //show up hte modification dialog.
                    modifyingCategory = categoryXAdapter.get(i);
                    showCategory();
                    TextView tvCategory = (TextView) findViewById(R.id.tvCategory);
                    tvCategory.setText(modifyingCategory.getCname());
                    TextView displayIdx = (TextView) findViewById(R.id.displayIdx);
                    displayIdx.setText(String.valueOf(modifyingCategory.getDisplayIdx()));

                    break;
                case R.id.delCategory:
                    Category cag = categoryXAdapter.get(i);
                    long cid = cag.getId();
                    List<Menu> mList = menuDao._queryCategory_MenuList(cid);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure to delete \"" + cag.getCname() + "\"?")
                            .setTitle("Notice");
                    builder.setNegativeButton("Delete", new CategoryDeleteListener(mList, cag));
                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();

                    break;
                case R.id.modifyMenu:
                    findViewById(R.id.categoryLayout).setVisibility(View.GONE);
                    findViewById(R.id.menuLayout).setVisibility(View.VISIBLE);

                    modifyingMenu = menuXAdapter.get(i);
                    ((EditText)findViewById(R.id.etMenuID)).setText(menuXAdapter.get(i).getID());
                    ((EditText)findViewById(R.id.etMenu)).setText(menuXAdapter.get(i).getMname());
                    Double price = menuXAdapter.get(i).getPrice();
                    ((EditText)findViewById(R.id.price)).setText(price == null ? "" : price.toString());

                    addPrints = new ArrayList<Printer>();
                    for(M2M_MenuPrint m2m_p : menuXAdapter.get(i).getM2M_MenuPrintList()){
                        addPrints.add(m2m_p.getPrint());
                    }

                    addPrintsAdapter = new XAdapter2<Printer>(getContext(), addPrints, AddPrintViewHolder.class);
                    addPrintListView.setAdapter(addPrintsAdapter);
                    addPrintsAdapter.setClickItemListener(new IXOnItemClickListener() {
                        @Override
                        public void onClickItem(View view, int i) {
                            addPrints.remove(i);
                            addPrintsAdapter.notifyDataSetChanged();
                        }
                    });
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
                    M2M_MenuPrintDao m2mDao = Applic.app.getDaoMaster().newSession().getM2M_MenuPrintDao();

                    new AlertDialog.Builder(getActivity())
                            .setMessage("Are you sure to delete \"" + menuXAdapter.get(i).getMname() + "\"?")
                            .setTitle("Notice")
                            .setNegativeButton("Delete", new MenuDeleteListener(menuXAdapter.get(i)))
                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();

                    break;
            }
        }
    };

    private class CategoryDeleteListener implements DialogInterface.OnClickListener{
        List<Menu> menus;
        Category category;
        public CategoryDeleteListener(List<Menu> menus, Category category){
            this.menus = menus;
            this.category = category;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            for(Menu menu:menus) {
                //delete menu-printer maps
                List<M2M_MenuPrint> m2mList = menu.getM2M_MenuPrintList();
                for(M2M_MenuPrint m2m:m2mList) {
                    m2m.delete();
                }
                // delete menu.
                menu.delete();
            }
            category.logicDelete();
            loadCategory();
        }
    }

    private class MenuDeleteListener implements DialogInterface.OnClickListener{
        Menu menu;
        public MenuDeleteListener(Menu menu){
            this.menu = menu;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            List<M2M_MenuPrint> m2mList = menu.getM2M_MenuPrintList();
            for(M2M_MenuPrint m2m:m2mList) {
                m2m.delete();
            }
            menuDao.delete(menu);
            loadMenu();
        }
    }

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

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();

        categoryXAdapter = new XAdapter2<Category>(getContext(), ConfigCategoryViewHolder.class);
        categoryXAdapter.setClickItemListener(categoryAdapterClick);
        //categoryXAdapter.setLongClickItemListener(this);
        lvCategory.setAdapter(categoryXAdapter);

        menuXAdapter = new XAdapter2<Menu>(getContext(), ConfigMenuViewHolder.class);
        menuXAdapter.setClickItemListener(categoryAdapterClick);
        lvMenu.setAdapter(menuXAdapter);

        menuDao = Applic.app.getDaoMaster().newSession().getMenuDao();

        loadCategory();
        loadMenu();
    }

    @XClick({R.id.showCategory})
    private void showCategory() {
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        findViewById(R.id.categoryLayout).setVisibility(View.VISIBLE);
        ((EditText)findViewById(R.id.tvCategory)).setText("");
        ((EditText)findViewById(R.id.displayIdx)).setText("");
    }

    @XViewByID(R.id.addPrintListView)
    ListView addPrintListView;

    @XClick({R.id.showMenu})
    private void showMenu() {
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
        findViewById(R.id.menuLayout).setVisibility(View.VISIBLE);

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
                         @XGetValueByView(fromId = R.id.price) TextView mprice,
                         @XGetValueByView(fromId = R.id.selectPrint) TextView print) {

        if (mCategory == null) {
            showToast("Please choose a category");
            return;
        }

        if (mid.getText().length() == 0 || mname.getText().length() == 0 || mprice.getText().length() == 0 ) {
            if(mid.getText().length() == 0) {
                showToast("Please input code");
            }else if(mname.getText().length() == 0){
                showToast("Please input name");
            }else if(mprice.getText().length() == 0){
                showToast("Please input price");
            }
            return;
        }
        Double price = 0.0;
        try{
            price =Double.valueOf(String.valueOf(mprice.getText()));
        } catch(Exception e){
            showToast("Please input valid price. e.g. 15.99");
            return;
        }
        //should allow dish without printer in mode 2
        if (addPrints == null || addPrints.size() == 0) {
            if(!AppData.isMode2()) {
                showToast("Please select printer");
                return;
            }
        }


        //filter out "is adding" and "code is ocupied" case.
        Menu menu = menuDao.load(mid.getText().toString());
        if (menu != null && menu.getState() == State.def && modifyingMenu == null) {
            showToast("the code is used already");
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

        if(menu == null){   //the code mapping to no record in db.
            menu = new Menu();
            menu.setID(mid.getText().toString().trim());
            menu.setState(State.def);
            menu.setMname(mname.getText().toString().trim());
            menu.setCid(mCategory.getId());
            menu.setPrice(price);
            menuDao.insert(menu);
        }else{              //the code is same with some record in db. means it's updating....
            menu.setID(mid.getText().toString().trim());
            menu.setState(State.def);
            menu.setMname(mname.getText().toString().trim());
            menu.setCid(mCategory.getId());
            menu.setPrice(price);
            menuDao.update(menu);
        }
        if(modifyingMenu != null && !modifyingMenu.getID().equals(menu.getID())){ //if it's modifying, and the code is changed to be same with some other existing one.
            menuDao.delete(modifyingMenu);
        }

        menu.updateAndUpgrade();
        M2M_MenuPrintDao m2mDao = Applic.app.getDaoMaster().newSession().getM2M_MenuPrintDao();
        if(modifyingMenu != null) {
            for (M2M_MenuPrint p : modifyingMenu.getM2M_MenuPrintList()) {
                m2mDao.delete(p);
            }
        }
        for (Printer p : addPrints) {
            M2M_MenuPrint m2m = new M2M_MenuPrint();
            m2m.setMenu(menu);
            m2m.setPrint(p);
            m2mDao.insertOrReplace(m2m);
        }
        AppData.updataeLastModifyTime(null);
        modifyingMenu = null;
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        loadMenu();
    }

    @XClick({R.id.addCategory})
    private void addCategory(@XGetValueByView(fromId = R.id.tvCategory) TextView cname,
                             @XGetValueByView(fromId = R.id.displayIdx) TextView displayIdx) {
        if (cname.getText().length() == 0) {
            showToast("Please input category");
            return;
        }

        String idxString = displayIdx.getText().toString();
        double idx = 0.0;
        if(idxString.length() > 0) {
            try {
                idx = Double.valueOf(idxString);
            } catch (Exception e) {
                showToast("Please input a valid number to indicate the display order");
                return;
            }
        }

        Category cat = null;
        if (modifyingCategory == null) {
            cat = new Category();
        }else {
            cat = modifyingCategory;
        }
        cat.setCname(cname.getText().toString().trim());
        cat.setDisplayIdx(idx);

        cat.setState(State.def);
        Applic.app.getDaoMaster().newSession().getCategoryDao().insertOrReplace(cat);
        cat.updateAndUpgrade();
        AppData.updataeLastModifyTime(null);
        loadCategory();
        cname.setText("");
        modifyingCategory = null;
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
    }

    @XClick({R.id.selectPrint})
    private void selectPrint(final TextView view) {
        final List<Printer> data = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getPrinterDao());
        data.removeAll(addPrints);
        if (data.size() == 0) {
            showToast("Please add printer first");
            return;
        }
        if (data.size() == 0) {
            showToast("No printer to select");
            return;
        }
        final CharSequence[] items = new CharSequence[data.size()];
        for (int i = 0, s = data.size(); i < s; i++) {
            Printer p = data.get(i);
            items[i] = p.getPname() + "_" + p.getIp();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Please choose a printer");
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

    @XClick({R.id.cancelAddCat, R.id.cancelAddMenu})
    private void cancel() {
        findViewById(R.id.menuLayout).setVisibility(View.GONE);
        findViewById(R.id.categoryLayout).setVisibility(View.GONE);
    }

    private void loadCategory() {

        List<Category> list = DaoExpand.queryAllNotDeleted(Applic.app.getDaoMaster().newSession().getCategoryDao()).orderAsc(CategoryDao.Properties.DisplayIdx).list();
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
            List<Menu> menuList = DaoExpand.queryMenuByCategory(Applic.app.getDaoMaster().newSession().getMenuDao(), mCategory);
            menuXAdapter.setData(menuList);
            menuXAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public String toString() {
        return "Menu Management";
    }

//    @Override
//    public boolean onLongClickItem(View view, int i) {
//        modifyingCategory = categoryXAdapter.get(i);
//        showCategory();
//        TextView tvCategory = (TextView) findViewById(R.id.tvCategory);
//        tvCategory.setText(modifyingCategory.getCname());
//
//        return true;
//    }
}
