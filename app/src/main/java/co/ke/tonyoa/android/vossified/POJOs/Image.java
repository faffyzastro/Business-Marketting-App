package co.ke.tonyoa.android.vossified.POJOs;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.firebase.firestore.Exclude;

import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;


public class Image {

    @Exclude
    public static final int CATEGORYIMAGE=0;
    @Exclude
    public static final int ITEMIMAGE=1;
    @Exclude
    public static final int USERREQUESTIMAGE=2;

    private long id;
    private String link;
    private long refId;
    private Long lastModified;
    private boolean mainImage;
    @Exclude
    private String localPath;
    @Exclude
    private boolean modified;

    public Image(){

    }

    public Image(long id, String link, long refId, boolean mainImage, long lastModified, String localPath){
        this.id = id;
        this.link = link;
        this.refId = refId;
        this.mainImage = mainImage;
        this.lastModified = lastModified;
        this.localPath = localPath;
    }

    public Image(Cursor cursor, int imageType){
        if (imageType==CATEGORYIMAGE) {
            link = cursor.getString(cursor.getColumnIndex(VossitContract.CATEGORYIMAGESENTRY.CATEGORYIMAGECOLUMN));
            refId=cursor.getLong(cursor.getColumnIndex(VossitContract.CATEGORYIMAGESENTRY.CATEGORYIDIDCOLUMN));
            localPath=cursor.getString(cursor.getColumnIndex(VossitContract.CATEGORYIMAGESENTRY.LOCALPATHCOLUMN));
        }
        if (imageType==ITEMIMAGE) {
            link = cursor.getString(cursor.getColumnIndex(VossitContract.ITEMIMAGESENTRY.ITEMIMAGECOLUMN));
            refId=cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN));
            mainImage=cursor.getInt(cursor.getColumnIndex(VossitContract.ITEMIMAGESENTRY.MAINIMAGECOLUMN))==1;
            localPath=cursor.getString(cursor.getColumnIndex(VossitContract.ITEMIMAGESENTRY.LOCALPATHCOLUMN));
        }
        if (imageType==USERREQUESTIMAGE) {
            link = cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIMAGECOLUMN));
            refId=cursor.getLong(cursor.getColumnIndex(VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIDCOLUMN));
            mainImage=cursor.getInt(cursor.getColumnIndex(VossitContract.USERREQUESTIMAGESENTRY.MAINIMAGECOLUMN))==1;
            localPath=cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTIMAGESENTRY.LOCALPATHCOLUMN));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.ITEMSENTRY._ID))) {
            id = cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMSENTRY._ID));
        }
        if (!cursor.isNull(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))) {
            lastModified = getDateInUtcFromTimeStamp(cursor.getString(cursor.getColumnIndex(VossitContract.LASTMODIFIEDCOLUMN))).getTime();
        }
    }

    @Exclude
    public ContentValues getContentValues(boolean withId, String foreignKey, String link, String localPath, String mainImage) {
        ContentValues contentValues = new ContentValues();
        if (withId)
            contentValues.put(VossitContract.ITEMSENTRY._ID, getId());
        if (mainImage!=null){
            contentValues.put(mainImage, isMainImage()?1:0);
        }
        contentValues.put(link, getLink());
        contentValues.put(foreignKey, getRefId());
        contentValues.put(localPath, getLocalPath());
        contentValues.put(VossitContract.LASTMODIFIEDCOLUMN, getStringFromTimeStamp(getLastModified()));
        return contentValues;
    }

    @Exclude
    public ContentValues getContentValues(boolean withId, String foreignKey, String link, String localPath) {
        return getContentValues(withId, foreignKey, link, localPath, null);
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

    public long getRefId() {
        return refId;
    }

    public void setRefId(long refId) {
        this.refId = refId;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isMainImage() {
        return mainImage;
    }

    public void setMainImage(boolean mainImage) {
        this.mainImage = mainImage;
        modified=true;
    }

    @Exclude
    public String getLocalPath() {
        return localPath;
    }

    @Exclude
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Exclude
    public boolean isModified() {
        return modified;
    }

    @Exclude
    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
