package co.ke.tonyoa.android.vossified.Data;

import android.net.Uri;
import android.provider.BaseColumns;

public class VossitContract {

    public static final String AUTHORITY = "co.ke.tonyoa.android.vossified";
    public static final Uri BASEURI = Uri.parse("content://" + AUTHORITY);
    public static final String LASTMODIFIEDCOLUMN = "LASTMODIFIED";

    public static class CATEGORIESENTRY implements BaseColumns {
        public static final String TABLENAME = "CATEGORIES";
        public static final String CATEGORYNAMECOLUMN = "CATEGORYNAMECOLUMN";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + CATEGORYNAMECOLUMN + " TEXT NOT NULL UNIQUE, " +
                LASTMODIFIEDCOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP)";
    }

    public static class CATEGORYIMAGESENTRY implements BaseColumns {
        public static final String TABLENAME = "CATEGORYIMAGES";
        public static final String CATEGORYIMAGECOLUMN = "CATEGORYIMAGE";
        public static final String CATEGORYIDIDCOLUMN = "CATEGORYID";
        public static final String LOCALPATHCOLUMN="LOCALPATH";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + CATEGORYIMAGECOLUMN + " TEXT, " +
                CATEGORYIDIDCOLUMN + " INTEGER NOT NULL, "+LOCALPATHCOLUMN+" TEXT, " +
                LASTMODIFIEDCOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(" + CATEGORYIDIDCOLUMN + ")"
                + " REFERENCES " + CATEGORIESENTRY.TABLENAME + "(" + CATEGORIESENTRY._ID + ") ON UPDATE CASCADE ON DELETE CASCADE)";
    }

    public static class ITEMSENTRY implements BaseColumns {
        public static final String TABLENAME = "ITEMS";
        public static final String ITEMNAMECOLUMN = "ITEMNAME";
        public static final String ITEMDESCRIPTIONCOLUMN = "ITEMDESCRIPTION";
        public static final String ITEMCOSTCOLUMN = "ITEMCOST";
        public static final String CATEGORYIDCOLUMN = "CATEGORYID";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEMNAMECOLUMN + " TEXT NOT NULL UNIQUE, " +
                ITEMDESCRIPTIONCOLUMN + " TEXT, " + ITEMCOSTCOLUMN + " NUMBER, " + CATEGORYIDCOLUMN +
                " INTEGER NOT NULL, " + LASTMODIFIEDCOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP, "+
                "FOREIGN KEY("+CATEGORYIDCOLUMN+") REFERENCES "+ CATEGORIESENTRY.TABLENAME+"("+
                CATEGORIESENTRY._ID+") ON UPDATE CASCADE ON DELETE RESTRICT)";
    }

    public static class ITEMIMAGESENTRY implements BaseColumns {
        public static final String TABLENAME = "ITEMIMAGES";
        public static final String ITEMIMAGECOLUMN = "ITEMIMAGE";
        public static final String ITEMIDCOLUMN = "ITEMID";
        public static final String MAINIMAGECOLUMN="MAINIMAGE";
        public static final String LOCALPATHCOLUMN="LOCALPATH";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEMIMAGECOLUMN + " TEXT, " +
                ITEMIDCOLUMN + " INTEGER NOT NULL, "+MAINIMAGECOLUMN+" INTEGER(1) NOT NULL DEFAULT 0, "+
                LOCALPATHCOLUMN+" TEXT, "+ LASTMODIFIEDCOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(" + ITEMIDCOLUMN + ")"
                + " REFERENCES " + ITEMSENTRY.TABLENAME + "(" + ITEMSENTRY._ID + ") ON UPDATE CASCADE ON DELETE CASCADE)";
    }

    public static class ITEMLINKSENTRY implements BaseColumns {
        public static final String TABLENAME = "ITEMLINKS";
        public static final String ITEMLINKCOLUMN = "ITEMLINK";
        public static final String STORECOLUMN = "STORECOLUMN";
        public static final String ITEMIDCOLUMN = "ITEMIDCOLUMN";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEMLINKCOLUMN + " TEXT, " +
                STORECOLUMN + " TEXT, " +ITEMIDCOLUMN + " INTEGER NOT NULL, " + LASTMODIFIEDCOLUMN +
                " TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(" + ITEMIDCOLUMN + ")"
                + " REFERENCES " + ITEMSENTRY.TABLENAME + "(" + ITEMSENTRY._ID + ") ON UPDATE CASCADE ON DELETE CASCADE)";

    }

    public static class USERSENTRY implements BaseColumns {
        public static final String TABLENAME = "USERS";
        public static final String FIRSTNAMECOLUMN = "FIRSTNAME";
        public static final String LASTNAMECOLUMN = "LASTNAME";
        public static final String EMAILCOLUMN = "EMAIL";
        public static final String ADMINCOLUMN = "ADMIN";
        public static final String USERIDCOLUMN = "USERID";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIRSTNAMECOLUMN + " TEXT NOT NULL, " +
                LASTNAMECOLUMN + " TEXT NOT NULL, " + EMAILCOLUMN + " TEXT NOT NULL, " + ADMINCOLUMN +
                " INTEGER(1) NOT NULL DEFAULT 0, "+USERIDCOLUMN+" TEXT NOT NULL UNIQUE, " +
                LASTMODIFIEDCOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP)";
    }

    public static class USERREQUESTSENTRY implements BaseColumns {
        public static final String TABLENAME = "USERREQUESTS";
        public static final String ITEMNAMECOLUMN = "ITEMNAME";
        public static final String ITEMDESCRIPTIONCOLUMN = "ITEMDESCRIPTION";
        public static final String ADDEDCOLUMN = "ADDED";
        public static final String USERIDCOLUMN = "USERID";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEMNAMECOLUMN + " TEXT NOT NULL, " +
                ITEMDESCRIPTIONCOLUMN + " TEXT, " + ADDEDCOLUMN + " INTEGER NOT NULL DEFAULT 0, " +
                USERIDCOLUMN+" TEXT NOT NULL, "+ LASTMODIFIEDCOLUMN +
                " TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY ("+USERIDCOLUMN+")"+
                "REFERENCES "+ USERSENTRY.TABLENAME+"("+ USERSENTRY.USERIDCOLUMN+") ON UPDATE CASCADE ON DELETE CASCADE)";
    }

    public static class USERREQUESTIMAGESENTRY implements BaseColumns {
        public static final String TABLENAME = "USERREQUESTIMAGES";
        public static final String USERREQUESTIMAGECOLUMN = "USERREQUESTIMAGE";
        public static final String USERREQUESTIDCOLUMN = "USERREQUESTIDCOLUMN";
        public static final String MAINIMAGECOLUMN="MAINIMAGE";
        public static final String LOCALPATHCOLUMN="LOCALPATH";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + USERREQUESTIMAGECOLUMN + " TEXT, " +
                USERREQUESTIDCOLUMN + " INTEGER NOT NULL, "+MAINIMAGECOLUMN+" INTEGER(1) NOT NULL DEFAULT 0, "+
                LOCALPATHCOLUMN+" TEXT, "+ LASTMODIFIEDCOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(" +
                USERREQUESTIDCOLUMN + ")"+ " REFERENCES " + USERREQUESTSENTRY.TABLENAME + "(" + USERREQUESTSENTRY._ID +
                ") ON UPDATE CASCADE ON DELETE CASCADE)";
    }

    public static class ITEMCLICKSENTRY implements BaseColumns {
        public static final String TABLENAME = "ITEMCLICKS";
        public static final String ITEMIDCOLUMN = "ITEMID";
        public static final String USERIDCOLUMN = "USERID";
        public static final String TIMECOLUMN = "TIME";

        public static final String CREATETABLE = "CREATE TABLE " + TABLENAME + "(" + _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEMIDCOLUMN + " INTEGER NOT NULL, " +
                USERIDCOLUMN + " TEXT NOT NULL, " + TIMECOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                LASTMODIFIEDCOLUMN + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                " FOREIGN KEY ("+ITEMIDCOLUMN+")"+"REFERENCES "+ ITEMSENTRY.TABLENAME+"("+
                ITEMSENTRY._ID+") ON UPDATE CASCADE ON DELETE CASCADE)";
    }

    public static String getCategoryToCategoryImage(){
        return CATEGORIESENTRY.TABLENAME+" INNER JOIN "+ CATEGORYIMAGESENTRY.TABLENAME+" ON "+ CATEGORIESENTRY.TABLENAME+
                "."+ CATEGORIESENTRY._ID+"="+ CATEGORYIMAGESENTRY.CATEGORYIDIDCOLUMN;
    }

    public static String getUserRequestToUserToUser(){
        return USERREQUESTSENTRY.TABLENAME+" INNER JOIN "+ USERSENTRY.TABLENAME+" ON "+ USERSENTRY.TABLENAME+
                "."+ USERSENTRY.USERIDCOLUMN+"="+ USERREQUESTSENTRY.TABLENAME+"."+ USERREQUESTSENTRY.USERIDCOLUMN;
    }

    public static String getItemToCategoriesToCategoryImages(){
        return ITEMSENTRY.TABLENAME+" INNER JOIN "+ CATEGORIESENTRY.TABLENAME+" ON "+ ITEMSENTRY.TABLENAME+
                "."+ ITEMSENTRY.CATEGORYIDCOLUMN+"="+ CATEGORIESENTRY.TABLENAME+"."+ CATEGORIESENTRY._ID+
                " INNER JOIN "+ CATEGORYIMAGESENTRY.TABLENAME+ " ON "+ CATEGORYIMAGESENTRY.TABLENAME+"."+
                CATEGORYIMAGESENTRY.CATEGORYIDIDCOLUMN + "="+ ITEMSENTRY.TABLENAME+"."+ ITEMSENTRY.CATEGORYIDCOLUMN;
    }

}
