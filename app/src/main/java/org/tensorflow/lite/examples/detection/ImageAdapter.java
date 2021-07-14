package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> implements View.OnClickListener {

    private List<DetectedImage> imageList = new ArrayList<>();;
    private Context context;

    public void AddData(DetectedImage newData, int position) {
        imageList.add(position, newData);
        notifyDataSetChanged();
    }

    public void UpdateData (List<DetectedImage> data) {
        imageList.clear();
        imageList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        ImageView imageView = (ImageView) v;
        try {
            imageView.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap image = drawable.getBitmap();
            Intent intent = new Intent(context, EdgeDetectionActivity.class);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            intent.putExtra("BMP",bytes);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView dateText;
        public final ImageView imageView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.imageRowTextView);
            imageView = itemView.findViewById(R.id.imageRowSrc);

        }

        public void SetTextToView(DetectedImage detectedImage) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            imageView.setImageBitmap(detectedImage.getImage());
            dateText.setText("Taken on " + format.format(detectedImage.getCreationDate()) + " by user.");
        }

    }

    public ImageAdapter(List<DetectedImage> images, Context context) {
        this.imageList = images;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.image_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.SetTextToView(imageList.get(position));
        viewHolder.imageView.setOnClickListener(this);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (imageList == null) return 0;
        return imageList.size();
    }
}
