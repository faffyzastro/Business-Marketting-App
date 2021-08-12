package co.ke.tonyoa.android.vossified.Data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.CountDownLatch;


import co.ke.tonyoa.android.vossified.POJOs.Category;
import co.ke.tonyoa.android.vossified.POJOs.Image;
import co.ke.tonyoa.android.vossified.POJOs.Item;
import co.ke.tonyoa.android.vossified.POJOs.Link;
import co.ke.tonyoa.android.vossified.POJOs.User;
import co.ke.tonyoa.android.vossified.POJOs.UserRequest;
import co.ke.tonyoa.android.vossified.R;
import co.ke.tonyoa.android.vossified.Utils;

import static co.ke.tonyoa.android.vossified.Data.VossitContract.BASEURI;
import static co.ke.tonyoa.android.vossified.Data.VossitContract.LASTMODIFIEDCOLUMN;
import static co.ke.tonyoa.android.vossified.Utils.getDateInUtcFromTimeStamp;
import static co.ke.tonyoa.android.vossified.Utils.getSystemTime;


public class SyncWorker extends Worker {

    public static final String LAST_MODIFIED_CATEGORIES = "lastModifiedCategories";
    public static final String LAST_MODIFIED_CATEGORYIMAGES = "lastModifiedCategoryImages";
    public static final String LAST_MODIFIED_ITEMS = "lastModifiedItems";
    public static final String LAST_MODIFIED_ITEMIMAGES = "lastModifiedItemImages";
    public static final String LAST_MODIFIED_ITEMLINKS = "lastModifiedItemLinks";
    public static final String LAST_MODIFIED_USERREQUESTS = "lastModifiedUserRequests";
    public static final String LAST_MODIFIED_USERREQUESTIMAGES = "lastModifiedUserRequestImages";
    public static final String INITIAL_LOAD = "INITIAL_LOAD";
    private static final String LAST_MODIFIED_USERS = "lastModifiedUsers";

    public static boolean finishedSync;
    public static boolean finishedFirstCategories;

    private boolean finishedCategories;
    private boolean finishedCategoryImages;
    private boolean finishedItems;
    private boolean finishedItemImages;
    private boolean finishedItemLinks;
    private boolean finishedUsers;
    private boolean finishedUserRequests;
    private boolean finishedUserRequestImages;

    @NonNull
    private Context context;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private VossitOpenHelper databaseHelper;

    private ListenerRegistration categoriesRegistration;
    private ListenerRegistration categoryImagesRegistration;
    private ListenerRegistration itemsRegistration;
    private ListenerRegistration itemImagesRegistration;
    private ListenerRegistration itemLinksRegistration;
    private ListenerRegistration usersRegistration;
    private ListenerRegistration userRequestsRegistration;
    private ListenerRegistration userRequestImagesRegistration;

    private SharedPreferences sharedPreferences;

    private SettableFuture<Result> mFuture;

