package co.ke.tonyoa.android.vossified.POJOs;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.Exclude;

import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;

public class Category {

    private long id = -1;
    private String name;
    private Long lastModified;

    public Category(){

    }

    public Category(int id, String name, long lastModified){
        this.id = id;
        this.name = name;
        this.lastModified = lastModified;
    }

    public Category(Cursor cursor){
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.CATEGORIESENTRY._ID))) {
            id = cursor.getLong(cursor.getColumnIndex(VossitContract.CATEGORIESENTRY._ID));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN))) {
            name = cursor.getString(cursor.getColumnIndex(VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))) {
            lastModified = getDateInUtcFromTimeStamp(cursor.getString(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))).getTime();
        }
    }

    @Exclude
    public ContentValues getContentValues(boolean withId) {
        ContentValues contentValues = new ContentValues();
        if (withId)
            contentValues.put(VossitContract.CATEGORIESENTRY._ID, getId());
        contentValues.put(VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN, getName());
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

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    @Exclude
    @NonNull
    @Override
    public String toString() {
        return getName();
    }

    @Exclude
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Category) {
            Category category = (Category) obj;
            return category.getName().equals(name);
        }
        return false;
    }
}
