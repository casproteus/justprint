package com.generate;


import org.junit.Test;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class Generated {

    @Test
    public void gen() {
        generateAll(1, "com.just.print");
    }


    public void generateAll(int version, String dir) {
        String dbroot = dir + ".db";

        Schema schema = new Schema(version, dbroot + ".bean");
        schema.setDefaultJavaPackageDao(dbroot + ".dao");
        schema.setDefaultJavaPackageTest(dbroot + ".test");
        schema.enableKeepSectionsByDefault();
        schema.enableActiveEntitiesByDefault();
        creat(schema);

        File f = new File(getOutPath());

        if (!f.exists())
            f.mkdirs();
        try {
            new DaoGenerator().generateAll(schema, f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void creat(Schema schema) {
        /**
         *  @table Printer
         *  @pk id
         *
         * +---------+---------+---------+---------+
         * |    id   |   pname |   ip    |   note  |
         * +---------+---------+---------+---------+
         * */
        Entity print = schema.addEntity("Printer");
        print.addIdProperty();
        print.addStringProperty("ip").columnName("IP");
        print.addStringProperty("pname").columnName("P_NAME");
        print.addStringProperty("note").columnName("NOTE");
        print.addIntProperty("firstPrint");
        print.addIntProperty("type");

        /**
         *  @table Category 类别
         *  @pk id
         * +---------+---------+
         * |    id   |   cname |
         * +---------+---------+
         * */
        Entity category = schema.addEntity("Category");//类别
        category.addStringProperty("cname").columnName("C_NAME");
        category.addIdProperty();

        /**
         *  @table Menu 菜单
         *  @pk id
         *  @fk pid, cid
         *  pid->Printer.id;
         *  cid->Category.id;
         * +---------+---------+---------+---------+
         * |    id   |   pid   |  cid    |   manem |
         * +---------+---------+---------+---------+
         * */
        Entity menu = schema.addEntity("Menu");//类别->菜单
        menu.addStringProperty("mname").columnName("M_NAME");
        menu.addStringProperty("ID").primaryKey().unique();
        menu.addDoubleProperty("price");//单价
        Property.PropertyBuilder cid = menu.addLongProperty("cid").columnName("C_ID");
        category.addToMany(menu, cid.getProperty());

        /**
         * printer and dish are multi to multi, add a dish table.
         *
         * **/
        Entity menuPrint = schema.addEntity("M2M_MenuPrint");
        Property.PropertyBuilder pid = menuPrint.addLongProperty("pid");
        Property.PropertyBuilder mid = menuPrint.addStringProperty("mid");
        menuPrint.addIdProperty().primaryKeyAsc().unique();
        menuPrint.addToOne(print, pid.getProperty(), "print");
        menuPrint.addToOne(menu, mid.getProperty(), "menu");
        menu.addToMany(menuPrint, mid.getProperty());

        /****/
        Entity mark = schema.addEntity("Mark");
         mark.addStringProperty("name").columnName("NAME").primaryKey().unique();

        Entity log = schema.addEntity("Log");
        log.setTableName("Logs");
        log.addIdProperty();
        log.addStringProperty("msg");
        log.addStringProperty("uName");
        log.addDateProperty("date");
        log.addIntProperty("logType");

        //---------------添加店铺外键-------------

//        for (Entity en : schema.getEntities()) {
//            en.addStringProperty("shopId");
//        }
//        Entity user = schema.addEntity("User");
//        user.addStringProperty("ip").unique().primaryKey();
//        user.addStringProperty("userID");
//        user.addStringProperty("phoneIMEI");
//        user.addStringProperty("phoneID");
//        user.addStringProperty("alias");//别名
//        user.addStringProperty("pwd");
//        user.addIntProperty("permissions");// 1 is the biggest
        //Property.PropertyBuilder shopId = user.addStringProperty("shopId").columnName("SHOP_ID");
        //  user.addDateProperty("createTime");
//        Entity shop = schema.addEntity("Shop");
//        shop.addStringProperty("shopName");
//        shop.addStringProperty("shopId").primaryKey().unique();
//        shop.addStringProperty("shopGroup");
//        shop.addDateProperty("createTime");
//        shop.addToMany(user, shopId.getProperty(), "workers");//  employtees belongs to the store.

        //-------------添加,state,version---------------
        for (Entity en : schema.getEntities()) {
            en.addIntProperty("state").columnName("STATE");// default:0, del:1,
            en.addLongProperty("version").columnName("VERSION");
        }

        Entity device = schema.addEntity("Device");//在线设备
        device.addStringProperty("ip");
        device.addStringProperty("userName");
    }

    protected String getOutPath() {
        return "../justprintdb/src/main/java/";
    }
}
