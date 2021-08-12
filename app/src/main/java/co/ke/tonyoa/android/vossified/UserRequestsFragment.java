package co.ke.tonyoa.android.vossified;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.QUERY;
import static co.ke.tonyoa.android.vossified.Utils.RECYCLERPOSITION;
import static co.ke.tonyoa.android.vossified.Utils.appendAnd;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserRequestsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnUserLog  {


    @BindView(R.id.fab_rv)
    FloatingActionButton fab;
    @BindView(R.id.rv_layout)
    RecyclerView recyclerView;
    @BindView(R.id.textView_emptyView)
    TextView textViewEmpty;
    @BindView(R.id.progressBar_recycler)
    ProgressBar progressBar;
    private Unbinder unbinder;
    private MainRecyclerAdapter mainRecyclerAdapter;

    private String filterItemName = null;
    private int filterAddedState=Spinner.INVALID_POSITION;

    private String orderDirection = "DESC";
    private String orderTable = VossitContract.USERREQUESTSENTRY.TABLENAME+"."+VossitContract.LASTMODIFIEDCOLUMN;

    private String userId;

    public UserRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_recycler, container, false);
        unbinder = ButterKnife.bind(this, view);
        mainRecyclerAdapter = new MainRecyclerAdapter(getActivity(), null, this.getClass().getName());
        if (savedInstanceState != null) {
            mainRecyclerAdapter.setSearchTag(savedInstanceState.getString(QUERY));
        }
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mainRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userId=FirebaseAuth.getInstance().getUid();
        if(userId==null)
            userId="()";
        if (savedInstanceState != null) {
            mainRecyclerAdapter.setLastFirstVisiblePosition(savedInstanceState.getInt(RECYCLERPOSITION));
            mainRecyclerAdapter.setSearchTag(savedInstanceState.getString(QUERY));
        }
        LoaderManager.getInstance(this).initLoader(0, null, this).forceLoad();
        return view;
    }

    @OnClick(R.id.fab_rv)
    public void newRequest() {
        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            Snackbar.make(fab, "Log in to request for item verifications", BaseTransientBottomBar.LENGTH_INDEFINITE).
                    setAction("Log in", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(getContext(), LoginActivity.class);
                            getContext().startActivity(intent);
                        }
                    }).show();
            return;
        }
        Intent intent = new Intent(getContext(), UserRequestActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mainRecyclerAdapter.setSearchTag(query);
                if (!query.trim().isEmpty()) {
                    Bundle bundle = new Bundle();
                    bundle.putString(QUERY, query);
                    LoaderManager.getInstance(UserRequestsFragment.this).restartLoader(0, bundle, UserRequestsFragment.this).forceLoad();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mainRecyclerAdapter.setSearchTag(newText);
                Bundle bundle = new Bundle();
                bundle.putString(QUERY, newText);
                LoaderManager.getInstance(UserRequestsFragment.this).restartLoader(0, bundle, UserRequestsFragment.this).forceLoad();
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_filter_user_request, null, false);
        final CheckBox checkBoxItemName = view.findViewById(R.id.checkbox_filter_user_request_itemName);
        final CheckBox checkBoxAddedState = view.findViewById(R.id.checkbox_filter_user_request_added);

        CardView cardViewItemName = view.findViewById(R.id.cardView_filter_user_request_itemName);
        CardView cardViewAddedState = view.findViewById(R.id.cardView_filter_user_request_added);

        final Spinner spinnerAddedState = view.findViewById(R.id.spinner_filter_user_request_added);
        final TextInputEditText textInputEditTextItemName = view.findViewById(R.id.textInputEditText_filter_user_request_itemName);

        checkBoxItemName.setChecked(filterItemName != null);
        checkBoxAddedState.setChecked(filterAddedState != Spinner.INVALID_POSITION);

        if (filterItemName != null) {
            cardViewItemName.setVisibility(View.VISIBLE);
            textInputEditTextItemName.setText(filterItemName);
        }
        if (filterAddedState != Spinner.INVALID_POSITION) {
            cardViewAddedState.setVisibility(View.VISIBLE);
            spinnerAddedState.setSelection(filterAddedState);
        }

        checkBoxItemName.setOnCheckedChangeListener(new ItemListActivity.MyCheckBoxListener(cardViewItemName));
        checkBoxAddedState.setOnCheckedChangeListener(new ItemListActivity.MyCheckBoxListener(cardViewAddedState));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.filter);
        builder.setView(view);
        builder.setPositiveButton(R.string.filter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBoxItemName.isChecked()) {
                    String itemName = textInputEditTextItemName.getText().toString().trim();
                    if (!TextUtils.isEmpty(itemName)) {
                        filterItemName = itemName;
                    }
                    else {
                        filterItemName = null;
                    }
                }
                else {
                    filterItemName = null;
                }

                if (checkBoxAddedState.isChecked()) {
                    filterAddedState = spinnerAddedState.getSelectedItemPosition();
                }
                else {
                    filterAddedState = Spinner.INVALID_POSITION;
                }

                Bundle bundle = new Bundle();
                bundle.putString(QUERY, mainRecyclerAdapter.getSearchTag());
                LoaderManager.getInstance(UserRequestsFragment.this).restartLoader(0, bundle, UserRequestsFragment.this).forceLoad();
                getActivity().invalidateOptionsMenu();
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_order_user_request, null, false);
        final RadioGroup radioGroupOrder = view.findViewById(R.id.radioGroup_order_direction);
        RadioButton radioButtonAsc = view.findViewById(R.id.radio_order_asc);
        RadioButton radioButtonDesc = view.findViewById(R.id.radio_order_desc);
        final RadioGroup radioGroupItem = view.findViewById(R.id.radioGroup_user_request);
        RadioButton radioButtonItemName = view.findViewById(R.id.radio_order_user_request_itemName);
        RadioButton radioButtonAddedState = view.findViewById(R.id.radio_order_user_request_addedState);
        RadioButton radioButtonLastModified = view.findViewById(R.id.radio_order_user_request_lastModified);

        if (orderDirection.equals("ASC")) {
            radioButtonAsc.setChecked(true);
        }
        else {
            radioButtonDesc.setChecked(true);
        }
        switch (orderTable) {
            case VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN:
                radioButtonItemName.setChecked(true);
                break;
            case VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN:
                radioButtonAddedState.setChecked(true);
                break;
            case VossitContract.USERREQUESTSENTRY.TABLENAME+"."+VossitContract.LASTMODIFIEDCOLUMN:
                radioButtonLastModified.setChecked(true);
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                    case R.id.radio_order_user_request_itemName:
                        orderTable = VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN;
                        break;
                    case R.id.radio_order_user_request_addedState:
                        orderTable = VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN;
                        break;
                    case R.id.radio_order_user_request_lastModified:
                        orderTable = VossitContract.USERREQUESTSENTRY.TABLENAME+"."+VossitContract.LASTMODIFIEDCOLUMN;
                        break;
                }

                Bundle bundle = new Bundle();
                bundle.putString(QUERY, mainRecyclerAdapter.getSearchTag());
                LoaderManager.getInstance(UserRequestsFragment.this).restartLoader(0, bundle, UserRequestsFragment.this).forceLoad();
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
                String baseQuery = " (" + VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN + " LIKE ? OR "+
                        VossitContract.USERREQUESTSENTRY.ITEMDESCRIPTIONCOLUMN+" LIKE ? " +") ";

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

            if (filterItemName != null) {
                appendAnd(selectionBuilder, arraySelectionArgs).append(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN + " LIKE ?");
                arraySelectionArgs.add("%" + filterItemName + "%");
            }

            if (filterAddedState != Spinner.INVALID_POSITION) {
                appendAnd(selectionBuilder, arraySelectionArgs).append(VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN + "=?");
                arraySelectionArgs.add((filterAddedState-1) + "");
            }
            appendAnd(selectionBuilder, arraySelectionArgs).append(VossitContract.USERREQUESTSENTRY.TABLENAME+"."+VossitContract.USERREQUESTSENTRY.USERIDCOLUMN).append("=?");
            arraySelectionArgs.add(userId);

            return new CursorLoader(getContext(), Uri.withAppendedPath(VossitContract.BASEURI,
                    VossitContract.getUserRequestToUserToUser()),
                    new String[]{VossitContract.USERREQUESTSENTRY.TABLENAME+"."+ VossitContract.USERREQUESTSENTRY._ID,
                            VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN, VossitContract.USERREQUESTSENTRY.ITEMDESCRIPTIONCOLUMN,
                            VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN, VossitContract.USERSENTRY.FIRSTNAMECOLUMN, VossitContract.USERSENTRY.LASTNAMECOLUMN},
                    arraySelectionArgs.size() < 1 ? null : selectionBuilder.toString(),
                    arraySelectionArgs.size() < 1 ? null : arraySelectionArgs.toArray(new String[0]),
                    orderTable + " "+orderDirection );
        }
        else if (id==1){
            ArrayList<String> arraySelectionArgs = new ArrayList<>();
            StringBuilder selectionBuilder = new StringBuilder();
            String baseQuery = " (" + VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIDCOLUMN + " = ? )";
            selectionBuilder.append(baseQuery);
            for (int x = 1; x < mainRecyclerAdapter.getItemCount(); x++) {
                selectionBuilder.append(" OR ").append(baseQuery);
            }

            Cursor cursorItems=mainRecyclerAdapter.getCursor();
            cursorItems.moveToPosition(-1);
            while (cursorItems.moveToNext()){
                arraySelectionArgs.add(cursorItems.getLong(cursorItems.getColumnIndex(VossitContract.USERREQUESTSENTRY._ID)) + "");
            }
            selectionBuilder.append(" AND ").append(VossitContract.USERREQUESTIMAGESENTRY.MAINIMAGECOLUMN).append("=?");
            arraySelectionArgs.add("1");
            return new CursorLoader(getContext(), Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.USERREQUESTIMAGESENTRY.TABLENAME),
                    new String[]{VossitContract.USERREQUESTIMAGESENTRY._ID, VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIMAGECOLUMN,
                            VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIDCOLUMN},
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
                textViewEmpty.setText(getString(R.string.no_items, "user requests"));
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
                    images.put(data.getLong(data.getColumnIndex(VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIDCOLUMN)),
                            data.getString(data.getColumnIndex(VossitContract.USERREQUESTIMAGESENTRY.USERREQUESTIMAGECOLUMN)));
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

    @Override
    public void onUserLog() {
        userId=FirebaseAuth.getInstance().getUid();
        if (userId==null)
            userId="()";
        LoaderManager.getInstance(this).restartLoader(0, null, this).forceLoad();
    }
}
