package com.just.print.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.db.dao.CategoryDao;
import com.just.print.db.dao.MenuDao;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.ui.holder.MarkViewHolder;
import com.just.print.ui.holder.SubTitleMenuExpandViewHolder;
import com.just.print.ui.holder.TtitleCategoryViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.adapter.expand.OnXItemClickListener;
import com.stupid.method.adapter.expand.XExpadnAdapter;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderCategoryFragment extends BaseFragment {

    //add second level menu
    @XViewByID(R.id.odExCategory)
    ExpandableListView odExCategory;

    @Override
    protected int getLayoutId() {
        return R.layout.order_category_fragment;
    }

    public static Map<Category, List<Menu>> categorizedContent = new LinkedHashMap<>();
    public XExpadnAdapter<Category, Menu> categoryExXAdapter;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        categoryExXAdapter = new XExpadnAdapter<Category, Menu>(getContext(), TtitleCategoryViewHolder.class,
                SubTitleMenuExpandViewHolder.class);
        odExCategory.setAdapter(categoryExXAdapter);
        categorizedContent = new LinkedHashMap<>();

        List<Category> categoryList = DaoExpand.queryAllNotDeleted(Applic.app.getDaoMaster().newSession().getCategoryDao()).orderAsc(CategoryDao.Properties.DisplayIdx).list();
        MenuDao dao = Applic.app.getDaoMaster().newSession().getMenuDao();
        for (Category ca : categoryList) {
            List<Menu> menus = DaoExpand.queryMenuByCategory(ca, dao);
            Collections.sort(menus, new Comparator<Menu>() {
                @Override
                public int compare(Menu m1, Menu m2) {
                    String id1 = m1.getID();
                    String id2 = m2.getID();
                    int minLen = id1.length() > id2.length() ? id2.length() : id1.length();
                    for(int i = 0; i < minLen; i++){
                        char c1 = id1.charAt(i);
                        char c2 = id2.charAt(i);
                        if(c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') { //if it's number, then treat specially.
                            String s1 = id1.substring(i);
                            String s2 = id2.substring(i);
                            try{
                                return Integer.valueOf(s1) - Integer.valueOf(s2);
                            }catch (Exception e){
                                try {
                                    int numInseid1 = 0;
                                    boolean matched1 = false;
                                    for (int j = 0; j < s1.length(); j++) {
                                        char ch1 = s1.charAt(j);
                                        if (ch1 >= '0' && ch1 <= '9') {
                                            continue;
                                        } else {
                                            numInseid1 = Integer.valueOf(s1.substring(0, j));
                                            matched1 = true;
                                            break;
                                        }
                                    }
                                    if (!matched1) {
                                        numInseid1 = Integer.valueOf(s1);
                                    }

                                    int numInseid2 = 0;
                                    boolean matched2 = false;
                                    for (int j = 0; j < s2.length(); j++) {
                                        char ch1 = s2.charAt(j);
                                        if (ch1 >= '0' && ch1 <= '9') {
                                            continue;
                                        } else {
                                            numInseid2 = Integer.valueOf(s2.substring(0, j));
                                            matched2 = true;
                                            break;
                                        }
                                    }
                                    if (!matched2) {
                                        numInseid2 = Integer.valueOf(s2);
                                    }
                                    return numInseid1 == numInseid2 ? s1.compareTo(s2) : numInseid1 - numInseid2;
                                }catch(Exception exp){
                                    exp.printStackTrace();
                                }
                            }
                        }else if(c1 != c2){
                            return c1 - c2;
                        }
                    }
                    return id1.length() - id2.length();
                }
            });
            categorizedContent.put(ca, menus);
        }
        categoryExXAdapter.clear();
        categoryExXAdapter.addAll(categorizedContent);
        categoryExXAdapter.setClickItemListener(new OnXItemClickListener() {

            @Override
            public void onClickItem(View view, int i) {
                odExCategory.setSelectedGroup(i);
            }

            @Override
            public void onClickChild(View view, int i, int i1) {
                addMenu((Menu) categoryExXAdapter.getChild(i, i1),1,null);
                //new addMenuCtrl((Menu) categoryExXAdapter.getChild(i, i1)).show();
            }
        });

    }

    /**
     * 在这里添加到点菜列表里
     */
    private void addMenu(Menu menu, int count, List<Mark> marks) {
        //使用MenuService封装的静态对象List保存菜单。菜单只由该对象管理
        //List对象为DishesDetailModel
        SelectionDetail ddm = new SelectionDetail();
        ddm.setDish(menu);
        ddm.setDishNum(count);
        List<String> list = new ArrayList<String>();
        ddm.setMarkList(marks);
        CustomerSelection.getInstance().addSelectedDish(ddm);
        getEventBus().post(OrderIdentifierFragment.EVENT_ADD_MENU);
        showToast("Added successfully");
    }

    @XViewByID(R.id.showTitle)
    TextView showTitle;
    @XViewByID(R.id.showCount)
    TextView showCount;
    @XViewByID(R.id.buttonReduce)
    Button buttonReduce;
    @XViewByID(R.id.buttonAdd)
    Button buttonAdd;
    @XViewByID(R.id.showMarks)
    FlowListView showMarks;
    @XViewByID(R.id.addMenuCtrl)
    View addMenuCtrl;
    @XViewByID(R.id.btnConfirmResetReportOK)
    Button buttonOk;
    @XViewByID(R.id.buttonCancel)
    Button buttonCancel;

    class addMenuCtrl implements View.OnClickListener {
        int mCount = 1;
        final Menu mMenu;
        XAdapter2<Mark> markAdapter;

        List<Mark> marks = new ArrayList<Mark>(3);

        addMenuCtrl(Menu men) {
            mMenu = men;
            markAdapter = new XAdapter2<Mark>(getActivity(), DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getMarkDao()), MarkViewHolder.class);
            markAdapter.setClickItemListener(new OnClickItemListener() {
                @Override
                public void onClickItem(View view, int i) {

                    Mark m = markAdapter.getItem(i);
                    if (m.select) {
                        m.select = false;
                        marks.remove(m);
                    } else {
                        m.select = true;
                        marks.add(m);
                    }
                    markAdapter.notifyDataSetChanged();
                }
            });
            showMarks.setAdapter(markAdapter);
            showTitle.setText(men.getMname());
            showCount.setText(Integer.toString(mCount));
            buttonAdd.setOnClickListener(this);
            buttonReduce.setOnClickListener(this);
            buttonCancel.setOnClickListener(this);
            buttonOk.setOnClickListener(this);

        }

        void show() {
            addMenuCtrl.setVisibility(View.VISIBLE);
        }

        void cancel() {
            addMenuCtrl.setVisibility(View.GONE);
            buttonAdd.setOnClickListener(null);
            buttonReduce.setOnClickListener(null);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonAdd:
                    mCount++;
                    showCount.setText(Integer.toString(mCount));
                    break;
                case R.id.buttonReduce:
                    if (mCount > 1)
                        mCount--;
                    showCount.setText(Integer.toString(mCount));
                    break;
                case R.id.btnConfirmResetReportOK:
                    addMenu(mMenu, mCount, marks);
                case R.id.buttonCancel:
                    cancel();
                    break;
            }
        }
    }
}