    private CountDownLatch countDownLatchCategories;
    private CountDownLatch countDownLatchItems;
    private CountDownLatch countDownLatchUsers;
    private CountDownLatch countDownLatchUserRequests;
    private boolean initialLoad;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.databaseHelper = new VossitOpenHelper(context);
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Result doWork() {
        finishedSync=false;
        countDownLatchCategories=new CountDownLatch(1);
        countDownLatchItems=new CountDownLatch(1);
        countDownLatchUsers=new CountDownLatch(1);
        countDownLatchUserRequests=new CountDownLatch(1);
        initialLoad=sharedPreferences.getBoolean(INITIAL_LOAD, true);
        mFuture = SettableFuture.create();
        if (categoriesRegistration != null) {
            categoriesRegistration.remove();
            categoryImagesRegistration.remove();
            itemsRegistration.remove();
            itemImagesRegistration.remove();
            itemLinksRegistration.remove();
            usersRegistration.remove();
            userRequestsRegistration.remove();
            userRequestImagesRegistration.remove();
        }

        //Fetch the lowest level table first, categories
        firebaseFirestore.collection(VossitContract.CATEGORIESENTRY.TABLENAME).whereGreaterThan("lastModified", sharedPreferences.getLong(LAST_MODIFIED_CATEGORIES,
                0)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                categoriesRegistration = firebaseFirestore.collection(VossitContract.CATEGORIESENTRY.TABLENAME).whereGreaterThan("lastModified",
                        sharedPreferences.getLong(LAST_MODIFIED_CATEGORIES, 0)).addSnapshotListener(getEventListener(VossitContract.CATEGORIESENTRY.TABLENAME));

                //Once categories is fetched, fetch tables depending on it, categoryImages and items
                categoryImagesRegistration = firebaseFirestore.collection(VossitContract.CATEGORYIMAGESENTRY.TABLENAME).whereGreaterThan("lastModified",
                        sharedPreferences.getLong(LAST_MODIFIED_CATEGORYIMAGES, 0)).addSnapshotListener(getEventListener(VossitContract.CATEGORYIMAGESENTRY.TABLENAME));

                firebaseFirestore.collection(VossitContract.ITEMSENTRY.TABLENAME).whereGreaterThan("lastModified",
                        sharedPreferences.getLong(LAST_MODIFIED_ITEMS, 0)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //Once low level items table is fetched, fetch tables depending on it, itemImages and itemLinks
                        itemsRegistration = firebaseFirestore.collection(VossitContract.ITEMSENTRY.TABLENAME).whereGreaterThan("lastModified",
                                sharedPreferences.getLong(LAST_MODIFIED_ITEMS, 0)).addSnapshotListener(getEventListener(VossitContract.ITEMSENTRY.TABLENAME));
                        itemImagesRegistration = firebaseFirestore.collection(VossitContract.ITEMIMAGESENTRY.TABLENAME).whereGreaterThan("lastModified",
                                sharedPreferences.getLong(LAST_MODIFIED_ITEMIMAGES, 0)).addSnapshotListener(getEventListener(VossitContract.ITEMIMAGESENTRY.TABLENAME));
                        itemLinksRegistration = firebaseFirestore.collection(VossitContract.ITEMLINKSENTRY.TABLENAME).whereGreaterThan("lastModified",
                                sharedPreferences.getLong(LAST_MODIFIED_ITEMLINKS, 0)).addSnapshotListener(getEventListener(VossitContract.ITEMLINKSENTRY.TABLENAME));
                    }
                });
            }
        });

        String userId= FirebaseAuth.getInstance().getUid();
        if (userId==null) {
            userId="()";
        }
        String finalUserId = userId;

        firebaseFirestore.collection(VossitContract.USERSENTRY.TABLENAME).whereEqualTo("userId", userId).whereGreaterThan("lastModified", sharedPreferences.getLong(LAST_MODIFIED_USERS,
                0)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                usersRegistration = firebaseFirestore.collection(VossitContract.USERSENTRY.TABLENAME).whereEqualTo("userId", finalUserId).whereGreaterThan("lastModified",
                        sharedPreferences.getLong(LAST_MODIFIED_USERS, 0)).addSnapshotListener(getEventListener(VossitContract.USERSENTRY.TABLENAME));

                firebaseFirestore.collection(VossitContract.USERREQUESTSENTRY.TABLENAME).whereEqualTo("userId", finalUserId).whereGreaterThan("lastModified",
                        sharedPreferences.getLong(LAST_MODIFIED_USERREQUESTS, 0)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        userRequestsRegistration = firebaseFirestore.collection(VossitContract.USERREQUESTSENTRY.TABLENAME).whereEqualTo("userId", finalUserId).whereGreaterThan("lastModified",
                                sharedPreferences.getLong(LAST_MODIFIED_USERREQUESTS, 0)).addSnapshotListener(getEventListener(VossitContract.USERREQUESTSENTRY.TABLENAME));
                        userRequestImagesRegistration = firebaseFirestore.collection(VossitContract.USERREQUESTIMAGESENTRY.TABLENAME).whereEqualTo("userId", finalUserId).whereGreaterThan("lastModified",
                                sharedPreferences.getLong(LAST_MODIFIED_USERREQUESTIMAGES, 0)).addSnapshotListener(getEventListener(VossitContract.USERREQUESTIMAGESENTRY.TABLENAME));
                    }
                });
            }
        });

        return Result.success();
    }

    private void finishedWork(){
        if (finishedCategories && finishedCategoryImages && finishedItems && finishedItemImages &&
                finishedItemLinks && finishedUsers && finishedUserRequests && finishedUserRequestImages){
            finishedSync=true;
        }
    }

    public EventListener<QuerySnapshot> getEventListener(String table) {
        EventListener<QuerySnapshot> eventListener = null;
        switch (table) {
            case VossitContract.CATEGORIESENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        saveCategory(queryDocumentSnapshots);
                    }
                };
                break;
            case VossitContract.CATEGORYIMAGESENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        saveCategoryImages(queryDocumentSnapshots);
                    }
                };
                break;
            case VossitContract.ITEMSENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        saveItems(queryDocumentSnapshots);
                    }
                };
                break;
            case VossitContract.ITEMIMAGESENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        saveItemImages(queryDocumentSnapshots);
                    }
                };
                break;
            case VossitContract.ITEMLINKSENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        saveItemLinks(queryDocumentSnapshots);
                    }
                };
                break;
            case VossitContract.USERSENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        saveUsers(queryDocumentSnapshots);
                    }
                };
                break;
            case VossitContract.USERREQUESTSENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        saveUserRequests(queryDocumentSnapshots);
                    }
                };
                break;
            case VossitContract.USERREQUESTIMAGESENTRY.TABLENAME:
                eventListener = new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        saveUserRequestImages(queryDocumentSnapshots);
                    }
                };
                break;

        }
        return eventListener;
    }

    public boolean deleteEntryIfExists(DocumentChange dc, Uri itemUri, Cursor itemCursor) {
        if (dc.getDocument().contains("del")) {
            if (itemCursor != null && itemCursor.moveToFirst()) {
                context.getContentResolver().delete(itemUri, null, null);
            }
            return true;
        }
        return false;
    }

    public void saveCategory(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        countDownLatchCategories=new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    long id = dc.getDocument().getLong("id");
                    Uri categories = Uri.withAppendedPath(BASEURI, VossitContract.CATEGORIESENTRY.TABLENAME);
                    Uri categoryUri = ContentUris.withAppendedId(categories, id);

                    Cursor categoryCursor = context.getContentResolver().query(categoryUri, null,
                            null, null, null);
                    if (categoryCursor!=null) {
                        if (deleteEntryIfExists(dc, categoryUri, categoryCursor))
                            continue;

                        Category category = dc.getDocument().toObject(Category.class);
                        ContentValues categoryContentValues = category.getContentValues(true);
                        saveToDb(dc, categories, categoryUri, categoryCursor, categoryContentValues, category.getLastModified());
                    }
                }

                SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_CATEGORIES,
                        getSystemTime(databaseHelper.getReadableDatabase()));
                editor.apply();
                finishedCategories=true;
                finishedWork();
                countDownLatchCategories.countDown();
            }
        }).start();
    }

    private void saveToDb(DocumentChange dc, Uri categories, Uri categoryUri, Cursor cursor, ContentValues contentValues, Long lastModified) {
        switch (dc.getType()) {
            case ADDED:
                //If the item is already in the db, save the latest copy, if it does not exist, create it
                if (cursor.moveToFirst()) {
                    long localCopyTime = getDateInUtcFromTimeStamp(cursor.getString(cursor.getColumnIndex(LASTMODIFIEDCOLUMN))).getTime();
                    if (localCopyTime != lastModified) {
                        context.getContentResolver().update(categoryUri, contentValues, null, null);
                    }
                } else {
                    context.getContentResolver().insert(categories, contentValues);
                }
                break;
            case MODIFIED:
                if (cursor != null && cursor.moveToFirst()) {
                    long localCopyTime = getDateInUtcFromTimeStamp(cursor.getString(cursor.getColumnIndex(LASTMODIFIEDCOLUMN))).getTime();
                    if (localCopyTime != lastModified) {
                        context.getContentResolver().update(categoryUri, contentValues, null, null);
                    }
                } else {
                    context.getContentResolver().insert(categories, contentValues);
                }
                break;
            case REMOVED:
                if (cursor != null && cursor.moveToFirst()) {
                    context.getContentResolver().delete(categoryUri, null, null);
                }
                break;
        }
    }

    public void saveCategoryImages(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait for categories to finish updating
                try {
                    countDownLatchCategories.await();
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        long id = dc.getDocument().getLong("id");
                        Uri categoryImages = Uri.withAppendedPath(BASEURI, VossitContract.CATEGORYIMAGESENTRY.TABLENAME);
                        Uri categoryImageUri = ContentUris.withAppendedId(categoryImages, id);

                        Cursor categoryImagesCursor = context.getContentResolver().query(categoryImageUri, null,
                                null, null, null);
                        if (categoryImagesCursor!=null) {
                            if (deleteEntryIfExists(dc, categoryImageUri, categoryImagesCursor))
                                continue;

                            Image categoryImage = dc.getDocument().toObject(Image.class);
                            ContentValues categoryImageContentValues = categoryImage.getContentValues(
                                    true, VossitContract.CATEGORYIMAGESENTRY.CATEGORYIDIDCOLUMN, VossitContract.CATEGORYIMAGESENTRY.CATEGORYIMAGECOLUMN,
                                    VossitContract.CATEGORYIMAGESENTRY.LOCALPATHCOLUMN);
                            saveToDb(dc, categoryImages, categoryImageUri, categoryImagesCursor, categoryImageContentValues, categoryImage.getLastModified());
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_CATEGORYIMAGES, getSystemTime(databaseHelper.getReadableDatabase()));
                    editor.apply();
                    finishedCategoryImages=true;
                    finishedFirstCategories=true;
                    finishedWork();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    saveCategoryImages(queryDocumentSnapshots);
                }
            }
        }).start();
    }

    public void saveItems(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        countDownLatchItems=new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait for categories to finish updating
                try {
                    countDownLatchCategories.await();
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        long id = dc.getDocument().getLong("id");
                        Uri items = Uri.withAppendedPath(BASEURI, VossitContract.ITEMSENTRY.TABLENAME);
                        Uri itemUri = ContentUris.withAppendedId(items, id);

                        Cursor itemsCursor = context.getContentResolver().query(itemUri, null,
                                null, null, null);
                        if (itemsCursor!=null) {
                            if (deleteEntryIfExists(dc, itemUri, itemsCursor))
                                continue;

                            Item item = dc.getDocument().toObject(Item.class);
                            ContentValues itemContentValues = item.getContentValues(true);
                            saveToDb(dc, items, itemUri, itemsCursor, itemContentValues, item.getLastModified());
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_ITEMS, getSystemTime(databaseHelper.getReadableDatabase()));
                    editor.apply();
                    finishedItems=true;
                    finishedWork();
                    countDownLatchItems.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    saveItems(queryDocumentSnapshots);
                }
            }
        }).start();
    }

    public void saveItemImages(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait for categories to finish updating
                try {
                    countDownLatchItems.await();
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        long id = dc.getDocument().getLong("id");
                        Uri itemImages = Uri.withAppendedPath(BASEURI, VossitContract.ITEMIMAGESENTRY.TABLENAME);
                        Uri itemImageUri = ContentUris.withAppendedId(itemImages, id);

                        Cursor itemImagesCursor = context.getContentResolver().query(itemImageUri, null,
                                null, null, null);
                        if (itemImagesCursor!=null) {
                            if (deleteEntryIfExists(dc, itemImageUri, itemImagesCursor))
                                continue;

                            Image itemImage = dc.getDocument().toObject(Image.class);
                            ContentValues itemImageContentValues = itemImage.getContentValues(true, VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN,
                                    VossitContract.ITEMIMAGESENTRY.ITEMIMAGECOLUMN, VossitContract.ITEMIMAGESENTRY.LOCALPATHCOLUMN, VossitContract.ITEMIMAGESENTRY.MAINIMAGECOLUMN);
                            saveToDb(dc, itemImages, itemImageUri, itemImagesCursor, itemImageContentValues, itemImage.getLastModified());
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_ITEMIMAGES, getSystemTime(databaseHelper.getReadableDatabase()));
                    editor.apply();
                    finishedItemImages=true;
                    finishedWork();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    saveItemImages(queryDocumentSnapshots);
                }
            }
        }).start();
    }

    public void saveItemLinks(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait for categories to finish updating
                try {
                    countDownLatchItems.await();
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        long id = dc.getDocument().getLong("id");
                        Uri itemLinks = Uri.withAppendedPath(BASEURI, VossitContract.ITEMLINKSENTRY.TABLENAME);
                        Uri itemLinksUri = ContentUris.withAppendedId(itemLinks, id);

                        Cursor itemLinksCursor = context.getContentResolver().query(itemLinksUri, null,
                                null, null, null);
                        if (itemLinksCursor!=null) {
                            if (deleteEntryIfExists(dc, itemLinksUri, itemLinksCursor))
                                continue;

                            Link itemLink = dc.getDocument().toObject(Link.class);
                            ContentValues linkContentValues = itemLink.getContentValues(true);
                            saveToDb(dc, itemLinks, itemLinksUri, itemLinksCursor, linkContentValues, itemLink.getLastModified());
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_ITEMLINKS, getSystemTime(databaseHelper.getReadableDatabase()));
                    editor.apply();
                    finishedItemLinks=true;
                    finishedWork();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    saveItemLinks(queryDocumentSnapshots);
                }
            }
        }).start();
    }

    public void saveUsers(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        countDownLatchUsers=new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    long id = dc.getDocument().getLong("id");
                    Uri users = Uri.withAppendedPath(BASEURI, VossitContract.USERSENTRY.TABLENAME);
                    Uri userUri = ContentUris.withAppendedId(users, id);

                    Cursor userCursor = context.getContentResolver().query(userUri, null,
                            null, null, null);
                    if (userCursor!=null) {
                        if (deleteEntryIfExists(dc, userUri, userCursor))
                            continue;

                        User user = dc.getDocument().toObject(User.class);
                        ContentValues userContentValues = user.getContentValues(true);
                        saveToDb(dc, users, userUri, userCursor, userContentValues, user.getLastModified());
                    }
                }

                if (queryDocumentSnapshots.size()>0) {
                    SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_USERS,
                            getSystemTime(databaseHelper.getReadableDatabase()));
                    editor.apply();
                }
                finishedUsers=true;
                finishedWork();
                countDownLatchUsers.countDown();
            }
        }).start();
    }

    public void saveUserRequests(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        countDownLatchUserRequests=new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait for categories to finish updating
                try {
                    countDownLatchUsers.await();
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        long id = dc.getDocument().getLong("id");
                        Uri userRequests = Uri.withAppendedPath(BASEURI, VossitContract.USERREQUESTSENTRY.TABLENAME);
                        Uri userRequestUri = ContentUris.withAppendedId(userRequests, id);

                        Cursor itemsCursor = context.getContentResolver().query(userRequestUri, null,
                                null, null, null);
                        if (itemsCursor!=null) {
                            if (deleteEntryIfExists(dc, userRequestUri, itemsCursor))
                                continue;

                            UserRequest userRequest = dc.getDocument().toObject(UserRequest.class);
                            ContentValues itemContentValues = userRequest.getContentValues(true);
                            saveToDb(dc, userRequests, userRequestUri, itemsCursor, itemContentValues, userRequest.getLastModified());
                        }
                    }

                    if (queryDocumentSnapshots.size()>0) {
                        SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_USERREQUESTS, getSystemTime(databaseHelper.getReadableDatabase()));
                        editor.apply();
                    }
                    finishedUserRequests=true;
                    finishedWork();
                    countDownLatchUserRequests.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    saveUserRequests(queryDocumentSnapshots);
                }
            }
        }).start();
    }

    public void saveUserRequestImages(@javax.annotation.Nullable final QuerySnapshot queryDocumentSnapshots) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait for categories to finish updating
                try {
                    countDownLatchUserRequests.await();
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        long id = dc.getDocument().getLong("id");
                        Uri userRequestImages = Uri.withAppendedPath(BASEURI, VossitContract.USERREQUESTIMAGESENTRY.TABLENAME);
                        Uri userRequestUri = ContentUris.withAppendedId(userRequestImages, id);

                        Cursor userRequestImagesCursor = context.getContentResolver().query(userRequestUri, null,
                                null, null, null);
                        if (userRequestImagesCursor!=null) {
                            if (deleteEntryIfExists(dc, userRequestUri, userRequestImagesCursor))
                                continue;

                            Image userRequestImage = dc.getDocument().toObject(Image.class);
                            ContentValues userRequestImageContentValues = userRequestImage.getContentValues(
                                    true, VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIDCOLUMN, VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIMAGECOLUMN,
                                    VossitContract.USERREQUESTIMAGESENTRY.LOCALPATHCOLUMN, VossitContract.USERREQUESTIMAGESENTRY.MAINIMAGECOLUMN);
                            saveToDb(dc, userRequestImages, userRequestUri, userRequestImagesCursor, userRequestImageContentValues, userRequestImage.getLastModified());
                        }
                    }

                    if (queryDocumentSnapshots.size()>0) {
                        SharedPreferences.Editor editor = sharedPreferences.edit().putLong(LAST_MODIFIED_USERREQUESTIMAGES, getSystemTime(databaseHelper.getReadableDatabase()));
                        editor.apply();
                    }
                    finishedUserRequestImages=true;
                    finishedWork();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    saveUserRequestImages(queryDocumentSnapshots);
                }
            }
        }).start();
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

}
