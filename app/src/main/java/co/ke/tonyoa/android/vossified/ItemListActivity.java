package co.ke.tonyoa.android.vossified;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.QUERY;
import static co.ke.tonyoa.android.vossified.Utils.RECYCLERPOSITION;
import static co.ke.tonyoa.android.vossified.Utils.appendAnd;

public class ItemListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.fab_rv)
    FloatingActionButton fab;
    @BindView(R.id.rv_layout)
    RecyclerView recyclerView;
    @BindView(R.id.textView_emptyView)
    TextView textViewEmpty;
    @BindView(R.id.progressBar_recycler)
    ProgressBar progressBar;
    private MainRecyclerAdapter mainRecyclerAdapter;

    private String filterName = null;
    private float filterCostGreater = Spinner.INVALID_POSITION;
    private float filterCostLess = Spinner.INVALID_POSITION;
    private String orderDirection = "ASC";
    private String orderTable = VossitContract.ITEMSENTRY.ITEMNAMECOLUMN;

    private long categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recycler);
        ButterKnife.bind(this);
        categoryId=getIntent().getLongExtra(VossitContract.CATEGORIESENTRY._ID, -1);
        String category=getIntent().getStringExtra(VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN);
        setTitle(category);
        mainRecyclerAdapter = new MainRecyclerAdapter(this, null, this.getClass().getName());
        if (savedInstanceState != null) {
            mainRecyclerAdapter.setSearchTag(savedInstanceState.getString(QUERY));
        }
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mainRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (savedInstanceState != null) {
            mainRecyclerAdapter.setLastFirstVisiblePosition(savedInstanceState.getInt(RECYCLERPOSITION));
            mainRecyclerAdapter.setSearchTag(savedInstanceState.getString(QUERY));
        }
        fab.hide();
        LoaderManager.getInstance(this).initLoader(0, null, this).forceLoad();
    }

    @Override
    public void onPause() {
        super.onPause();
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            //Check completely visible child and change it only when it is not at the beginning
            if (firstCompletelyVisibleItemPosition != -1) {
                mainRecyclerAdapter.setLastFirstVisiblePosition(firstCompletelyVisibleItemPosition);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mainRecyclerAdapter.setSearchTag(query);
                if (!query.trim().isEmpty()) {
                    Bundle bundle = new Bundle();
                    bundle.putString(QUERY, query);
                    LoaderManager.getInstance(ItemListActivity.this).restartLoader(0, bundle, ItemListActivity.this).forceLoad();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mainRecyclerAdapter.setSearchTag(newText);
                Bundle bundle = new Bundle();
                bundle.putString(QUERY, newText);
                LoaderManager.getInstance(ItemListActivity.this).restartLoader(0, bundle, ItemListActivity.this).forceLoad();
                return true;
            }
        });

        if (mainRecyclerAdapter.getSearchTag() != null && !mainRecyclerAdapter.getSearchTag().trim().isEmpty()) {
            searchView.post(new Runnable() {
                @Override
                public void run() {
                    searchView.setQuery(mainRecyclerAdapter.getSearchTag(), true);
                }
            });
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RECYCLERPOSITION, mainRecyclerAdapter.getLastFirstVisiblePosition());
        if (mainRecyclerAdapter.getSearchTag() != null && !mainRecyclerAdapter.getSearchTag().trim().isEmpty()) {
            outState.putString(QUERY, mainRecyclerAdapter.getSearchTag());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                filter();
                return true;
            case R.id.action_order:
                order();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filter(){
        View view = LayoutInflater.from(this).inflate(R.layout.layout_filter_item, null, false);
        final CheckBox checkBoxItemName = view.findViewById(R.id.checkbox_filter_item_name);
        final CheckBox checkBoxCost = view.findViewById(R.id.checkbox_filter_item_cost);

        CardView cardViewItemName = view.findViewById(R.id.cardView_filter_item_name);
        CardView cardViewCost = view.findViewById(R.id.cardView_filter_item_cost);

        final TextInputEditText textInputEditTextCostGreater = view.findViewById(R.id.textInputEditText_item_costGreater);
        final TextInputEditText textInputEditTextCostLess = view.findViewById(R.id.textInputEditText_filter_item_costLess);
        final TextInputEditText textInputEditTextName = view.findViewById(R.id.textInputEditText_filter_item_name);

        checkBoxCost.setChecked(filterCostGreater != Spinner.INVALID_POSITION || filterCostLess != Spinner.INVALID_POSITION);
        checkBoxItemName.setChecked(filterName != null);

        if (filterCostGreater != Spinner.INVALID_POSITION) {
            cardViewCost.setVisibility(View.VISIBLE);
            textInputEditTextCostGreater.setText(String.valueOf(filterCostGreater));
        }
        if (filterCostLess != Spinner.INVALID_POSITION) {
            cardViewCost.setVisibility(View.VISIBLE);
            textInputEditTextCostLess.setText(String.valueOf(filterCostLess));
        }
        if (filterName != null) {
            cardViewItemName.setVisibility(View.VISIBLE);
            textInputEditTextName.setText(filterName);
        }

        checkBoxItemName.setOnCheckedChangeListener(new MyCheckBoxListener(cardViewItemName));
        checkBoxCost.setOnCheckedChangeListener(new MyCheckBoxListener(cardViewCost));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filter);
        builder.setView(view);
        builder.setPositiveButton(R.string.filter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (checkBoxItemName.isChecked()) {
                    String itemName = textInputEditTextName.getText().toString().trim();
                    if (!TextUtils.isEmpty(itemName)) {
                        filterName = itemName;
                    }
                    else {
                        filterName = null;
                    }
                }
                else {
                    filterName = null;
                }

                if (checkBoxCost.isChecked()) {
                    String costGreater = textInputEditTextCostGreater.getText().toString().trim();
                    String costLess = textInputEditTextCostLess.getText().toString().trim();
                    if (!TextUtils.isEmpty(costGreater)) {
                        filterCostGreater = Float.valueOf(costGreater);
                    }
                    else {
                        filterCostGreater = Spinner.INVALID_POSITION;
                    }

                    if (!TextUtils.isEmpty(costLess)) {
                        filterCostLess = Float.valueOf(costLess);
                    }
                    else {
                        filterCostLess = Spinner.INVALID_POSITION;
                    }
                }
                else {
                    filterCostGreater = Spinner.INVALID_POSITION;
                    filterCostLess = Spinner.INVALID_POSITION;
                }

                if (filterCostGreater != Spinner.INVALID_POSITION && filterCostLess != Spinner.INVALID_POSITION) {
                    if (filterCostLess < filterCostGreater || filterCostLess == filterCostGreater) {
                        Snackbar.make(recyclerView, "The cost values are constrained wrongly",
                                Snackbar.LENGTH_LONG).setAction(R.string.edit, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                filter();
                            }
                        }).show();
                        return;
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putString(QUERY, mainRecyclerAdapter.getSearchTag());
                LoaderManager.getInstance(ItemListActivity.this).restartLoader(0, bundle, ItemListActivity.this).forceLoad();
                ItemListActivity.this.invalidateOptionsMenu();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();


        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alertDialog.show();
    }

    private void order(){
        View view = LayoutInflater.from(this).inflate(R.layout.layout_order_item, null, false);
        final RadioGroup radioGroupOrder = view.findViewById(R.id.radioGroup_order_direction);
        RadioButton radioButtonAsc = view.findViewById(R.id.radio_order_asc);
        RadioButton radioButtonDesc = view.findViewById(R.id.radio_order_desc);
        final RadioGroup radioGroupItem = view.findViewById(R.id.radioGroup_item);
        RadioButton radioButtonName = view.findViewById(R.id.radio_order_item_name);
        RadioButton radioButtonCost = view.findViewById(R.id.radio_order_item_cost);

        if (orderDirection.equals("ASC")) {
            radioButtonAsc.setChecked(true);
        }
        else {
            radioButtonDesc.setChecked(true);
        }
        switch (orderTable) {
            case VossitContract.ITEMSENTRY.ITEMNAMECOLUMN:
                radioButtonName.setChecked(true);
                break;
            case VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN:
                radioButtonCost.setChecked(true);
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.sort));
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.sort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (radioGroupOrder.getCheckedRadioButtonId() == R.id.radio_order_asc) {
                    orderDirection = "ASC";
                }
                else {
                    orderDirection = "DESC";
                }
                switch (radioGroupItem.getCheckedRadioButtonId()) {
                    case R.id.radio_order_item_name:
                        orderTable = VossitContract.ITEMSENTRY.ITEMNAMECOLUMN;
                        break;
                    case R.id.radio_order_item_cost:
                        orderTable = VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN;
                        break;
                }

                Bundle bundle = new Bundle();
                bundle.putString(QUERY, mainRecyclerAdapter.getSearchTag());
                LoaderManager.getInstance(ItemListActivity.this).restartLoader(0, bundle, ItemListActivity.this).forceLoad();
            }
        });

        builder.create().show();

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == 0) {
            Utils.showProgressBar(progressBar, recyclerView, true);
            ArrayList<String> arraySelectionArgs = new ArrayList<>();
            StringBuilder selectionBuilder = new StringBuilder();
            if (args != null && args.getString(QUERY) != null) {
                String query = args.getString(QUERY);
                String[] words = query.split(" ");
                String baseQuery = " (" + VossitContract.ITEMSENTRY.ITEMNAMECOLUMN + " LIKE ? OR "+
                        VossitContract.ITEMSENTRY.ITEMDESCRIPTIONCOLUMN +" LIKE ?) ";
                selectionBuilder.append(baseQuery);
                for (int x = 1; x < words.length; x++) {
                    selectionBuilder.append(" AND ").append(baseQuery);
                }

                for (String word : words) {
                    for (int x = 0; x < 2; x++) {
                        arraySelectionArgs.add("%" + word + "%");
                    }
                }
            }

            if (filterName != null) {
                appendAnd(selectionBuilder, arraySelectionArgs).append(VossitContract.ITEMSENTRY.ITEMNAMECOLUMN + " LIKE ?");
                arraySelectionArgs.add("%" + filterName + "%");
            }

            if (filterCostLess != Spinner.INVALID_POSITION) {
                appendAnd(selectionBuilder, arraySelectionArgs).append(VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN + "<?");
                arraySelectionArgs.add(filterCostLess + "");
            }

            if (filterCostGreater != Spinner.INVALID_POSITION) {
                appendAnd(selectionBuilder, arraySelectionArgs).append(VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN + ">?");
                arraySelectionArgs.add(filterCostGreater + "");
            }
            appendAnd(selectionBuilder, arraySelectionArgs).append(VossitContract.ITEMSENTRY.TABLENAME).append(".").append(VossitContract.ITEMSENTRY.CATEGORYIDCOLUMN + "=?");
            arraySelectionArgs.add(categoryId + "");

            return new CursorLoader(this, Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.getItemToCategoriesToCategoryImages()),
                    new String[]{VossitContract.ITEMSENTRY.TABLENAME+"."+ VossitContract.ITEMSENTRY._ID,
                            VossitContract.ITEMSENTRY.ITEMNAMECOLUMN, VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN,
                            VossitContract.ITEMSENTRY.TABLENAME+"."+VossitContract.ITEMSENTRY.CATEGORYIDCOLUMN,
                            VossitContract.CATEGORYIMAGESENTRY.CATEGORYIMAGECOLUMN},
                    arraySelectionArgs.size() < 1 ? null : selectionBuilder.toString(),
                    arraySelectionArgs.size() < 1 ? null : arraySelectionArgs.toArray(new String[0]),
                    orderTable +" "+orderDirection);
        }
        else if (id==1){
            ArrayList<String> arraySelectionArgs = new ArrayList<>();
            StringBuilder selectionBuilder = new StringBuilder();
            String baseQuery = " (" + VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN + " = ? )";
            selectionBuilder.append(baseQuery);
            for (int x = 1; x < mainRecyclerAdapter.getItemCount(); x++) {
                selectionBuilder.append(" OR ").append(baseQuery);
            }

            Cursor cursorItems=mainRecyclerAdapter.getCursor();
            cursorItems.moveToPosition(-1);
            while (cursorItems.moveToNext()){
                arraySelectionArgs.add(cursorItems.getLong(cursorItems.getColumnIndex(VossitContract.ITEMSENTRY._ID)) + "");
            }
            selectionBuilder.append(" AND ").append(VossitContract.ITEMIMAGESENTRY.MAINIMAGECOLUMN).append("=?");
            arraySelectionArgs.add("1");
            return new CursorLoader(this, Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.ITEMIMAGESENTRY.TABLENAME),
                    new String[]{VossitContract.ITEMIMAGESENTRY._ID, VossitContract.ITEMIMAGESENTRY.ITEMIMAGECOLUMN, VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN},
                    arraySelectionArgs.size() < 1 ? null : selectionBuilder.toString(),
                    arraySelectionArgs.size() < 1 ? null : arraySelectionArgs.toArray(new String[0]),
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 0) {
            Utils.showProgressBar(progressBar, recyclerView, false);
            if (data != null) {
                mainRecyclerAdapter.setOriginalCursor(data);
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(mainRecyclerAdapter.getLastFirstVisiblePosition());
                    }
                });
            }
            if (mainRecyclerAdapter.getSearchTag()==null || mainRecyclerAdapter.getSearchTag().trim().isEmpty()){
                textViewEmpty.setText(getString(R.string.no_items, "items"));
            }
            else {
                textViewEmpty.setText(getString(R.string.no_results, mainRecyclerAdapter.getSearchTag()));
            }
            Utils.setEmptyList(recyclerView, textViewEmpty);
            LoaderManager.getInstance(this).restartLoader(1, null, this).forceLoad();
        }
        else if (loader.getId()==1){
            if (data!=null){
                LongSparseArray<String> images=new LongSparseArray<>();
                while (data.moveToNext()){
                    images.put(data.getLong(data.getColumnIndex(VossitContract.ITEMIMAGESENTRY.ITEMIDCOLUMN)),
                            data.getString(data.getColumnIndex(VossitContract.ITEMIMAGESENTRY.ITEMIMAGECOLUMN)));
                }
                mainRecyclerAdapter.setImages(images);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == 0) {
            mainRecyclerAdapter.setOriginalCursor(null);
        }
        else if (loader.getId()==1){
            mainRecyclerAdapter.setImages(null);
        }
    }

    static class MyCheckBoxListener implements CompoundButton.OnCheckedChangeListener {

        private View controlledView;

        public MyCheckBoxListener(View controlledView) {
            this.controlledView = controlledView;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                controlledView.setVisibility(View.VISIBLE);
            else
                controlledView.setVisibility(View.GONE);
        }
    }

}
