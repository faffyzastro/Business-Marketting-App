package co.ke.tonyoa.android.vossified;

import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.LongSparseArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ke.tonyoa.android.vossified.Data.VossitContract;
import co.ke.tonyoa.android.vossified.Libraries.CircleTransform;
import co.ke.tonyoa.android.vossified.Libraries.CursorRecyclerViewAdapter;
import ir.mirrajabi.searchdialog.StringsHelper;
import jp.wasabeef.picasso.transformations.BlurTransformation;


interface MyOnClickListener {
    void onClick(View view, int position);

    void onItemLongClick(View view, int position);
}

public class MainRecyclerAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final String caller;
    private String searchTag;
    private Cursor originalCursor;
    private String mHighlightColor = "#FF9FD81B";
    private int lastFirstVisiblePosition;
    private LongSparseArray<String> images;
    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    private MyOnClickListener myOnClickListener = new MyOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            getCursor().moveToPosition(position);

            if (caller.equals(CategoriesFragment.class.getName())) {
                if (firebaseAuth.getCurrentUser()==null){
                    Snackbar.make(view, "Log in and verify all your items", BaseTransientBottomBar.LENGTH_INDEFINITE).
                            setAction("Log in", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(context, LoginActivity.class);
                            context.startActivity(intent);
                        }
                    }).show();
                    return;
                }
                Intent intent = new Intent(context, ItemListActivity.class);
                intent.putExtra(VossitContract.ITEMSENTRY._ID,
                        getCursor().getLong(getCursor().getColumnIndex(VossitContract.CATEGORIESENTRY._ID)));
                intent.putExtra(VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN,
                        getCursor().getString(getCursor().getColumnIndex(VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN)));
                context.startActivity(intent);
                return;
            }
            Class cls = null;
            String tableName = null;
            if (caller.equals(ItemListActivity.class.getName())) {
                cls = ItemActivity.class;
                tableName = VossitContract.ITEMSENTRY.TABLENAME;
            }
            else if (caller.equals(UserRequestsFragment.class.getName())) {
                cls = UserRequestActivity.class;
                tableName = VossitContract.USERREQUESTSENTRY.TABLENAME;
            }
            if (cls != null && tableName != null) {
                Intent intent = new Intent(context, cls);
                intent.setData(ContentUris.withAppendedId(Uri.withAppendedPath(VossitContract.BASEURI,
                        tableName), getCursor().getLong(getCursor().getColumnIndex(VossitContract.ITEMSENTRY._ID))));
                context.startActivity(intent);
            }

        }

        @Override
        public void onItemLongClick(View view, int position) {

        }
    };

    public int getLastFirstVisiblePosition() {
        return lastFirstVisiblePosition;
    }

    public void setLastFirstVisiblePosition(int lastFirstVisiblePosition) {
        this.lastFirstVisiblePosition = lastFirstVisiblePosition;
    }

    public MainRecyclerAdapter(Activity context, Cursor cursor, String caller) {
        super(context, cursor);
        this.context = context;
        this.caller = caller;
        originalCursor = cursor;
        images=new LongSparseArray<>();
    }

    static StringBuilder setWhere(StringBuilder where, int selectionLength, String column) {
        where.append(column).append("=?");
        for (int y = 1; y < selectionLength; y++) {
            where.append(" OR ").append(column).append("=?");
        }
        return where;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        if (caller.equals(ItemListActivity.class.getName())) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            itemViewHolder.bind(cursor);
        }
        else if (caller.equals(CategoriesFragment.class.getName())) {
            CategoryViewHolder categoryViewHolder = (CategoryViewHolder) viewHolder;
            categoryViewHolder.bind(cursor);
        }
        else if (caller.equals(UserRequestsFragment.class.getName())) {
            UserRequestViewHolder userRequestViewHolder = (UserRequestViewHolder) viewHolder;
            userRequestViewHolder.bind(cursor);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (caller.equals(ItemListActivity.class.getName())) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_item, parent, false);
            return new ItemViewHolder(view);
        }
        else if (caller.equals(CategoriesFragment.class.getName())) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        }
        else if (caller.equals(UserRequestsFragment.class.getName())) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_user_request, parent, false);
            return new UserRequestViewHolder(view);
        }
        return null;
    }

    public String getSearchTag() {
        return searchTag;
    }

    public void setSearchTag(String searchTag) {
        this.searchTag = searchTag;
    }

    public Cursor getOriginalCursor() {
        return originalCursor;
    }

    public void setOriginalCursor(Cursor originalCursor) {
        this.originalCursor = originalCursor;
        swapCursor(originalCursor);
    }

    public LongSparseArray<String> getImages() {
        return images;
    }

    public void setImages(LongSparseArray<String> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public static String getCategoryHexStringColor(long id){
        return Integer.toHexString(getCategoryColor(id));
    }

    public static int getCategoryColor(long id){
        long color=(id*7)+(id*11);
        while (color>255){
            color-=(id*5);
        }
        return (int)color;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageView_item_item_mainImage)
        ImageView imageViewMain;
        @BindView(R.id.textView_item_item_name)
        TextView textViewName;
        @BindView(R.id.imageView_item_item_category)
        ImageView imageViewCategory;
        @BindView(R.id.textView_item_item_cost)
        TextView textViewCost;

        public ItemViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myOnClickListener.onClick(itemView, getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    myOnClickListener.onItemLongClick(itemView, getAdapterPosition());
                    return true;
                }
            });
        }

        public void bind(Cursor cursor) {
            Picasso.with(context).load(images.get(cursor.getLong(cursor.getColumnIndex(VossitContract.ITEMSENTRY._ID)))).placeholder(R.drawable.v).into(imageViewMain);
            Drawable drawable=context.getResources().getDrawable(android.R.drawable.star_off);
            Picasso.with(context).load(cursor.getString(cursor.getColumnIndex(VossitContract.CATEGORYIMAGESENTRY.CATEGORYIMAGECOLUMN))).
                    placeholder(drawable).transform(new CircleTransform()).into(imageViewCategory);
            if (searchTag != null) {
                textViewName.setText(StringsHelper.highlightLCS(cursor.getString(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMNAMECOLUMN)), getSearchTag(), Color.parseColor(mHighlightColor)));
            }
            else {
                textViewName.setText(cursor.getString(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMNAMECOLUMN)));
            }
            float cost = cursor.getFloat(cursor.getColumnIndex(VossitContract.ITEMSENTRY.ITEMCOSTCOLUMN));
            if (cost>0){
                textViewCost.setVisibility(View.VISIBLE);
            }
            else {
                textViewCost.setVisibility(View.GONE);
            }
            textViewCost.setText(context.getString(R.string.cost, cost));
        }
    }


    class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageView_item_category)
        ImageView imageView;
        @BindView(R.id.textView_item_category)
        TextView textView;

        public CategoryViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myOnClickListener.onClick(itemView, getAdapterPosition());
                }
            });
        }

        public void bind(Cursor cursor) {
            Picasso.with(context).load(cursor.getString(cursor.getColumnIndex(VossitContract.CATEGORYIMAGESENTRY.CATEGORYIMAGECOLUMN))).
                transform(new BlurTransformation(context, 10, 1)).
                placeholder(new ColorDrawable(0xff000000 + Integer.parseInt(getCategoryHexStringColor(cursor.getLong(cursor.getColumnIndex(VossitContract.CATEGORYIMAGESENTRY._ID))), 16))).
                into(imageView);
            textView.setText(cursor.getString(cursor.getColumnIndex(VossitContract.CATEGORIESENTRY.CATEGORYNAMECOLUMN)));
        }
    }

    class UserRequestViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageView_item_userRequest)
        ImageView imageView;
        @BindView(R.id.textView_item_userRequest_itemName)
        TextView textViewItemName;
        @BindView(R.id.imageView_item_userRequest_status)
        ImageView imageViewStatus;

        public UserRequestViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myOnClickListener.onClick(itemView, getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    myOnClickListener.onItemLongClick(itemView, getAdapterPosition());
                    return true;
                }
            });
        }

        public void bind(Cursor cursor) {
            Picasso.with(context).load(images.get(cursor.getLong(cursor.getColumnIndex(VossitContract.USERREQUESTIMAGESENTRY._ID)))).placeholder(R.drawable.v).into(imageView);
            UserRequestActivity.setStatusImage(context, imageViewStatus, cursor.getInt(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ADDEDCOLUMN)));
            String name=cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.FIRSTNAMECOLUMN))+" "+cursor.getString(cursor.getColumnIndex(VossitContract.USERSENTRY.LASTNAMECOLUMN));
            if (searchTag != null) {
                textViewItemName.setText(StringsHelper.highlightLCS(cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN)), getSearchTag(), Color.parseColor(mHighlightColor)));
            }
            else {
                textViewItemName.setText(cursor.getString(cursor.getColumnIndex(VossitContract.USERREQUESTSENTRY.ITEMNAMECOLUMN)));
            }
        }

    }

}
