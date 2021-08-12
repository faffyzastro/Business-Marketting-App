package co.ke.tonyoa.android.vossified;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

import co.ke.tonyoa.android.vossified.Data.SyncWorker;
import co.ke.tonyoa.android.vossified.Data.VossitContract;

import static co.ke.tonyoa.android.vossified.Data.SyncWorker.LAST_MODIFIED_USERREQUESTIMAGES;
import static co.ke.tonyoa.android.vossified.Data.SyncWorker.LAST_MODIFIED_USERREQUESTS;
import static co.ke.tonyoa.android.vossified.Utils.getSystemTime;

public class MainActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener{


    public static final String VIEWABLE_FRAGMENT = "viewableFragment";
    private CategoriesFragment categoriesFragment;
    private UserRequestsFragment userRequestsFragment;
    private Fragment viewableFragment;

    private BroadcastReceiver networkReceiver;

    private NavigationView navigationView;

    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //Restore fragments on configuration changes
        if (savedInstanceState != null) {
            categoriesFragment = (CategoriesFragment) getSupportFragmentManager().getFragment(savedInstanceState, getString(R.string.categories));
            if (categoriesFragment == null) {
                categoriesFragment = new CategoriesFragment();
            }
            userRequestsFragment = (UserRequestsFragment) getSupportFragmentManager().getFragment(savedInstanceState, getString(R.string.item_request));
            if (userRequestsFragment == null) {
                userRequestsFragment = new UserRequestsFragment();
            }
            viewableFragment = getSupportFragmentManager().getFragment(savedInstanceState, VIEWABLE_FRAGMENT);
            if (viewableFragment instanceof CategoriesFragment) {
                setTitle(getString(R.string.categories));
                navigationView.setCheckedItem(R.id.nav_categories);
            }
            else if (viewableFragment instanceof UserRequestsFragment) {
                setTitle(getString(R.string.item_request));
                navigationView.setCheckedItem(R.id.nav_request);
            }
        } else {
            categoriesFragment=new CategoriesFragment();
            userRequestsFragment=new UserRequestsFragment();
            viewableFragment = categoriesFragment;
            setTitle(getString(R.string.categories));
            navigationView.setCheckedItem(R.id.nav_categories);
        }
        setHeaderText();
        MenuItem menuItem=navigationView.getMenu().findItem(R.id.nav_sign);

        if (firebaseAuth.getCurrentUser()==null){
            menuItem.setTitle(R.string.action_sign_in_short);
        }
        else {
            menuItem.setTitle(R.string.sign_out);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.host_container, viewableFragment, getTitle().toString()).commit();

        networkReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();
    }

    private void setHeaderText(){
        View view=navigationView.getHeaderView(0);
        TextView textViewName=view.findViewById(R.id.textView_nav_header_name);
        TextView textViewEmail=view.findViewById(R.id.textView_nav_header_email);
        String name="Vossified";
        String email="Verify your life";
        String userId=FirebaseAuth.getInstance().getUid();
        if (userId!=null) {
            Cursor cursor = getContentResolver().query(Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.USERSENTRY.TABLENAME),
                    new String[]{VossitContract.USERSENTRY.FIRSTNAMECOLUMN, VossitContract.USERSENTRY.LASTNAMECOLUMN,
                            VossitContract.USERSENTRY.EMAILCOLUMN, VossitContract.USERSENTRY._ID}, VossitContract.USERSENTRY.USERIDCOLUMN+
                    "=?", new String[]{userId}, null);
            if (cursor!=null && cursor.moveToFirst()){
                String firstName=cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.FIRSTNAMECOLUMN));
                String lastName=cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.LASTNAMECOLUMN));
                name=firstName+" "+lastName;
                email=cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.EMAILCOLUMN));
            }
        }
        textViewName.setText(name);
        textViewEmail.setText(email);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        /*if (MainRecyclerAdapter.actionMode != null)
            MainRecyclerAdapter.actionMode.finish();*/
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_categories:
                setTitle(getString(R.string.categories));
                viewableFragment = categoriesFragment;
                break;
            case R.id.nav_request:
                setTitle(getString(R.string.item_request));
                viewableFragment = userRequestsFragment;
                break;
            case R.id.nav_sign:
                if (menuItem.getTitle().equals(getString(R.string.sign_out))) {
                    firebaseAuth.signOut();
                    if (categoriesFragment.isAdded()) {
                        ((OnUserLog) categoriesFragment).onUserLog();
                    }
                    if (userRequestsFragment.isAdded()) {
                        ((OnUserLog) userRequestsFragment).onUserLog();
                    }
                    setHeaderText();
                    menuItem.setTitle(R.string.action_sign_in_short);
                }
                else {
                    Intent intent=new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    menuItem.setTitle(R.string.sign_out);
                }
                break;
        }
        if (id==R.id.nav_categories || id==R.id.nav_request) {
            getSupportFragmentManager().beginTransaction().replace(R.id.host_container, viewableFragment, getTitle().toString()).commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, VIEWABLE_FRAGMENT, viewableFragment);
        if (categoriesFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, getString(R.string.categories), categoriesFragment);
        }
        if (userRequestsFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, getString(R.string.item_request), userRequestsFragment);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        viewableFragment = getSupportFragmentManager().getFragment(savedInstanceState, VIEWABLE_FRAGMENT);
        Fragment fragmentCategories = getSupportFragmentManager().getFragment(savedInstanceState, getString(R.string.categories));
        if (fragmentCategories!= null) {
            categoriesFragment = (CategoriesFragment) fragmentCategories;
        }
        Fragment fragmentUserRequests = getSupportFragmentManager().getFragment(savedInstanceState, getString(R.string.item_request));
        if (fragmentUserRequests != null) {
            userRequestsFragment = (UserRequestsFragment) fragmentUserRequests;
        }
    }

    private void registerNetworkBroadcastForNougat() {
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }

}
