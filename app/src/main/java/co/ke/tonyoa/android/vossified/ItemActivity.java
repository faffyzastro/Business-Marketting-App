package co.ke.tonyoa.android.vossified;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ke.tonyoa.android.vossified.Data.VossitContract;
import co.ke.tonyoa.android.vossified.POJOs.Image;
import co.ke.tonyoa.android.vossified.POJOs.Link;


public class ItemActivity extends AppCompatActivity {

    @BindView(R.id.textView_item_name)
    TextView textViewName;
    @BindView(R.id.textView_item_description)
    TextView textViewDescription;
    @BindView(R.id.textView_item_cost)
    TextView textViewCost;
    @BindView(R.id.recyclerView_item_links)
    RecyclerView recyclerViewLinks;
    @BindView(R.id.recyclerView_item_images)
    RecyclerView recyclerViewImages;

    private Uri uri;

    private LinkAdapter linkAdapter;
    private PhotoRecyclerAdapter imageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.bind(this);
        uri = getIntent().getData();
        linkAdapter = new LinkAdapter(this, new ArrayList<>());
        imageAdapter = new PhotoRecyclerAdapter(this, this.getClass().getName(), new ArrayList<>(), null, recyclerViewImages);
        linkAdapter.setEditMode(false);
        imageAdapter.setEditMode(false);
        if (uri != null) {
            new LoadItem(savedInstanceState).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        setTitle(R.string.app_name);

        recyclerViewLinks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLinks.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerViewLinks.setAdapter(linkAdapter);

        SnapHelper snapHelper= new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewImages);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerViewImages.setAdapter(imageAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadItem(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            String itemName = cursor.getString(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMNAMECOLUMN));
            setTitle(itemName);
            textViewName.setText(itemName);
            textViewDescription.setText(cursor.getString(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMDESCRIPTIONCOLUMN)));
            textViewCost.setText(getString(R.string.cost, cursor.getFloat(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN))));
            cursor.close();
        }
    }

    private void loadLinks(Cursor cursor) {
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Link link = new Link(cursor);
                linkAdapter.addLink(link);
            }
            cursor.close();
        }
    }

    private void loadImages(Cursor cursor) {
        imageAdapter.getImages().clear();
        if (cursor != null) {
            while (cursor.moveToNext()){
                Image image=new Image(cursor, Image.ITEMIMAGE);
                imageAdapter.addImage(image);
            }
            cursor.close();
        }
    }

    class LoadItem extends AsyncTask<Void, Void, Map<Integer, Cursor>> {

        private final int itemId = 0;
        private final int imageId = 1;
        private final int linkId = 2;
        private Bundle bundle;

        public LoadItem(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        protected Map<Integer, Cursor> doInBackground(Void... voids) {
            Map<Integer, Cursor> cursors = new HashMap<>();
            Cursor cursorItem = getContentResolver().query(Uri.withAppendedPath(VossitContract.BASEURI,
                    VossitContract.getItemToCategoriesToCategoryImages()), new String[]{VossitContract.ITEMSENTRY.TABLENAME+
                            "."+ VossitContract.ITEMSENTRY._ID, VossitContract.ITEMSENTRY.ITEMNAMECOLUMN,
                            VossitContract.ITEMSENTRY.ITEMDESCRIPTIONCOLUMN, VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN,
                            VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN}, VossitContract.ITEMSENTRY.TABLENAME+
                            "."+ VossitContract.ITEMSENTRY._ID+"=?",new String[]{ContentUris.parseId(uri)+""}, null);
            cursors.put(itemId, cursorItem);
            Cursor cursorImage = getContentResolver().query(Uri.withAppendedPath(VossitContract.BASEURI,
                    VossitContract.ITEMIMAGESENTRY.TABLENAME), null,
                    VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN + "=?",
                    new String[]{ContentUris.parseId(uri) + ""}, null);
            cursors.put(imageId, cursorImage);
            Cursor cursorLink = getContentResolver().query(Uri.withAppendedPath(VossitContract.BASEURI,
                    VossitContract.ITEMLINKSENTRY.TABLENAME), null,
                    VossitContract.ITEMLINKSENTRY.ITEMIDCOLUMN + "=?",
                    new String[]{ContentUris.parseId(uri) + ""}, null);
            cursors.put(linkId, cursorLink);
            return cursors;
        }

        @Override
        protected void onPostExecute(Map<Integer, Cursor> cursors) {
            super.onPostExecute(cursors);
            loadItem(cursors.get(itemId));
            loadImages(cursors.get(imageId));
            loadLinks(cursors.get(linkId));
        }
    }



}
