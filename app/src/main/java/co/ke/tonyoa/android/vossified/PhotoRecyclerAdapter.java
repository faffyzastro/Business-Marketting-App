package co.ke.tonyoa.android.vossified;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ke.tonyoa.android.vossified.POJOs.Image;

interface MyCheckListener {
    void onCheckChange(View view, int position, boolean isChecked);
}

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.PhotoViewHolder> {

    private Context context;
    private String caller;
    private List<Image> images;
    private Utils.MyTextWatcher myTextWatcher=null;
    private RecyclerView recyclerView;
    private List<Image> deletedImages;
    private int visibleImage=-1;
    private boolean editMode;

    private MyCheckListener myCheckListener=new MyCheckListener() {
        @Override
        public void onCheckChange(View view, int position, boolean isChecked) {
            Image image=images.get(position);
            if (!image.isMainImage() && isChecked){
                int previousMain;
                for (previousMain = 0; previousMain<images.size(); previousMain++){
                    Image allImage=images.get(previousMain);
                    if (allImage.isMainImage()){
                        allImage.setMainImage(false);
                        break;
                    }
                }
                image.setMainImage(true);
                int finalPreviousMain = previousMain;
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemChanged(finalPreviousMain);
                        notifyItemChanged(position);
                    }
                });
            }
        }
    };

    private MyOnClickListener deleteClickListener=new MyOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            removeImage(position);
        }

        @Override
        public void onItemLongClick(View view, int position) {

        }
    };

    public PhotoRecyclerAdapter(Context context, String caller){
        this.context = context;
        this.caller = caller;
        images=new ArrayList<>();
        deletedImages=new ArrayList<>();
    }

    public PhotoRecyclerAdapter(Context context, String caller, List<Image> images, Utils.MyTextWatcher myTextWatcher, RecyclerView recyclerView){
        this.context = context;
        this.caller = caller;
        this.images = images;
        this.myTextWatcher = myTextWatcher;
        this.recyclerView = recyclerView;
        deletedImages=new ArrayList<>();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (images!=null)
            return images.size();
        return 0;
    }

    public List<Image> getDeletedImages() {
        return deletedImages;
    }

    public void setDeletedImages(List<Image> deletedImages) {
        this.deletedImages = deletedImages;
    }

    public void addDeletedImage(Image image){
        deletedImages.add(image);
    }

    public int getVisibleImage() {
        return visibleImage;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageView_item_image)
        ImageView imageView;
        @BindView(R.id.imageView_item_Image_delete)
        ImageView imageViewDelete;
        @BindView(R.id.checkBox_item_image_main)
        CheckBox checkBoxMain;
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            checkBoxMain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    myCheckListener.onCheckChange(buttonView, getAdapterPosition(), isChecked);
                }
            });
            imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteClickListener.onClick(v, getAdapterPosition());
                }
            });
        }

        void bind (int position){
            if (editMode && caller.equals(UserRequestActivity.class.getName())){
                imageViewDelete.setVisibility(View.VISIBLE);
                checkBoxMain.setEnabled(true);
            }
            else {
                imageViewDelete.setVisibility(View.GONE);
                checkBoxMain.setEnabled(false);
            }
            Image image = images.get(position);
            if (image.getId()==-1){
                Picasso.with(context).load(image.getLocalPath()).placeholder(R.drawable.v).into(imageView);
            }
            else {
                Picasso.with(context).load(image.getLink()).placeholder(R.drawable.v).into(imageView);
            }
            if (image.isMainImage()){
                checkBoxMain.setClickable(false);
            }
            else {
                checkBoxMain.setClickable(true);
            }
            checkBoxMain.setChecked(image.isMainImage());
            visibleImage=position;
        }
    }

    public void addImage(Image image){
        images.add(image);
        if (myTextWatcher!=null){
            myTextWatcher.setModified(true);
        }
        notifyDataSetChanged();
    }

    public void removeImage(int imageIndex){
        Image image = images.get(imageIndex);
        deletedImages.add(image);
        images.remove(imageIndex);
        if (image.isMainImage() && images.size()>0){
            images.get(0).setMainImage(true);
        }
        if (myTextWatcher!=null){
            myTextWatcher.setModified(true);
        }
        notifyItemRemoved(imageIndex);
        notifyItemChanged(0);
    }

    public void setImages(List<Image> images) {
        this.images =new ArrayList<>(images);
        notifyDataSetChanged();
    }

    public List<Image> getImages(){
        return images;
    }
}
