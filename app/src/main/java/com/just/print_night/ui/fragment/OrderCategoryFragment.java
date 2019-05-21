package com.just.print_night.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.just.print_night.R;
import com.just.print_night.app.Applic;
import com.just.print_night.app.BaseFragment;
import com.just.print_night.db.bean.Category;
import com.just.print_night.db.bean.Mark;
import com.just.print_night.db.bean.Menu;
import com.just.print_night.db.dao.CategoryDao;
import com.just.print_night.db.dao.MenuDao;
import com.just.print_night.db.expand.DaoExpand;
import com.just.print_night.sys.model.SelectionDetail;
import com.just.print_night.sys.server.CustomerSelection;
import com.just.print_night.ui.holder.MarkViewHolder;
import com.just.print_night.ui.holder.SubTitleMenuExpandViewHolder;
import com.just.print_night.ui.holder.TtitleCategoryViewHolder;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.adapter.expand.OnXItemClickListener;
import com.stupid.method.adapter.expand.XExpadnAdapter;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XViewByID;
import com.stupid.method.widget.flowlayout.FlowListView;

import java.util.ArrayList;
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


    public XExpadnAdapter<Category, Menu> categoryExXAdapter;

    @Override
    public void onCreated(Bundle savedInstanceState) {
        new StupidReflect(this, getView()).init();
        categoryExXAdapter = new XExpadnAdapter<Category, Menu>(getContext(), TtitleCategoryViewHolder.class,
                SubTitleMenuExpandViewHolder.class);
        odExCategory.setAdapter(categoryExXAdapter);
        Map<Category, List<Menu>> mdata = new LinkedHashMap<>();
        List<Category> list = DaoExpand.queryAllNotDeleted(Applic.app.getDaoMaster().newSession().getCategoryDao()).orderAsc(CategoryDao.Properties.DisplayIdx).list();
        MenuDao dao = Applic.app.getDaoMaster().newSession().getMenuDao();
        for (Category ca : list) {
            mdata.put(ca, DaoExpand.queryMenuByCategory(ca, dao));
        }
        categoryExXAdapter.addAll(mdata);
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
    @XViewByID(R.id.buttonOK)
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
                case R.id.buttonOK:
                    addMenu(mMenu, mCount, marks);
                case R.id.buttonCancel:
                    cancel();
                    break;
            }
        }
    }
}