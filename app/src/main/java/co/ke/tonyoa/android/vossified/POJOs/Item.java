package co.ke.tonyoa.android.vossified.POJOs;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.firestore.Exclude;

import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;


public class Item {

    private long id;
    private String name;
    private String description;
    private float cost;
    private long categoryId;
    private Long lastModified;

    public Item(){

    }

    public Item(long id, String name, String description, float cost, long categoryId, long lastModified){
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.categoryId = categoryId;
        this.lastModified = lastModified;
    }

    public Item(Cursor cursor){
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMNAMECOLUMN))) {
            name = cursor.getString(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMNAMECOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMDESCRIPTIONCOLUMN))) {
            description = cursor.getString(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMDESCRIPTIONCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN))) {
            cost=cursor.getFloat(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMSENTRY.CATEGORYIDCOLUMN))) {
            categoryId=cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMSENTRY.CATEGORYIDCOLUMN));
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
        contentValues.put(VossitContract.ITEMSENTRY.ITEMNAMECOLUMN, getName());
        contentValues.put(VossitContract.ITEMSENTRY.ITEMDESCRIPTIONCOLUMN, getDescription());
        contentValues.put(VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN, getCost());
        contentValues.put(VossitContract.ITEMSENTRY.CATEGORYIDCOLUMN, getCategoryId());
        contentValues.put(VossitContract.LASTMODIFIEDCOLUMN, getStringFromTimeStamp(getLastModified()));
        return contentValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }
}
