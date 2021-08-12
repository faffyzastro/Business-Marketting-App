package co.ke.tonyoa.android.vossified;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Utils {

    public static final String QUERY = "query";
    public static final String RECYCLERPOSITION = "RECYCLERPOSITION";
    public static final String SELECTEDIDS = "SELECTEDIDS";

    public static final int REQUESTIMAGE = 2;

    public static void setActivityTitle(Activity context, String item, Uri uri) {
        if (uri != null) {
            context.setTitle(item + " No." + ContentUris.parseId(uri));
        }
        else {
            context.setTitle("New " + item);
        }
    }

    public static void buttonEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

    public static void enableViews(List<View> views, boolean enabled) {
        for (View view : views) {
            view.setEnabled(enabled);
        }
    }

    public static boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return false;
        }
    }

    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public static boolean isNameValid(String name) {
        return name != null && name.trim().length() > 2;
    }

    public static void setInputType(List<? extends TextView> textViews, int inputType) {
        for (TextView textView : textViews) {
            textView.setInputType(inputType);
        }
    }

    public static void enableFocusable(List<? extends View> views, boolean isLongClickable) {
        for (View view : views) {
            view.setFocusable(isLongClickable);
            view.setFocusableInTouchMode(isLongClickable);
            view.setLongClickable(isLongClickable);
        }
    }

    public static AlertDialog.Builder promptDeleteBuilder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder;
    }

    public static AlertDialog.Builder promptQuitBuilder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.discard_changes));
        builder.setCancelable(false);
        return builder;
    }

    public static void setEmptyList(RecyclerView recyclerView, TextView emptyView) {
        if (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() <= 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    public static Date getDateFromString(String datetoSaved) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return format.parse(datetoSaved);
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static String getStringFromDate(long date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(date);
    }

    public static String getStringFromTimeStamp(long date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static long getSystemTime(SQLiteDatabase database) {
        long systemTime;
        Cursor cursorTime = database.rawQuery("SELECT CURRENT_TIMESTAMP AS time", null);
        cursorTime.moveToFirst();
        systemTime = getDateInUtcFromTimeStamp(cursorTime.getString(cursorTime.getColumnIndex("time"))).getTime();
        cursorTime.close();
        return systemTime;
    }

    //Get the date in UTC
    public static Date getDateInUtcFromTimeStamp(String dateSaved) {
        if (dateSaved != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = sdf.parse(dateSaved);
                return date;
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getFileNameNoExtension(File file) {
        return getFileNameNoExtension(file == null ? "" : file.getName());
    }

    public static String getFileNameNoExtension(String file) {
        if (file != null && !file.trim().isEmpty()) {
            return file.substring(0, file.lastIndexOf("."));
        }
        return "";
    }

    public static boolean hasNetwork(ConnectivityManager connectivityManager) {
        boolean hasWifi = false;
        boolean hasMobileData = false;
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info : networkInfos) {
            if (info.getTypeName().equalsIgnoreCase("Wifi")) {
                if (info.isConnected()) {
                    hasWifi = true;
                }
            }
            if (info.getTypeName().equalsIgnoreCase("Mobile")) {
                if (info.isConnected()) {
                    hasMobileData = true;
                }
            }
        }
        return hasWifi || hasMobileData;
    }

    public static void showProgressBar(ProgressBar progressBar, RecyclerView recyclerView, boolean show){
        if (show){
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void addChips(Context context, String[] text, ChipGroup chipGroup){
        for (int x=0; x<text.length; x++){
            Chip chip=new Chip(context);
            chip.setText(text[x]);
            chipGroup.addView(chip);
        }
    }

    public static StringBuilder appendAnd(StringBuilder selectionBuilder, List<String> selectionArgs) {
        if (selectionArgs.size() > 0)
            return selectionBuilder.append(" AND ");
        else
            return selectionBuilder;
    }

    public static void performCrop(Uri selectedImage, Activity activity) {
        // start cropping activity for pre-acquired image saved on the device
        CropImage.activity(selectedImage).setGuidelines(CropImageView.Guidelines.ON).
                setRequestedSize(720, 960, CropImageView.RequestSizeOptions.RESIZE_INSIDE).
                setFixAspectRatio(true).setBackgroundColor(R.color.colorPrimaryFaded).start(activity);
    }

    static class MyTextWatcher implements TextWatcher {

        private Activity context;
        private List<TextView> textViews;
        private boolean modified;

        public MyTextWatcher(Activity context, List<? extends TextView> textViews) {
            this.context = context;
            this.textViews = new ArrayList<>(textViews);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (textViews.contains(context.getCurrentFocus()))
                modified = true;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        public boolean isModified() {
            return modified;
        }

        public void setModified(boolean modified) {
            this.modified = modified;
        }

        public void addTextView(TextView textView) {
            textViews.add(textView);
        }

        public void removeTextView(TextView textView) {
            textViews.remove(textView);
        }

        public List<TextView> getTextViews() {
            return textViews;
        }
    }

}
