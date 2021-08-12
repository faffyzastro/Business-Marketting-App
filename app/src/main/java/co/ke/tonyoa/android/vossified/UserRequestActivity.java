package co.ke.tonyoa.android.vossified;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ke.tonyoa.android.vossified.Data.VossitContract;
import co.ke.tonyoa.android.vossified.POJOs.Image;

import static co.ke.tonyoa.android.vossified.Utils.performCrop;
import static co.ke.tonyoa.android.vossified.Utils.setActivityTitle;

public class UserRequestActivity extends AppCompatActivity {

    private static final String ITEMNAME = "ITEMNAME";
    private static final String ITEMDESCRIPTION = "ITEMDESCRIPTION";
    private static final String EDITABLE = "EDITABLE";
    private static final String MODIFIED = "MODIFIED";

    @BindView(R.id.textInputEditText_userRequest_itemName)
    TextInputEditText textInputEditTextName;
    @BindView(R.id.textInputEditText_userRequest_itemDescription)
    TextInputEditText textInputEditTextDescription;
    @BindView(R.id.recyclerView_userRequest)
    RecyclerView recyclerView;
    @BindView(R.id.button_userRequest_addImage)
    Button buttonAddImage;
    @BindView(R.id.imageView_userRequest_status)
    ImageView imageViewStatus;

    private Uri uri;
    private Utils.MyTextWatcher allChangeTextWatcher;

    private PhotoRecyclerAdapter imageAdapter;

    private ConnectivityManager connectivityManager;

