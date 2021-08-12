package co.ke.tonyoa.android.vossified.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import co.ke.tonyoa.android.vossified.POJOs.Image;
import co.ke.tonyoa.android.vossified.POJOs.ItemClick;
import co.ke.tonyoa.android.vossified.POJOs.User;
import co.ke.tonyoa.android.vossified.POJOs.UserRequest;


import static co.ke.tonyoa.android.vossified.Data.VossitContract.BASEURI;
import static co.ke.tonyoa.android.vossified.Utils.getStringFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getSystemTime;
import static co.ke.tonyoa.android.vossified.Utils.hasNetwork;


public class VossitProvider extends ContentProvider {

    public static final String INSERT = "INSERT";
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";

    public static final int MATCHCATEGORIES = 1;
    public static final int MATCHCATEGORYIMAGES = 2;
    public static final int MATCHITEMS = 3;
    public static final int MATCHITEMIMAGES = 4;
    public static final int MATCHITEMLINKS = 5;
    public static final int MATCHUSERS = 6;
    public static final int MATCHUSERREQUESTS = 7;
    public static final int MATCHUSERREQUESTIMAGES = 8;
    public static final int MATCHITEMCLICKS = 9;

    public static final int MATCHCATEGORIESTOCATEGORYIMAGES=40;
    public static final int MATCHUSERREQUESTSTOUSERSTOUSERREQUESTIMAGES=41;
    public static final int MATCHITEMSTOCATEGORIESTOCATEGORYIMAGES =42;

    public static final int NONETWORK = -99;

