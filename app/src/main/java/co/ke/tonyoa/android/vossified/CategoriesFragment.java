package co.ke.tonyoa.android.vossified;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Utils.RECYCLERPOSITION;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnUserLog  {


    @BindView(R.id.button_categories_login)
    Button buttonCategories;
    @BindView(R.id.recyclerView_categories)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar_categories)
    ProgressBar progressBarCategories;

    private Unbinder unbinder;

    private MainRecyclerAdapter mainRecyclerAdapter;

    public CategoriesFragment() {
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_categories, container, false);
        unbinder = ButterKnife.bind(this, view);
        mainRecyclerAdapter = new MainRecyclerAdapter(getActivity(), null, this.getClass().getName());

        recyclerView.setAdapter(mainRecyclerAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        if (savedInstanceState != null) {
            mainRecyclerAdapter.setLastFirstVisiblePosition(savedInstanceState.getInt(RECYCLERPOSITION));
        }
        LoaderManager.getInstance(this).initLoader(0, null, this).forceLoad();
        showLoginButton();
        return view;
    }

    private void showLoginButton() {
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            buttonCategories.setVisibility(View.GONE);
        }
        else {
            buttonCategories.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.button_categories_login)
    public void login(){
        Intent intent=new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RECYCLERPOSITION, mainRecyclerAdapter.getLastFirstVisiblePosition());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == 0) {
            Utils.showProgressBar(progressBarCategories, recyclerView, true);
            ArrayList<String> arraySelectionArgs = new ArrayList<>();
            StringBuilder selectionBuilder = new StringBuilder();

            return new CursorLoader(getContext(), Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.getCategoryToCategoryImage()),
                    new String[]{VossitContract.CATEGORIESENTRY.TABLENAME+"."+ VossitContract.CATEGORIESENTRY._ID,
                            VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN, VossitContract.CATEGORYIMAGESENTRY.CATEGORYIMAGECOLUMN},
                    arraySelectionArgs.size() < 1 ? null : selectionBuilder.toString(),
                    arraySelectionArgs.size() < 1 ? null : arraySelectionArgs.toArray(new String[0]),
                    VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN + " ASC" );
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 0) {
            Utils.showProgressBar(progressBarCategories, recyclerView, false);
            if (data != null) {
                mainRecyclerAdapter.setOriginalCursor(data);
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(mainRecyclerAdapter.getLastFirstVisiblePosition());
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == 0) {
            mainRecyclerAdapter.setOriginalCursor(null);
        }
    }


    @Override
    public void onUserLog() {
        showLoginButton();
    }
}
