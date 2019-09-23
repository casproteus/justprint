package com.just.print.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;
import android.widget.ViewSwitcher;

import com.just.print.R;
import com.just.print.app.AppData;
import com.just.print.app.Applic;
import com.just.print.app.BaseFragment;
import com.just.print.app.EventBus;
import com.just.print.db.bean.Category;
import com.just.print.db.bean.Mark;
import com.just.print.db.bean.Menu;
import com.just.print.db.bean.SaleRecord;
import com.just.print.db.expand.DaoExpand;
import com.just.print.sys.model.SelectionDetail;
import com.just.print.sys.server.CustomerSelection;
import com.just.print.sys.server.WifiPrintService;
import com.just.print.ui.activity.ConfigActivity;
import com.just.print.ui.holder.OrderMarksSelectionViewHolder;
import com.just.print.ui.holder.OrderIdentifierItemViewHolder;
import com.just.print.ui.holder.OrderIdentifierMarkViewHolder;
import com.just.print.ui.holder.OrderMenuViewHolder;
import com.just.print.util.L;
import com.just.print.util.StringUtils;
import com.stupid.method.adapter.IXOnItemClickListener;
import com.stupid.method.adapter.OnClickItemListener;
import com.stupid.method.adapter.XAdapter2;
import com.stupid.method.reflect.StupidReflect;
import com.stupid.method.reflect.annotation.XClick;
import com.stupid.method.reflect.annotation.XViewByID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class OrderIdentifierFragment extends BaseFragment implements View.OnClickListener, OnClickItemListener, EventBus.EventHandler{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String EVENT_ADD_MENU = "EVENT_ADD_MENU=";
    private static final String TAG = "OrderIdentifierFragment";

    private static OrderIdentifierFragment instance = null;
    private static boolean isCancel;
    private List<Mark> allMarks;
    private List<Mark> marksOfCurDish;
    private int curmarkitem;

    public static OrderIdentifierFragment getInstance(){
        if(instance == null){
            instance = new OrderIdentifierFragment();
        }
        return instance;
    }

    @XViewByID(R.id.viewSwitcher)
    private ViewSwitcher viewSwitcher;

    @XViewByID(R.id.odIdFrLoutMenuList)
    private ListView odIdFrLoutMenuList;

    @XViewByID(R.id.odIdTable2)
    private static ToggleButton odIdTable2;

    @XViewByID(R.id.odIdDelBtn)
    private ListView odIdDelBtn;

    @XViewByID(R.id.odIdOkBtn)
    private ListView odIdOkBtn;

    @XViewByID(R.id.odIdInput)
    private TextView odIdInput;

    @XViewByID(R.id.odIdfrName)
    private TextView odIdfrName;

    @XViewByID(R.id.odIdCategoryGrid)
    private GridView odIdCategoryGrid;

    @XViewByID(R.id.odIdLoutItemsGv)
    private GridView odIdLoutItemsGv;

    @XViewByID(R.id.odIdMarksGrid)
    private GridView odIdMarksGrid;

    @XViewByID(R.id.odIdTableTbtn)
    ToggleButton odIdTableTbtn;

    @XViewByID(R.id.allMarkList)
    ListView markAllList;

    private Menu storedMenu;

    XAdapter2<Mark> markShorCutXAdapter;
    XAdapter2<Menu> menuXAdapter;
    XAdapter2<SelectionDetail> dishesXAdapter;
    XAdapter2<String> categoryXAdapter;
    XAdapter2<String> itemXAdapter;
    XAdapter2<Mark> markAllXAdapter;

    private List<Mark> shortCutMarks;

    public static List<SelectionDetail> bkOfLastSelection;
//    private static CharSequence bkOfLastTable;
    static int times = 0;
    String[] categoryNames = {""};
    String[] items;// = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "H", "S", "U", "+", "togo", "canc"};

    @Override
    public void onCreated(Bundle savedInstanceState) {
        getEventBus().register(EVENT_ADD_MENU, this);
        new StupidReflect(this, getView()).init();
        //设置餐桌号用
        //CustomerSelection.getInstance().setTableName(odIdTableNumEt.getText().toString());
        storedMenu = null;
        //the model of the menus.
        if(AppData.isMode2()) {
            //prepare the categoryNames and items.
            List<Category> categories = OrderCategoryFragment.getCategoryList();
            categoryNames = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                categoryNames[i] = categories.get(i).getCname();
            }

            List<Menu> menus;
            findViewById(R.id.odIdInfo).setVisibility(View.GONE);
            findViewById(R.id.odIdSndBtn).setVisibility(View.GONE);

            if(categories.size() > 1) {
                //remove control buttons(will use send to go to back end(when selected dishes are empty)
                findViewById(R.id.topButtons).setVisibility(View.GONE);
                ((GridView)findViewById(R.id.odIdCategoryGrid)).setNumColumns(categories.size());
                menus = OrderCategoryFragment.getCategorizedContent().get(categories.get(0));
            }else{
                findViewById(R.id.odIdCategoryGrid).setVisibility(View.GONE);
                if(categories.size() == 0){
                    menus = new ArrayList<Menu>();
                }
                menus = OrderCategoryFragment.getCategorizedContent().get(categories.get(0));
            }

            //might need more col for menu.
            String col = AppData.getCustomData("column");
            try {
                ((GridView) findViewById(R.id.odIdLoutItemsGv)).setNumColumns(Integer.valueOf(col));
            }catch(Exception e){
                //ignore, it's normal user didn't change the setting. then leave it to be 2.
            }

            //get the menu for current category.
            items = new String[menus.size()];
            for (int i = 0; i < menus.size(); i++) {
                items[i] = menus.get(i).getID();
            }

            odIdTable2.setText(AppData.getCustomData("kitchenBillIdx"));
        }else{
            findViewById(R.id.odIdCategoryGrid).setVisibility(View.GONE);
            findViewById(R.id.odIdTools).setVisibility(View.GONE);
            //the "must have" buttons
            items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C"};
            List<String> ary = new ArrayList<String>();
            for (int i = 0; i < items.length; i++) {
                ary.add(items[i]);
            }
            //the flexible buttons which difined by KEY_CUST_LAST_CHAR
            String definedLast = AppData.getCustomData(AppData.KEY_CUST_LAST_CHAR);
            if (!StringUtils.isBlank(definedLast)) {
                char maxChar = definedLast.charAt(0);
                char lastChar = items[items.length - 1].charAt(0);
                while (lastChar < maxChar) {
                    lastChar++;
                    ary.add(String.valueOf(lastChar));
                }
            }
            //add the custChars, if not defined, then add an s and a u.
            String custChars = AppData.getCustomData("custChars");
            if (custChars.length() == 0) {
                custChars = "SU";
            }
            for (int i = 0; i < custChars.length(); i++) {
                ary.add(custChars.substring(i, i + 1));
            }
            //other fixed buttons.
            ary.add("+");
            ary.add("togo");
            ary.add("canc");

            items = ary.toArray(items);
        }

        categoryXAdapter = new XAdapter2<String>(getActivity(), Arrays.asList(categoryNames), OrderIdentifierItemViewHolder.class);
        categoryXAdapter.setClickItemListener(this.categoryXAdapterClick);
        odIdCategoryGrid.setAdapter(categoryXAdapter);

        itemXAdapter = new XAdapter2<String>(getActivity(), Arrays.asList(items), OrderIdentifierItemViewHolder.class);
        itemXAdapter.setClickItemListener(this.itemXAdapterClick);
        odIdLoutItemsGv.setAdapter(itemXAdapter);

        odIdTableTbtn.setTextOn(null);
        odIdTableTbtn.setTextOff(null);
        odIdTableTbtn.setText("");
        odIdTableTbtn.setChecked(true);
        odIdTableTbtn.setOnClickListener(this);

        //initSelected dishes part.
        dishesXAdapter = new XAdapter2<SelectionDetail>(getContext(), OrderMenuViewHolder.class);
        dishesXAdapter.setClickItemListener(this);
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        odIdFrLoutMenuList.setAdapter(dishesXAdapter);

        //add Marks
        allMarks = sortMarks(DaoExpand.queryNotDeletedAll(Applic.getMarkDao()));
        //add shortcut Marks.
        initShortCutMarks();

        //init MarkSelection panel
        markAllXAdapter = new XAdapter2<Mark>(getContext(), OrderMarksSelectionViewHolder.class);
        markAllXAdapter.setClickItemListener(this);
        markAllXAdapter.setData(allMarks);
        markAllList.setAdapter(markAllXAdapter);
    }

    @XClick({R.id.odIdConfigBtn,R.id.odIdConfigBtn2, R.id.odIdSndBtn, R.id.odIdSndBtn2, R.id.odIdDelBtn, R.id.odIdOkBtn, R.id.odIdCancel})
    private void exeControlCommand(View v) {
        switch (v.getId()) {
            case R.id.odIdConfigBtn:
            case R.id.odIdConfigBtn2:
                startActivity(new Intent(getContext(), ConfigActivity.class));
                break;
            case R.id.odIdCancel:
                printCurrentSelection(true);
                break;
            case R.id.odIdSndBtn:
            case R.id.odIdSndBtn2:
                printCurrentSelection(false);
                break;
            case R.id.odIdDelBtn:
                if( viewSwitcher.getDisplayedChild() == 0) {
                    DelOneText();
                }else{
                    viewSwitcher.setDisplayedChild(0);
                }
                break;
            case R.id.odIdOkBtn:
                if(AppData.isMode2()){
                    findViewById(R.id.topButtons).setVisibility(View.GONE);
                }
                String adminPassword = AppData.getCustomData("adminPassword");
                if(adminPassword == null || adminPassword.length() < 6){
                    adminPassword = "AA88AA";
                }
                if(adminPassword.equals(odIdTableTbtn.getText().toString()) || adminPassword.equals(odIdInput.getText().toString())){
                    //print report
                    List<SaleRecord> orders = Applic.app.getDaoMaster().newSession().getSaleRecordDao().loadAll();

                    if(orders == null || orders.size() == 0){
                        showToast("No report to print! The sales record has been cleaned!");
                    }else {
                        String reportStartDate = AppData.getCustomData("reportStartDate");
                        if (reportStartDate == null || reportStartDate.length() < 1) {
                            reportStartDate = AppData.getCustomData("lastsuccess");
                        }

                        //print code:
                        String result = WifiPrintService.getInstance().exePrintReportCommand(orders, reportStartDate, String.valueOf(new Date().getTime()));
                        if ("0".equals(result)) {
                            findViewById(R.id.viewSwitcher).setVisibility(View.INVISIBLE);
                            findViewById(R.id.topButtons).setVisibility(View.INVISIBLE);
                            findViewById(R.id.alertDlg).setVisibility(View.VISIBLE);
                        }
                    }
                    odIdTableTbtn.setText("");
                    odIdInput.setText("");
                }else if( viewSwitcher.getDisplayedChild() == 0) {
                    if (odIdTableTbtn.isChecked()) {    //waiting for input table num status. always allowed!
                        odIdTableTbtn.setChecked(false);
                        CustomerSelection.getInstance().setTableName(odIdTableTbtn.getText().toString());
                    } else {                            //inputting dishes status. need to check print status.
//this is marked off because we changed to allow user to add new order, just when user try to send will do a check and pump to ask if will go on.
//                        if (bkOfLastSelection != null) {      //categorizedContent not all send to printer yet.
//                            //if last order has only one dish and it happened to be same
//                            //as the one selected this time, waiter will think the roll back is not happenning.
//                            //we decided to not display last order--- rollbackLastOrder();
//                            ToastUtil.showToast("Printing...Please wait.");
//                            return;
//                        } else
                        if (storedMenu != null) {     //last order send to printer well.
                            addDish();
                            odIdInput.setText("");
                            odIdfrName.setText("");
                            if (dishesXAdapter != null) {
                                loadOrderMenu();
                            }
                        }
                    }
                }else{
                    updateSelectionOfCurrentDish();
                }
                break;

//            case R.id.odMarkSelectionCancel:
//                viewSwitcher.setDisplayedChild(0);
//                break;
//
//            case R.id.odMarkSelectionOK:
//                updateSelectionOfCurrentDish();
//                break;
        }

    }

    @XClick({R.id.buttonCancel})
    private void notResetReport(){
        findViewById(R.id.alertDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertDlg).setMinimumHeight(0);
        findViewById(R.id.topButtons).setY(0);
        findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        findViewById(R.id.viewSwitcher).setVisibility(View.VISIBLE);
        findViewById(R.id.topButtons).invalidate();
    }

    @XClick({R.id.btnConfirmResetReportOK})
    private void resetReport(){
        //when printed succcesfully, clean all records, and update now as the next reportStartDate
        Applic.app.getDaoMaster().newSession().getSaleRecordDao().deleteAll();
        AppData.putCustomData("reportStartDate", String.valueOf(new Date().getTime()));
        AppData.putCustomData("kitchenBillIdx", "1");        //reset kitchenbillIndex
        int reportIdx = 1;
        try{
            reportIdx = Integer.valueOf(AppData.getCustomData("reportIdx"));
        }catch(Exception e){
            //report error.
        }
        AppData.putCustomData("reportIdx", String.valueOf(reportIdx + 1));

        findViewById(R.id.alertDlg).setVisibility(View.INVISIBLE);
        findViewById(R.id.alertDlg).setMinimumHeight(0);
        findViewById(R.id.topButtons).setY(0);
        findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        findViewById(R.id.viewSwitcher).setVisibility(View.VISIBLE);
        findViewById(R.id.topButtons).invalidate();
    }

    private void updateSelectionOfCurrentDish() {
        for(int i = 0; i < allMarks.size(); i++){
            Mark mark = allMarks.get(i);
            boolean matched = false;
            for(int j = 0; j < marksOfCurDish.size(); j++){
                if(marksOfCurDish.get(j).getName().equals(mark.getName())){
                    if(mark.getQt() == 0){
                        marksOfCurDish.remove(j);
                    }else {
                        marksOfCurDish.get(j).setQt(mark.getQt());
                    }
                    matched = true;
                    break;
                }
            }
            if(!matched && mark.getQt() > 0){
                marksOfCurDish.add(mark.clone());
            }
        }
        viewSwitcher.setDisplayedChild(0);
        //menuXAdapter.notifyDataSetChanged();
        dishesXAdapter.get(curmarkitem).setMarkList(marksOfCurDish);

        loadOrderMenu();
    }

    private boolean printCurrentSelection(final boolean isCancel) {
        List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
        if(selectionDetails == null || selectionDetails.size() == 0){
            showToast("Nothing selected!");
            return false;
        }else {
            final String result = WifiPrintService.getInstance().exePrintCommand(isCancel);
            if (result.startsWith("192.") || result.startsWith("10.")) {
                // pup up a dialog to ask if user want to continue.....
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Last print on " + result + " not completed. Continue? ");
                builder.setTitle("WARNING").setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //do we need to clean bkSelections?????
                        confirmPrintOK();
                        //clean the printer maps, to make sure it can pass the check when call itself again to send content to printer.
                        WifiPrintService.getInstance().addProblematicPrinter(result);
                        WifiPrintService.getInstance().reInitPrintRelatedMaps();
                        WifiPrintService.getInstance().resetFlags();
                        printCurrentSelection(isCancel);
                    }
                });
                builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return false;
            } else {
                backupAndClearOrderMenu();
                return true;
            }
        }
    }

    //currently it's used by number buttons and the note tag buttons. both types are buttons in holder.
    IXOnItemClickListener itemXAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            L.d(TAG, "onClickItem");
            Menu tmpMenu;
            L.d(TAG, String.valueOf(view.getId()) + String.valueOf(i));
            switch (view.getId()) {
                case R.id.buttonholder: //number buttons.
                    if ("2".equals(AppData.getCustomData("appmode"))) {
                        storedMenu = SearchMenuFromDB(items[i]);
                        addDish();
                        if (dishesXAdapter != null) {
                            loadOrderMenu();
                        }
                    } else {
                        if (i < 10) {
                            InputText(Integer.toString((i + 1) % 10));
                        } else if (i == items.length - 3) {         //following are fixed buttons.
                            InputText("+");
                        } else if (i == items.length - 2) {
                            odIdTableTbtn.setText("TOGO");
                            CustomerSelection.getInstance().setTableName(odIdTableTbtn.getText().toString());
                        } else if (i == items.length - 1) {
                            odIdTableTbtn.setText("");
                            //added the selected dish with negative price.
                            if (printCurrentSelection(true)) {    //arranged to print---didn't met the case that previous print not finished yet.
                                isCancel = true;
                            }
                        } else {
                            String custChars = AppData.getCustomData("custChars");
                            if (custChars.length() == 0) {
                                custChars = "SU";
                            }
                            int p = i - (items.length - 3 - custChars.length());
                            if (p >= 0) {
                                InputText(custChars.substring(p, p + 1));
                            } else {
                                InputText(Character.toString((char) (i % 10 + 'A')));
                            }
                        }

                        if (!odIdTableTbtn.isChecked()) {
                            tmpMenu = SearchMenuFromDB(odIdInput.getText().toString());
                            if (null != tmpMenu) {
                                odIdfrName.setText(tmpMenu.getMname());
                            } else {
                                odIdfrName.setText("");
                            }
                        }
                    }
                    break;
                case R.id.tagid:            //not tag buttons.
                    L.d(TAG, "case tag");
                    List<SelectionDetail> selectionDetails = CustomerSelection.getInstance().getSelectedDishes();
                    //Mark Press
                    if (selectionDetails != null && selectionDetails.size() > 0) {
                        Mark selectedMark = markShorCutXAdapter.getItem(i);
                        L.d(TAG, String.valueOf(selectedMark.select));
                        List<Mark> marks = selectionDetails.get(selectionDetails.size() - 1).getMarkList();
                        boolean matched = false;
                        for(int m = 0; m < marks.size(); m++){
                            Mark mark = marks.get(m);
                            if(mark.getName().equals(selectedMark.getName())){
                                mark.setQt(mark.getQt() + 1);
                                matched = true;
                                break;
                            }
                        }
                        if(!matched){
                            Mark m = selectedMark.clone();
                            m.setQt(1);
                            marks.add(m);
                        }
                        loadOrderMenu();
                    }
            }

            //odIdfrName.setText(menuXAdapter.get(i).getMname());
        }
    };

    //currently it's used by number buttons and the note tag buttons. both types are buttons in holder.
    IXOnItemClickListener categoryXAdapterClick = new IXOnItemClickListener() {
        @Override
        public void onClickItem(View view, int i) {
            L.d(TAG, "onClickCategory" + view.getId() + String.valueOf(i));

            List<Menu> menus = OrderCategoryFragment.getCategorizedContent().get(OrderCategoryFragment.getCategoryList().get(i));
            items = new String[menus.size()];
            for (i = 0; i < menus.size(); i++) {
                items[i] = menus.get(i).getID();
            }
            itemXAdapter.setData(Arrays.asList(items));
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.order_identifier_fragment;
    }

    @Override
    public void handleEvent(String eventName, Object... argument) {
        if (EVENT_ADD_MENU.equals(eventName)) {
            loadOrderMenu();
        }
    }

    @Override
    public void onClick(View v) {
        int num;
        switch (v.getId()) {
            case R.id.odIdTableTbtn:
                L.d(TAG, "ClickTableButton");
                if (odIdTableTbtn.isChecked()) {
                    odIdTableTbtn.setText("");
                }
                break;
        }
    }

    private void InputText(String str) {
        if (odIdTableTbtn.isChecked()) {
            odIdTableTbtn.append(str);
        } else {
            odIdInput.append(str);
        }

    }

    private void DelOneText() {
        if (odIdTableTbtn.isChecked()) {
            if (odIdTableTbtn.length() > 0) {
                odIdTableTbtn.setText(odIdTableTbtn.getText().subSequence(0, odIdTableTbtn.length() - 1));
            }
        } else {
            if (odIdInput.length() > 0) {
                odIdInput.setText(odIdInput.getText().subSequence(0, odIdInput.length() - 1));
            }
        }
    }

    private Menu SearchMenuFromDB(String context) {
        List<Menu> mnlist = DaoExpand.queryFuzzyMenu(Applic.app.getDaoMaster().newSession().getMenuDao(), context);
        if (mnlist.size() >= 1) {
            Collections.sort(mnlist, new Comparator<Menu>() {
                @Override
                public int compare(Menu m1, Menu m2) {
                    return m1.getMname().compareTo(m2.getMname());
                }
            });
            storedMenu = mnlist.get(0);
            return mnlist.get(0);
        }
        if (context.length() == 0) {
            return null;
        }
        return null;
    }

    @Override
    public void onClickItem(View view, int i) {
        L.d(TAG, "onClickItem");
        switch (view.getId()) {
            case R.id.oddelDish:
                SelectionDetail a = dishesXAdapter.get(i);
                //dishesXAdapter.remove(a);
                CustomerSelection.getInstance().deleteSelectedDish(a);
                loadOrderMenu();
                break;

            case R.id.odMnLoutMarkBtn:
                curmarkitem = i;
                marksOfCurDish = dishesXAdapter.get(i).getMarkList();
                showMarksPanel();

                break;
        }
    }

    private void showMarksPanel(){   //final onChoiceMarks choiceMarks) {
        if(AppData.isMode2()){
            findViewById(R.id.topButtons).setVisibility(View.VISIBLE);
        }
        if (marksOfCurDish == null) {
            marksOfCurDish = new ArrayList<Mark>();
        }

        for(int j = 0; j < allMarks.size(); j++) {
            Mark mark = allMarks.get(j);
            boolean matched = false;
            for(int i = 0; i < marksOfCurDish.size(); i++){
                String markName = marksOfCurDish.get(i).getName();
                if(mark.getName().equals(markName)){
                    matched = true;
                    mark.setQt(marksOfCurDish.get(i).getQt());
                    break;
                }
            }

            if(!matched) {
                mark.setQt(0);
            }
        }


        markAllXAdapter.setData(allMarks);
        markAllList.setAdapter(markAllXAdapter);
        markAllList.setSelection(0);

        if (viewSwitcher.getDisplayedChild() == 0){
            viewSwitcher.setDisplayedChild(1);
        }
//        new AlertDialog.Builder(this.getActivity()).setMultiChoiceItems(markNames, selectedQT, new DialogInterface.OnMultiChoiceClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                if (isChecked)
//                    sa.put(Integer.toString(which), which);
//                else
//                    sa.remove(Integer.toString(which));
//            }
//        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                List<Mark> result = new ArrayList<Mark>();
//                for (Integer i : sa.values()) {
//                    result.add(allMarks.get(i));
//                }
//                choiceMarks.onChoiceMarks(result);
//
//            }
//        }).setNegativeButton("Cancel", null).show();
    }

    private void addDish() {
        SelectionDetail selectedDish = new SelectionDetail();
        selectedDish.setDish(storedMenu);
        selectedDish.setMarkList(new ArrayList<Mark>());
        L.d(TAG, shortCutMarks.toString());
        selectedDish.setDishNum(1);
        CustomerSelection.getInstance().addSelectedDish(selectedDish);
        storedMenu = null;
    }

    private void loadOrderMenu() {
        dishesXAdapter.setData(CustomerSelection.getInstance().getSelectedDishes());
        dishesXAdapter.notifyDataSetChanged();
    }

    private List<Mark> sortMarks(List<Mark> unsortedMarks) {
        List<Mark> sortedMarks = new ArrayList<Mark>();
        for (int i = 0; i < unsortedMarks.size(); i++) {
            Mark mark = unsortedMarks.get(i);
            boolean inserted = false;
            for (int j = 0; j < sortedMarks.size(); j++) {
                if (sortedMarks.get(j).getState() > mark.getState()) {
                    sortedMarks.add(j, mark);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                sortedMarks.add(mark);
            }
        }
        return sortedMarks;
    }

    private void initShortCutMarks() {
        shortCutMarks = new ArrayList<Mark>(3);
        List<Mark> marks = new ArrayList<Mark>();
        marks.add(allMarks.size() < 1 ? new Mark("M1") : allMarks.get(0));
        marks.add(allMarks.size() < 2 ? new Mark("M2") : allMarks.get(1));
        markShorCutXAdapter = new XAdapter2<Mark>(getActivity(), marks, OrderIdentifierMarkViewHolder.class);
        markShorCutXAdapter.setClickItemListener(this.itemXAdapterClick);
        odIdMarksGrid.setAdapter(markShorCutXAdapter);
    }

    private void backupAndClearOrderMenu() {
        //bk and clear selected menu
        bkOfLastSelection = new ArrayList<SelectionDetail>();
        for(SelectionDetail selectionDetail : CustomerSelection.getInstance().getSelectedDishes()){
            bkOfLastSelection.add(selectionDetail);
        }
        CustomerSelection.getInstance().clearMenu();

        //bk and clear bkOfLastTable
        //bkOfLastTable = odIdTableTbtn.getText();
        odIdTableTbtn.setText("");
        odIdTableTbtn.setChecked(true);
        loadOrderMenu();

        odIdTable2.setText(AppData.getCustomData("kitchenBillIdx"));
    }

    /**no one is calling this mehtod now, because we not response feels like app goes wrong. and user might input again.
     //waiting 15 seconds or untile confirmPrintOK() is called.
     private void waitForPrintSuccess(){
     int i = 0;
     while (true){
     i++;
     AppUtils.sleep(1000);
     if( lastSelection == null && "TB".equals(bkOfLastTable)){
     return;
     }
     }
     }

     //no one is calling this mehtod now, because we relized the order will not lost, it stay in ipConentMap
     //when connection come back, it will be printed out.
     private void rollbackLastOrder() {
     //save the menu into database.
     for(SelectionDetail selectionDetail : lastSelection){
     CustomerSelection.getInstance().addSelectedDish(selectionDetail);
     }

     //clear selected menu
     odIdTableTbtn.setText(bkOfLastTable);
     odIdTableTbtn.setChecked(false);
     loadOrderMenu();

     }*/

    public static void confirmPrintOK(){
        bkOfLastSelection = null;
        isCancel = false;
        //bkOfLastTable = "TB";
    }

}