    public static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.CATEGORIESENTRY.TABLENAME, MATCHCATEGORIES);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.CATEGORYIMAGESENTRY.TABLENAME, MATCHCATEGORYIMAGES);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMSENTRY.TABLENAME, MATCHITEMS);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMIMAGESENTRY.TABLENAME, MATCHITEMIMAGES);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMLINKSENTRY.TABLENAME, MATCHITEMLINKS);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.USERSENTRY.TABLENAME, MATCHUSERS);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.USERREQUESTSENTRY.TABLENAME, MATCHUSERREQUESTS);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.USERREQUESTIMAGESENTRY.TABLENAME, MATCHUSERREQUESTIMAGES);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMCLICKSENTRY.TABLENAME, MATCHITEMCLICKS);

        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.CATEGORIESENTRY.TABLENAME + "/#", MATCHCATEGORIES + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.CATEGORYIMAGESENTRY.TABLENAME + "/#", MATCHCATEGORYIMAGES + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMSENTRY.TABLENAME + "/#", MATCHITEMS + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMIMAGESENTRY.TABLENAME + "/#", MATCHITEMIMAGES + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMLINKSENTRY.TABLENAME + "/#", MATCHITEMLINKS + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.USERSENTRY.TABLENAME + "/#", MATCHUSERS + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.USERREQUESTSENTRY.TABLENAME + "/#", MATCHUSERREQUESTS + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.USERREQUESTIMAGESENTRY.TABLENAME + "/#", MATCHUSERREQUESTIMAGES + 20);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.ITEMCLICKSENTRY.TABLENAME + "/#", MATCHITEMCLICKS + 20);

        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.getCategoryToCategoryImage(), MATCHCATEGORIESTOCATEGORYIMAGES);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.getUserRequestToUserToUser(), MATCHUSERREQUESTSTOUSERSTOUSERREQUESTIMAGES);
        uriMatcher.addURI(VossitContract.AUTHORITY, VossitContract.getItemToCategoriesToCategoryImages(), MATCHITEMSTOCATEGORIESTOCATEGORYIMAGES);
    }

    private VossitOpenHelper dbOpenHelper;
    private ConnectivityManager connectivityManager;

    public VossitProvider() {

    }

    public static void saveOnline(Context context, final String transactionType, final Uri uri, final SQLiteDatabase database) {
        String[] pathAndId = {};
        if (uri.getPath() != null) {
            pathAndId = uri.getPath().substring(1).split("/");
        }
        Cursor cursor;
        cursor = database.query(pathAndId[0], null, VossitContract.ITEMSENTRY._ID + "=?",
                new String[]{pathAndId[1]}, null, null, null);
        //If the item does not exist in the online database and it was not deleted, return
        if (!cursor.moveToFirst() && !transactionType.equals(DELETE)) {
            return;
        }
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        long lastModified;
        //Set the lastModified to the time it was inserted in the db,
        lastModified =getSystemTime(database);
        final Task<Void>[] saveTask = new Task[]{null};
        int uriMatcherValue = uriMatcher.match(uri) - 20;
        //If an item was inserted, set its lastModified to now for other devices to be able to sync and get it
        if (transactionType.equals(INSERT) || transactionType.equals(UPDATE)) {
            //Update the time to the local database
            ContentValues contentValues = new ContentValues();
            contentValues.put(VossitContract.LASTMODIFIEDCOLUMN, getStringFromTimeStamp(lastModified));
            database.update(pathAndId[0], contentValues, VossitContract.ITEMSENTRY._ID + "=?", new String[]{pathAndId[1]});
            switch (uriMatcherValue) {
                case MATCHITEMIMAGES:
                    break;
                case MATCHUSERS:
                    if (transactionType.equals(INSERT)){
                        saveUserOnline(cursor, firebaseFirestore, lastModified, saveTask);
                    }
                    break;
                case MATCHUSERREQUESTS:
                    saveUserRequestOnline(cursor, firebaseFirestore, lastModified, saveTask);
                    break;
                case MATCHUSERREQUESTIMAGES:
                    saveImageOnline(database, cursor, firebaseFirestore, firebaseStorage, lastModified, saveTask, VossitContract.USERREQUESTIMAGESENTRY.TABLENAME, transactionType);
                    break;
                case MATCHITEMCLICKS:
                    saveItemClickOnline(cursor, firebaseFirestore, lastModified, saveTask);
                    break;
            }
        }
        //Set a deleted item's lastModified to now so that other devices can sync
        else if (transactionType.equals(DELETE)) {
            String collectionPath = null;
            final DeletedEntry deletedEntry = new DeletedEntry(ContentUris.parseId(uri), lastModified, true);
            String url = null;
            switch (uriMatcherValue) {
                case MATCHCATEGORIES:
                    break;
                case MATCHCATEGORYIMAGES:
                    break;
                case MATCHITEMS:
                    break;
                case MATCHITEMIMAGES:
                    break;
                case MATCHITEMLINKS:
                    collectionPath = VossitContract.ITEMLINKSENTRY.TABLENAME;
                    break;
                case MATCHUSERS:
                    break;
                case MATCHUSERREQUESTS:
                    break;
                case MATCHUSERREQUESTIMAGES:
                    break;
                case MATCHITEMCLICKS:
                    break;
            }
            final String finalCollectionPath = collectionPath;
            if (finalCollectionPath != null) {
                if (url != null) {
                    final String finalUrl = url;
                    firebaseFirestore.collection(finalCollectionPath).document(ContentUris.parseId(uri) + "").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String urlString = documentSnapshot.getString(finalUrl);
                                StorageReference storageReference = null;
                                //Delete the stored image from Firebase Storage
                                if (urlString != null && !urlString.trim().isEmpty()) {
                                    storageReference = firebaseStorage.getReferenceFromUrl(urlString);
                                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            saveTask[0] = firebaseFirestore.collection(finalCollectionPath).document(ContentUris.parseId(uri) + "").set(deletedEntry);
                                        }
                                    });
                                }
                                else {
                                    saveTask[0] = firebaseFirestore.collection(finalCollectionPath).document(ContentUris.parseId(uri) + "").set(deletedEntry);
                                }
                            }
                        }
                    });
                } else {
                    saveTask[0] = firebaseFirestore.collection(finalCollectionPath).document(ContentUris.parseId(uri) + "").set(deletedEntry);
                }
            }
        }
        cursor.close();
    }


    private static void saveUserOnline(Cursor cursor, FirebaseFirestore firebaseFirestore, long systemTime, Task<Void>[] saveTask) {
        User user = new User(cursor);
        user.setLastModified(systemTime);
        DocumentReference userDocument = firebaseFirestore.collection(VossitContract.USERSENTRY.TABLENAME).document(user.getId() + "");
        saveTask[0] = userDocument.set(user);
    }

    private static void saveUserRequestOnline(Cursor cursor, FirebaseFirestore firebaseFirestore, long systemTime, Task<Void>[] saveTask) {
        UserRequest userRequest = new UserRequest(cursor);
        userRequest.setLastModified(systemTime);
        DocumentReference userRequestDocument = firebaseFirestore.collection(VossitContract.USERREQUESTSENTRY.TABLENAME).document(userRequest.getId() + "");
        saveTask[0] = userRequestDocument.set(userRequest);
    }

    private static void saveItemClickOnline(Cursor cursor, FirebaseFirestore firebaseFirestore, long systemTime, Task<Void>[] saveTask) {
        ItemClick itemClick = new ItemClick(cursor);
        itemClick.setLastModified(systemTime);
        itemClick.setTime(systemTime);
        DocumentReference userRequestDocument = firebaseFirestore.collection(VossitContract.ITEMCLICKSENTRY.TABLENAME).document(itemClick.getId() + "");
        saveTask[0] = userRequestDocument.set(itemClick);
    }

    private static void saveImageOnline(final SQLiteDatabase database,
                                        Cursor cursor, FirebaseFirestore firebaseFirestore,
                                        FirebaseStorage firebaseStorage, long systemTime,
                                        final Task<Void>[] saveTask, final String tablename, String transactionType) {
        int imageType = Image.CATEGORYIMAGE;
        if (tablename.equals(VossitContract.ITEMIMAGESENTRY.TABLENAME))
            imageType = Image.ITEMIMAGE;
        else if (tablename.equals(VossitContract.USERREQUESTIMAGESENTRY.TABLENAME))
            imageType = Image.USERREQUESTIMAGE;
        final Image image = new Image(cursor, imageType);
        image.setLastModified(systemTime);
        final DocumentReference imageDocument = firebaseFirestore.collection(tablename).document(image.getId() + "");
        if (transactionType.equals(INSERT) || tablename.equals(VossitContract.CATEGORYIMAGESENTRY.TABLENAME)) {
            try {
                File imageFile = new File(new URI(image.getLocalPath()));
                StorageReference imageStorageReference = firebaseStorage.getReference().
                        child(tablename + "/" + image.getId());
                imageStorageReference.putFile(Uri.fromFile(imageFile)).
                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //Update the url of the image once it is received
                                        image.setLink(uri.toString());
                                        ContentValues contentValues = new ContentValues();
                                        String column = "";
                                        if (tablename.equals(VossitContract.ITEMIMAGESENTRY.TABLENAME)) {
                                            column = VossitContract.ITEMIMAGESENTRY.ITEMIMAGECOLUMN;
                                        } else if (tablename.equals(VossitContract.CATEGORYIMAGESENTRY.TABLENAME)) {
                                            column = VossitContract.CATEGORYIMAGESENTRY.CATEGORYIMAGECOLUMN;
                                        } else if (tablename.equals(VossitContract.USERREQUESTIMAGESENTRY.TABLENAME)) {
                                            column = VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIMAGECOLUMN;
                                        }
                                        contentValues.put(column, uri.toString());
                                        database.update(tablename, contentValues,
                                                VossitContract.ITEMSENTRY._ID + "=?", new String[]{image.getId() + ""});
                                        saveTask[0] = imageDocument.set(image);
                                    }
                                });
                            }
                        });
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        else {
            saveTask[0] = imageDocument.set(image);
        }
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = new VossitOpenHelper(getContext());
        connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        //If querying for multiple rows
        if (uriMatcher.match(uri) < 20) {
            cursor = database.query(uri.getPath().substring(1), projection, selection, selectionArgs,
                    null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        //If querying for a known row
        else if (uriMatcher.match(uri) < 40) {
            String[] pathAndId = uri.getPath().substring(1).split("/");
            String table = pathAndId[0];
            String[] id = new String[]{pathAndId[1]};
            cursor = database.query(table, projection, VossitContract.ITEMSENTRY._ID + "=?", id,
                    null, null, sortOrder);
        }
        else if (uriMatcher.match(uri)==MATCHCATEGORIESTOCATEGORYIMAGES ||
                uriMatcher.match(uri)==MATCHUSERREQUESTSTOUSERSTOUSERREQUESTIMAGES ||
                uriMatcher.match(uri)== MATCHITEMSTOCATEGORIESTOCATEGORYIMAGES){
            cursor = database.query(uri.getPath().substring(1),
                    projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        long id;
        String table = uri.getPath().substring(1);
        try {
            id = database.insertOrThrow(table, null, values);
        } catch (SQLiteConstraintException ex) {
            ex.printStackTrace();
            id = -2;
        }
        if (id > -1) {
            getContext().getContentResolver().notifyChange(uri, null);

            //Notify caller of changes
            getContext().getContentResolver().notifyChange(uri, null);
            notifyChangeMultipleRows(uri);

            //If an item is being inserted from the device, save it online if there is a connection
            if (values != null && !values.containsKey(VossitContract.LASTMODIFIEDCOLUMN)) {
                if (hasNetwork(connectivityManager)) {
                    FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
                    long finalId = id;
                    firebaseFirestore.collection(table).document(
                            id + "").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists()) {
                                saveOnline(getContext(), INSERT, ContentUris.withAppendedId(uri, finalId),
                                        database);
                            }
                            else {
                                firebaseFirestore
                                        .collection(table)
                                        .orderBy("id", Query.Direction.DESCENDING)
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(new LastIdCompleteListener(
                                                new DbTransaction(-1, table, finalId, INSERT, getSystemTime(database)),
                                                database));
                            }
                        }
                    });
                }
                else {
                    return ContentUris.withAppendedId(uri, NONETWORK);
                }
            }
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (!hasNetwork(connectivityManager))
            return NONETWORK;
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        //If a particular row is being updated
        if (uriMatcher.match(uri) >= 20) {
            String[] pathAndId = uri.getPath().substring(1).split("/");
            String table = pathAndId[0];
            String[] id = new String[]{pathAndId[1]};

            int affected;
            try {
                affected = database.update(table, values, VossitContract.ITEMSENTRY._ID + "=?", id);
            }
            catch (SQLiteConstraintException ex) {
                ex.printStackTrace();
                affected = -1;
            }
            //If one or more rows have been updated
            if (affected > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
                notifyChangeSingleRow(uri);

                //If the updates come from the local device, save them online if there is a connection, or enqueue them for sync
                if (values != null && !values.containsKey(VossitContract.LASTMODIFIEDCOLUMN)) {
                    if (hasNetwork(connectivityManager)) {
                        saveOnline(getContext(), UPDATE, uri, database);
                    } else {
                        return NONETWORK;
                    }
                }
            }
            return affected;
        }
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!hasNetwork(connectivityManager))
            return NONETWORK;
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        //If deleting a particular row
        if (uriMatcher.match(uri) >= 20) {
            String[] pathAndId = uri.getPath().substring(1).split("/");
            String table = pathAndId[0];
            String[] id = new String[]{pathAndId[1]};

            int uriMatching = uriMatcher.match(uri) - 20;
            Map<String, List<Long>> affectedTables = new HashMap<>();
            //Select the rows of tables having the affected rows as a foreign key for deletion
            if (uriMatching == MATCHCATEGORIES) {
                affectedCascade(id, affectedTables, VossitContract.CATEGORYIMAGESENTRY.TABLENAME,
                        VossitContract.CATEGORYIMAGESENTRY.CATEGORYIDIDCOLUMN, VossitContract.CATEGORYIMAGESENTRY._ID);
                affectedCascade(id, affectedTables, VossitContract.ITEMSENTRY.TABLENAME,
                        VossitContract.ITEMSENTRY.CATEGORYIDCOLUMN, VossitContract.ITEMSENTRY._ID);
            }
            else if (uriMatching == MATCHITEMS) {
                affectedCascade(id, affectedTables, VossitContract.ITEMIMAGESENTRY.TABLENAME,
                        VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN, VossitContract.ITEMIMAGESENTRY._ID);
                affectedCascade(id, affectedTables, VossitContract.ITEMLINKSENTRY.TABLENAME,
                        VossitContract.ITEMLINKSENTRY.ITEMIDCOLUMN, VossitContract.ITEMLINKSENTRY._ID);
                affectedCascade(id, affectedTables, VossitContract.ITEMCLICKSENTRY.TABLENAME,
                        VossitContract.ITEMCLICKSENTRY.ITEMIDCOLUMN, VossitContract.ITEMCLICKSENTRY._ID);
            }
            else if (uriMatching == MATCHUSERS) {
                affectedCascade(id, affectedTables, VossitContract.USERREQUESTSENTRY.TABLENAME,
                        VossitContract.USERREQUESTSENTRY.USERIDCOLUMN, VossitContract.USERREQUESTSENTRY._ID);
            }

            int affected;
            try {
                affected = database.delete(table, VossitContract.ITEMSENTRY._ID + "=?", id);
            }
            catch (SQLiteConstraintException ex) {
                ex.printStackTrace();
                affected = -2;
            }
            //If one or more rows are deleted
            if (affected > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
                notifyChangeSingleRow(uri);

                //Delete the items online if there is a connection, or enqueue them for sync
                if (hasNetwork(connectivityManager)) {
                    saveOnline(getContext(), DELETE, uri, database);
                } else {
                    affected=NONETWORK;
                }
                //Delete all the entries in other tables having the deleted row as a foreign key
                saveAffectedDeletes(database, affectedTables);
            }
            return affected;
        }
        //If multiple rows are to be deleted
        else {
            int affected;

            Map<String, List<Long>> affectedTables = new HashMap<>();
            //Select the rows of tables having the affected rows as a foreign key for deletion using the selection criteria
            if (uriMatcher.match(uri) == MATCHCATEGORIES) {
                if (selectionArgs != null) {
                    for (String id : selectionArgs) {
                        affectedCascade(new String[]{id}, affectedTables, VossitContract.CATEGORYIMAGESENTRY.TABLENAME,
                                VossitContract.CATEGORYIMAGESENTRY.CATEGORYIDIDCOLUMN, VossitContract.CATEGORYIMAGESENTRY._ID);
                        affectedCascade(new String[]{id}, affectedTables, VossitContract.ITEMSENTRY.TABLENAME,
                                VossitContract.ITEMSENTRY.CATEGORYIDCOLUMN, VossitContract.ITEMSENTRY._ID);
                    }
                }
            }
            else if (uriMatcher.match(uri) == MATCHITEMS) {
                if (selectionArgs != null) {
                    for (String id : selectionArgs) {
                        affectedCascade(new String[]{id}, affectedTables, VossitContract.ITEMIMAGESENTRY.TABLENAME,
                                VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN, VossitContract.ITEMIMAGESENTRY._ID);
                        affectedCascade(new String[]{id}, affectedTables, VossitContract.ITEMLINKSENTRY.TABLENAME,
                                VossitContract.ITEMLINKSENTRY.ITEMIDCOLUMN, VossitContract.ITEMLINKSENTRY._ID);
                        affectedCascade(new String[]{id}, affectedTables, VossitContract.ITEMCLICKSENTRY.TABLENAME,
                                VossitContract.ITEMCLICKSENTRY.ITEMIDCOLUMN, VossitContract.ITEMCLICKSENTRY._ID);
                    }
                }
            }
            else if (uriMatcher.match(uri) == MATCHUSERS) {
                if (selectionArgs != null) {
                    for (String id : selectionArgs) {
                        affectedCascade(new String[]{id}, affectedTables, VossitContract.USERREQUESTSENTRY.TABLENAME,
                                VossitContract.USERREQUESTSENTRY.USERIDCOLUMN, VossitContract.USERREQUESTSENTRY._ID);
                    }
                }
            }

            try {
                affected = database.delete(uri.getPath().substring(1), selection, selectionArgs);
            } catch (SQLiteConstraintException ex) {
                ex.printStackTrace();
                affected = -2;
            }
            //If one or more rows were deleted
            if (affected > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
                notifyChangeMultipleRows(uri);

                //Delete the items online if there is a connection, or enqueue them for sync
                if (hasNetwork(connectivityManager)) {
                    if (selectionArgs != null) {
                        for (String i : selectionArgs) {
                            saveOnline(getContext(), DELETE, ContentUris.withAppendedId(uri, Long.valueOf(i)), database);
                        }
                    }
                } else {
                    affected=NONETWORK;
                }

                //Delete all the entries in other tables having the deleted row as a foreign key
                saveAffectedDeletes(database, affectedTables);
            }
            return affected;
        }
    }

    private void notifyChangeSingleRow(Uri uri) {
        int match = uriMatcher.match(uri) - 20;
        if (match ==MATCHCATEGORIES || match ==MATCHCATEGORYIMAGES){
            getContext().getContentResolver().notifyChange(Uri.withAppendedPath(BASEURI,
                    VossitContract.getCategoryToCategoryImage()), null);
        }
        else if (match ==MATCHUSERS || match ==MATCHUSERREQUESTS || match ==MATCHUSERREQUESTIMAGES){
            getContext().getContentResolver().notifyChange(Uri.withAppendedPath(BASEURI,
                    VossitContract.getUserRequestToUserToUser()), null);
        }
        else if (match ==MATCHITEMS || match ==MATCHITEMIMAGES){
            getContext().getContentResolver().notifyChange(Uri.withAppendedPath(BASEURI,
                    VossitContract.getItemToCategoriesToCategoryImages()), null);
        }
    }

    private void notifyChangeMultipleRows(Uri uri) {
        int match = uriMatcher.match(uri);
        if (match ==MATCHCATEGORIES || match ==MATCHCATEGORIESTOCATEGORYIMAGES){
            getContext().getContentResolver().notifyChange(Uri.withAppendedPath(BASEURI,
                    VossitContract.getCategoryToCategoryImage()), null);
        }
        else if (match ==MATCHUSERS || match ==MATCHUSERREQUESTS || match ==MATCHUSERREQUESTIMAGES){
            getContext().getContentResolver().notifyChange(Uri.withAppendedPath(BASEURI,
                    VossitContract.getUserRequestToUserToUser()), null);
        }
        else if (match ==MATCHITEMS || match ==MATCHITEMIMAGES){
            getContext().getContentResolver().notifyChange(Uri.withAppendedPath(BASEURI,
                    VossitContract.getItemToCategoriesToCategoryImages()), null);
        }
    }

    private boolean affectedCascade(String[] id, Map<String, List<Long>> affectedTables,
                                    String tablename, String foreignKeyColumn, String objectId) {
        //Fetch rows having the affected row as a foreign key
        Cursor affectedItems = query(Uri.withAppendedPath(BASEURI, tablename),
                new String[]{objectId}, foreignKeyColumn + "=?", id, null);
        if (affectedItems!=null) {
            if (affectedItems.getCount() > 0) {
                List<Long> rows = new ArrayList<>();
                //Add the primary keys of the child rows to a list, and add the list in a map
                while (affectedItems.moveToNext()) {
                    rows.add(affectedItems.getLong(affectedItems.getColumnIndex(objectId)));
                }
                affectedTables.put(tablename, rows);
                return true;
            }
            affectedItems.close();
        }
        return false;
    }

    private void saveAffectedDeletes(SQLiteDatabase database, Map<String, List<Long>> affectedTables) {
        Iterator<Map.Entry<String, List<Long>>> iterator = affectedTables.entrySet().iterator();
        //Loop over all affected tables
        while (iterator.hasNext()) {
            Map.Entry<String, List<Long>> entry = iterator.next();
            //Loop over all affected rows in a table
            for (long affectedId : entry.getValue()) {
                Uri affectedUri = ContentUris.withAppendedId(Uri.withAppendedPath(BASEURI, entry.getKey()), affectedId);
                //If there is a connection, delete the row online, or enqueue for sync
                if (hasNetwork(connectivityManager)) {
                    saveOnline(getContext(), DELETE, affectedUri, database);
                }
            }
            iterator.remove();
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //Model of a deleted entry to enable syncing in other devices
    @Keep
    static class DeletedEntry {
        @PropertyName("id")
        private long id;
        @PropertyName("lm")
        private long lastModified;
        @PropertyName("del")
        private boolean del;

        public DeletedEntry() {

        }

        public DeletedEntry(long id, long lastModified, boolean del) {
            this.id = id;
            this.lastModified = lastModified;
            this.del = del;
        }

        @PropertyName("id")
        public long getId() {
            return id;
        }

        @PropertyName("id")
        public void setId(long id) {
            this.id = id;
        }

        @PropertyName("lm")
        public long getLastModified() {
            return lastModified;
        }

        @PropertyName("lm")
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        @PropertyName("del")
        public boolean isDel() {
            return del;
        }

        @PropertyName("del")
        public void setDel(boolean del) {
            this.del = del;
        }
    }

    static class DbTransaction {
        private long id;
        private String affectedTable;
        private long affectedRow;
        private String transactionType;
        private long lastModified;

        public DbTransaction(long id, String affectedTable, long affectedRow, String transactionType, long lastModified) {
            this.id = id;
            this.affectedTable = affectedTable;
            this.affectedRow = affectedRow;
            this.transactionType = transactionType;
            this.lastModified = lastModified;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getAffectedTable() {
            return affectedTable;
        }

        public void setAffectedTable(String affectedTable) {
            this.affectedTable = affectedTable;
        }

        public long getAffectedRow() {
            return affectedRow;
        }

        public void setAffectedRow(long affectedRow) {
            this.affectedRow = affectedRow;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this)
                return true;
            if (obj instanceof DbTransaction) {
                DbTransaction dbTransaction = (DbTransaction) obj;
                return (dbTransaction.getAffectedTable().equals(affectedTable)) && (dbTransaction.getAffectedRow() == affectedRow) && (dbTransaction.getTransactionType().equals(transactionType));
            }
            return false;
        }
    }

    class LastIdCompleteListener implements OnCompleteListener<QuerySnapshot> {

        private DbTransaction dbTransaction;
        private SQLiteDatabase database;

        public LastIdCompleteListener(DbTransaction dbTransaction, SQLiteDatabase database) {
            this.dbTransaction = dbTransaction;
            this.database = database;
        }

        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful() && task.getResult() != null) {
                long latestId = (Long) task.getResult().getDocuments().get(0).get("id");
                long newId = latestId + 1;
                ContentValues contentValues = new ContentValues();
                contentValues.put(VossitContract.ITEMSENTRY._ID, newId);
                getContext().getContentResolver().update(ContentUris.withAppendedId(Uri.withAppendedPath(BASEURI,
                        dbTransaction.getAffectedTable()), dbTransaction.getAffectedRow()), contentValues, null, null);
                saveOnline(getContext(), dbTransaction.getTransactionType(), ContentUris.withAppendedId(Uri.withAppendedPath(BASEURI,
                        dbTransaction.getAffectedTable()), newId), database);
            }
        }
    }

}
