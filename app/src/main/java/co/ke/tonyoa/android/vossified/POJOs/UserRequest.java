package co.ke.tonyoa.android.vossified.POJOs;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.firestore.Exclude;

import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;


public class UserRequest {

    private long id;
    private String itemName;
    private String description;
    private String userId;
    private int added;
    private Long lastModified;

    public UserRequest(){

    }

    public UserRequest(long id, String itemName, String description, String userId, int added, long lastModified){
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.userId = userId;
        this.added = added;
        this.lastModified = lastModified;
    }

    public UserRequest(Cursor cursor){
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN))) {
            itemName = cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMDESCRIPTIONCOLUMN))) {
            description = cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMDESCRIPTIONCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN))) {
            added =cursor.getInt(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.USERIDCOLUMN))) {
            userId=cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.USERIDCOLUMN));
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
        contentValues.put(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN, getItemName());
        contentValues.put(VossitContract.USERREQUESTSENTRY.ITEMDESCRIPTIONCOLUMN, getDescription());
        contentValues.put(VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN, getAdded());
        contentValues.put(VossitContract.USERREQUESTSENTRY.USERIDCOLUMN, getUserId());
        contentValues.put(VossitContract.LASTMODIFIEDCOLUMN, getStringFromTimeStamp(getLastModified()));
        return contentValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }
}
