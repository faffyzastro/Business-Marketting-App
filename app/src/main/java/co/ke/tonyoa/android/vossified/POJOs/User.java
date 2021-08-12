package co.ke.tonyoa.android.vossified.POJOs;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.firestore.Exclude;

import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;


public class User {

    private long id;
    private String fName;
    private String lName;
    private String email;
    private boolean admin;
    private String userId;
    private Long lastModified;

    public User(){

    }

    public User(long id, String fName, String lName, String email, boolean admin, String userId, long lastModified){
        this.id = id;
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.admin = admin;
        this.userId = userId;
        this.lastModified = lastModified;
    }

    public User(Cursor cursor){
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERSENTRY.FIRSTNAMECOLUMN))) {
            fName = cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.FIRSTNAMECOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERSENTRY.LASTNAMECOLUMN))) {
            lName = cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.LASTNAMECOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERSENTRY.EMAILCOLUMN))) {
            email=cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.EMAILCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERSENTRY.ADMINCOLUMN))) {
            admin=cursor.getInt(cursor.getColumnIndex(VossitContract.USERSENTRY.ADMINCOLUMN))==1;
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERSENTRY.USERIDCOLUMN))) {
            userId=cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.USERIDCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMSENTRY._ID))) {
            id = cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMSENTRY._ID));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))) {
            lastModified = getDateInUtcFromTimeStamp(cursor.getString(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))).getTime();
        }
    }

    @Exclude
    public ContentValues getContentValues(boolean withId) {
        ContentValues contentValues = new ContentValues();
        if (withId)
            contentValues.put(VossitContract.ITEMSENTRY._ID, getId());
        contentValues.put(VossitContract.USERSENTRY.FIRSTNAMECOLUMN, getfName());
        contentValues.put(VossitContract.USERSENTRY.LASTNAMECOLUMN, getlName());
        contentValues.put(VossitContract.USERSENTRY.EMAILCOLUMN, getEmail());
        contentValues.put(VossitContract.USERSENTRY.ADMINCOLUMN, isAdmin()?1:0);
        contentValues.put(VossitContract.USERSENTRY.USERIDCOLUMN, getUserId());
        contentValues.put(VossitContract.LASTMODIFIEDCOLUMN, getStringFromTimeStamp(getLastModified()));
        return contentValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
