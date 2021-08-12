package co.ke.tonyoa.android.vossified.POJOs;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.firestore.Exclude;

import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;

public class ItemClick {

    private long id;
    private long itemId;
    private String userId;
    private Long time;
    private Long lastModified;

    public ItemClick() {

    }

    public ItemClick(long id, long itemId, String userId, Long time, Long lastModified) {
        this.id = id;
        this.itemId = itemId;
        this.userId = userId;
        this.time = time;
        this.lastModified = lastModified;
    }

    public ItemClick(Cursor cursor){
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY.ITEMIDCOLUMN))) {
            itemId = cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY.ITEMIDCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY.USERIDCOLUMN))) {
            userId = cursor.getString(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY.USERIDCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY.TIMECOLUMN))) {
            time=cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY.TIMECOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY._ID))) {
            id = cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMCLICKSENTRY._ID));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))) {
            lastModified = getDateInUtcFromTimeStamp(cursor.getString(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))).getTime();
        }
    }

    @Exclude
    public ContentValues getContentValues(boolean withId) {
        ContentValues contentValues = new ContentValues();
        if (withId)
            contentValues.put(VossitContract.ITEMCLICKSENTRY._ID, getId());
        contentValues.put(VossitContract.ITEMCLICKSENTRY.ITEMIDCOLUMN, getItemId());
        contentValues.put(VossitContract.ITEMCLICKSENTRY.USERIDCOLUMN, getUserId());
        contentValues.put(VossitContract.ITEMCLICKSENTRY.TIMECOLUMN, getTime());
        contentValues.put(VossitContract.LASTMODIFIEDCOLUMN, getStringFromTimeStamp(getLastModified()));
        return contentValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }
}