    private boolean showMenu=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);
        ButterKnife.bind(this);
        uri = getIntent().getData();
        allChangeTextWatcher = new Utils.MyTextWatcher(this, Arrays.asList(textInputEditTextName,
                textInputEditTextDescription));
        imageAdapter = new PhotoRecyclerAdapter(this, this.getClass().getName(), new ArrayList<>(), allChangeTextWatcher, recyclerView);

        if (uri != null) {
            new LoadUserRequest(savedInstanceState).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            imageAdapter.setEditMode(true);
        }
        setActivityTitle(this, getString(R.string.item_request), uri);
        for (TextView textView : allChangeTextWatcher.getTextViews()) {
            textView.addTextChangedListener(allChangeTextWatcher);
        }

        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
            }
        });
        SnapHelper snapHelper= new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(imageAdapter);
        loadState(savedInstanceState);
        connectivityManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
    }

    private void addImage() {
        if (isEditMode()) {
            selectImageFile();
        }
    }

    private void selectImageFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, Utils.REQUESTIMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Utils.REQUESTIMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImage = data.getData();
                performCrop(selectedImage, this);
            }
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            String localPath=result.getUri().toString();
            Image image=new Image(-1, "", -1, imageAdapter.getItemCount()==0, -1, localPath);
            imageAdapter.addImage(image);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void viewMode() {
        invalidateOptionsMenu();
        textInputEditTextName.setInputType(InputType.TYPE_NULL);
        textInputEditTextDescription.setInputType(InputType.TYPE_NULL);
        buttonAddImage.setVisibility(View.GONE);
        if (imageAdapter.getItemCount() < 1) {
            recyclerView.setVisibility(View.GONE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
        }
        imageAdapter.setEditMode(false);
        allChangeTextWatcher.setModified(false);
    }

    private void editMode() {
        invalidateOptionsMenu();
        textInputEditTextName.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        textInputEditTextDescription.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        buttonAddImage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        imageAdapter.setEditMode(true);
    }

    public static void setStatusImage(Context context, ImageView imageView, int accepted){
        int resource;
        int tint;
        if (accepted<0){
            resource=R.drawable.ic_thumb_down;
            tint=android.R.color.holo_red_dark;
        }
        else if (accepted==0){
            resource=R.drawable.ic_timelapse;
            tint=android.R.color.holo_blue_light;
        }
        else {
            resource=R.drawable.ic_thumb_up;
            tint=android.R.color.holo_green_dark;
        }
        imageView.setColorFilter(ContextCompat.getColor(context, tint), android.graphics.PorterDuff.Mode.SRC_IN);
        imageView.setImageResource(resource);
    }

    private void save() {
        if (!Utils.hasNetwork(connectivityManager)){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            return;
        }
        if (allChangeTextWatcher.isModified()) {
            String itemName = textInputEditTextName.getText().toString().trim();
            String itemDescription = textInputEditTextDescription.getText().toString().trim();
            if (TextUtils.isEmpty(itemName)) {
                textInputEditTextName.setError(getString(R.string.error_empty));
                textInputEditTextName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(itemDescription)) {
                textInputEditTextDescription.setError(getString(R.string.error_empty));
                textInputEditTextDescription.requestFocus();
                return;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN, itemName);
            contentValues.put(VossitContract.USERREQUESTSENTRY.ITEMDESCRIPTIONCOLUMN, itemDescription);
            contentValues.put(VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN, 0);
            contentValues.put(VossitContract.USERREQUESTSENTRY.USERIDCOLUMN, FirebaseAuth.getInstance().getUid());

            if (this.uri == null) {
                Uri uri = getContentResolver().insert(Uri.withAppendedPath(VossitContract.BASEURI,
                        VossitContract.USERREQUESTSENTRY.TABLENAME), contentValues);
                if (ContentUris.parseId(uri) == -1) {
                    Toast.makeText(this, getString(R.string.error_add_db), Toast.LENGTH_SHORT).show();
                } else if (ContentUris.parseId(uri) == -2) {
                    Toast.makeText(this, getString(R.string.error_exists_db), Toast.LENGTH_SHORT).show();
                } else {
                    this.uri = uri;
                    saveImages();
                    viewMode();
                    setActivityTitle(this, "Item", uri);
                    Snackbar.make(textInputEditTextDescription, "Successfully added to the database", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                int affected = getContentResolver().update(uri, contentValues, null, null);
                if (affected > 0) {
                    saveImages();
                    viewMode();
                    Toast.makeText(this, " Successfully updated", Toast.LENGTH_SHORT).show();
                } else if (affected == -1) {
                    Toast.makeText(this, getString(R.string.error_exists_db), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, " Update failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (uri != null) {
            viewMode();
        }
        else {
            finish();
        }
    }

    private void saveImages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri imageUri = Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.USERREQUESTIMAGESENTRY.TABLENAME);
                for (Image image : imageAdapter.getImages()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIDCOLUMN, ContentUris.parseId(uri));
                    contentValues.put(VossitContract.USERREQUESTIMAGESENTRY.LOCALPATHCOLUMN, image.getLocalPath());
                    contentValues.put(VossitContract.USERREQUESTIMAGESENTRY.MAINIMAGECOLUMN, image.isMainImage()?1:0);
                    //If the image is new, insert it in the db
                    if (image.getId() == -1) {
                        getContentResolver().insert(imageUri, contentValues);
                        continue;
                    }
                    //If the image has been updated n.b. mainImage is the only update that can occur
                    if (image.isModified()) {
                        getContentResolver().update(ContentUris.withAppendedId(imageUri, image.getId()), contentValues, null, null);
                    }
                }
                //Delete deleted images
                for (Image image : imageAdapter.getDeletedImages()) {
                    getContentResolver().delete(ContentUris.withAppendedId(imageUri, image.getId()), null, null);
                }
            }
        }).start();
    }

    private boolean isEditMode() {
        return textInputEditTextName.getInputType()!=InputType.TYPE_NULL;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!showMenu) {
            menu.clear();
            return true;
        }
        if (isEditMode()) {
            menu.findItem(R.id.action_edit_delete_edit).setTitle(getString(R.string.done));
        } else {
            menu.findItem(R.id.action_edit_delete_edit).setTitle(getString(R.string.edit));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                promptQuit();
                return true;
            case R.id.action_edit_delete_edit:
                if (item.getTitle().equals(getString(R.string.edit))) {
                    editMode();
                } else {
                    save();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        promptQuit();
    }

    private void promptQuit() {
        if (allChangeTextWatcher.isModified()) {
            androidx.appcompat.app.AlertDialog.Builder alertDialog = Utils.promptQuitBuilder(this);
            alertDialog.setPositiveButton(getString(R.string.save_and_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    save();
                    if (!allChangeTextWatcher.isModified()) {
                        finish();
                    }
                }
            });
            alertDialog.setNegativeButton(super.getString(R.string.dont_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UserRequestActivity.super.onBackPressed();
                }
            });
            alertDialog.create().show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isEditMode()) {
            outState.putString(ITEMNAME, textInputEditTextName.getText().toString());
            outState.putString(ITEMDESCRIPTION, textInputEditTextDescription.getText().toString());
            outState.putBoolean(EDITABLE, true);
            outState.putBoolean(MODIFIED, allChangeTextWatcher.isModified());
        } else {
            outState.putBoolean(EDITABLE, false);
        }
    }

    private void loadState(Bundle bundle) {
        if (bundle != null && bundle.getBoolean(EDITABLE)) {
            if (uri != null) {
                editMode();
            }
            textInputEditTextName.setText(bundle.getString(ITEMNAME));
            textInputEditTextDescription.setText(bundle.getString(ITEMDESCRIPTION));
            allChangeTextWatcher.setModified(bundle.getBoolean(MODIFIED));
        }
    }


    private void loadUserRequest(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            textInputEditTextName.setText(cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN)));
            textInputEditTextDescription.setText(cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMDESCRIPTIONCOLUMN)));
            int status = cursor.getInt(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN));
            setStatusImage(this, imageViewStatus, status);
            showMenu= status != 1;
            invalidateOptionsMenu();
            cursor.close();
        }
    }

    private void loadUserRequestImages(Cursor cursor) {
        imageAdapter.getImages().clear();
        if (cursor != null) {
            while (cursor.moveToNext()){
                Image image=new Image(cursor, Image.USERREQUESTIMAGE);
                imageAdapter.addImage(image);
            }
            cursor.close();
        }
    }

    class LoadUserRequest extends AsyncTask<Void, Void, Map<Integer, Cursor>> {

        private final int userRequestId = 0;
        private final int imageId = 1;
        private Bundle bundle;

        public LoadUserRequest(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        protected Map<Integer, Cursor> doInBackground(Void... voids) {
            Map<Integer, Cursor> cursors = new HashMap<>();
            Cursor cursorUserRequest = getContentResolver().query(uri, null, null,
                    null, null);
            cursors.put(userRequestId, cursorUserRequest);
            Cursor cursorImage = getContentResolver().query(Uri.withAppendedPath(VossitContract.BASEURI,
                    VossitContract.USERREQUESTIMAGESENTRY.TABLENAME), null,
                    VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIDCOLUMN + "=?",
                    new String[]{ContentUris.parseId(uri) + ""}, null);
            cursors.put(imageId, cursorImage);
            return cursors;
        }

        @Override
        protected void onPostExecute(Map<Integer, Cursor> cursors) {
            super.onPostExecute(cursors);
            loadUserRequest(cursors.get(userRequestId));
            loadUserRequestImages(cursors.get(imageId));
            if (bundle == null || !bundle.getBoolean(EDITABLE)) {
                viewMode();
            } else {
                loadState(bundle);
            }
        }
    }

}
