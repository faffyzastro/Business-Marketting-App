package co.ke.tonyoa.android.vossified.POJOs;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.Exclude;

import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;


public class Link {

    private long id;
    private String link;
    private long itemId;
    private String store;
    private Long lastModified;
    @Exclude
    private boolean modified;

    public Link(){

    }

    public Link(long id, String link, long itemId, String store, long lastModified){
        this.id = id;
        this.link = link;
        this.itemId = itemId;
        this.store = store;
        this.lastModified = lastModified;
    }

    public Link(Cursor cursor){
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMLINKSENTRY.ITEMLINKCOLUMN))) {
            link = cursor.getString(cursor.getColumnIndex(VossitContract.ITEMLINKSENTRY.ITEMLINKCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMLINKSENTRY.STORECOLUMN))) {
            store = cursor.getString(cursor.getColumnIndex(VossitContract.ITEMLINKSENTRY.STORECOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMLINKSENTRY.ITEMIDCOLUMN))) {
            itemId =cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMLINKSENTRY.ITEMIDCOLUMN));
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
        contentValues.put(VossitContract.ITEMLINKSENTRY.ITEMLINKCOLUMN, getLink());
        contentValues.put(VossitContract.ITEMLINKSENTRY.STORECOLUMN, getStore());
        contentValues.put(VossitContract.ITEMLINKSENTRY.ITEMIDCOLUMN, getItemId());
        contentValues.put(VossitContract.LASTMODIFIEDCOLUMN, getStringFromTimeStamp(getLastModified()));
        return contentValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    @Exclude
    public boolean isModified() {
        return modified;
    }

    @Exclude
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Exclude
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Link) {
            Link link = (Link) obj;
            return link.getItemId() == itemId && link.getLink().equals(this.link);
        }
        return false;
    }

}
