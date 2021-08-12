package co.ke.tonyoa.android.vossified;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ke.tonyoa.android.vossified.Data.VossitContract;
import co.ke.tonyoa.android.vossified.Data.VossitOpenHelper;
import co.ke.tonyoa.android.vossified.Data.VossitProvider;
import co.ke.tonyoa.android.vossified.POJOs.ItemClick;
import co.ke.tonyoa.android.vossified.POJOs.Link;

public class LinkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<Link> links;

    private boolean editMode = true;

    private MyOnClickListener linkOnClickListener = new MyOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            Link link = links.get(position);
            ContentValues contentValues=new ContentValues();
            contentValues.put(VossitContract.ITEMCLICKSENTRY.ITEMIDCOLUMN, link.getItemId());
            contentValues.put(VossitContract.ITEMCLICKSENTRY.USERIDCOLUMN, mUserId);
            context.getContentResolver().insert(Uri.withAppendedPath(VossitContract.BASEURI, VossitContract.ITEMCLICKSENTRY.TABLENAME),
                    contentValues);
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(link.getLink()));
            context.startActivity(intent);
            Log.e("Click", "Item clicked");
        }

        @Override
        public void onItemLongClick(View view, int position) {

        }
    };
    private final String mUserId;

    public LinkAdapter(Context context, List<Link> links) {
        this.context = context;
        this.links = links;
        if (links == null)
            this.links = new ArrayList<>();
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        mUserId = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LinkViewHolder(LayoutInflater.from(context).inflate(R.layout.item_link, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LinkViewHolder) {
            LinkViewHolder linkViewHolder = (LinkViewHolder) holder;
            linkViewHolder.bind(position);
        }
    }

    @Override
    public int getItemCount() {
        if (links != null)
            return links.size();
        return 0;
    }

    public void addLink(Link link) {
        if (links == null)
            links = new ArrayList<>();
        int added = links.size();
        links.add(link);
        notifyItemInserted(added);
    }

    public List<Link> getLinks() {
        if (links == null)
            links = new ArrayList<>();
        return links;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.editMode = isEditMode;
        notifyDataSetChanged();
    }

    class LinkViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textView_item_link_link)
        TextView textViewLink;
        @BindView(R.id.textView_item__link_store)
        TextView textViewStore;
        @BindView(R.id.linearLayout_spelling_item)
        LinearLayout mLinearLayout;

        public LinkViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linkOnClickListener.onClick(v, getAdapterPosition());
                }
            });
            mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linkOnClickListener.onClick(v, getAdapterPosition());
                }
            });
            textViewLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linkOnClickListener.onClick(v, getAdapterPosition());
                }
            });
            textViewStore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linkOnClickListener.onClick(v, getAdapterPosition());
                }
            });
        }

        public void bind(int position) {
            Link link = links.get(position);
            textViewLink.setText(link.getLink());
            textViewStore.setText(link.getStore());
        }
    }

}
