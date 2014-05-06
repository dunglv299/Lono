package com.teusoft.lono.business;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.teusoft.lono.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

/**
 * Created by DungLV on 29/4/2014.
 */
public class Sharing {
    private Context context;
    private RelativeLayout mainLayout;
    private Bitmap mBitmap;

    public Sharing(Context context, RelativeLayout mainLayout) {
        this.context = context;
        this.mainLayout = mainLayout;
    }

    public void share() {
        mBitmap = getBitmapFromViewWithColor(mainLayout, Color.WHITE);
        new SaveImageAsync().execute();

    }

    /**
     * Save image in asyntask
     */
    public class SaveImageAsync extends AsyncTask<Void, String, Void> {
        String photoPath = null;
        private final ProgressDialog dialog = new ProgressDialog(context,
                AlertDialog.THEME_HOLO_LIGHT);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... filePath) {
            photoPath = createImageFileName();
            exportBitmapToFile(mBitmap, photoPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void filename) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            shareSocialIntent("Facebook", photoPath);
        }
    }

    /**
     * Export bitmap to file in project
     */
    public void exportBitmapToFile(Bitmap bitmap, String imageFileName) {
        try {
            FileOutputStream out = new FileOutputStream(imageFileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get folder path
     *
     * @param context
     * @return
     */
    public File getAlbumDir(Context context) {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            storageDir = context
                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.e("Pictures", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(context.getString(R.string.app_name),
                    "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    /**
     * Create image file name
     *
     * @return
     */
    private String createImageFileName() {
        try {
            // Create an image file name
            String imageFileName = "ScreenShot.jpg";
            File albumF = getAlbumDir(context);
            File imageF = new File(albumF, imageFileName);
            return imageF.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Export bitmap with transperant to white
     *
     * @param view
     * @return
     */
    public Bitmap getBitmapFromViewWithColor(View view, int color) {
        if (view.getWidth() > 0) {
            // Define a bitmap with the same size as the view
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),
                    view.getHeight(), Bitmap.Config.ARGB_8888);
            // Bind a canvas to it
            Canvas canvas = new Canvas(returnedBitmap);
            // Get the view's background
            Drawable bgDrawable = view.getBackground();
            if (bgDrawable != null) {
                // has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas);
            } else {
                // does not have background drawable, then draw white background
                // on
                // the canvas
                canvas.drawColor(color);
            }
            // draw the view on the canvas
            view.draw(canvas);

            canvas.drawBitmap(returnedBitmap, new Matrix(), null);
            Bitmap overlayBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.lono_300);

            canvas.drawBitmap(overlayBitmap, new Matrix(), null);
            // return the bitmap
            return returnedBitmap;
        }
        return null;
    }

    /**
     * Utils share via intent
     *
     * @param nameApp
     * @param imagePath
     */
    public void shareSocialIntent(String nameApp,
                                  String imagePath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        List<ResolveInfo> resInfo = context.getPackageManager()
                .queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()) {
            boolean isHasApplication = false;
            for (ResolveInfo info : resInfo) {
                Intent targetedShare = new Intent(
                        Intent.ACTION_SEND);
                targetedShare.setType("image/jpeg"); // put here your mime type
                if (info.activityInfo.packageName.toLowerCase(
                        Locale.getDefault()).contains(
                        nameApp.toLowerCase(Locale.getDefault()))
                        || info.activityInfo.name.toLowerCase(
                        Locale.getDefault()).contains(
                        nameApp.toLowerCase(Locale.getDefault()))) {
                    isHasApplication = true;
                    targetedShare.putExtra(Intent.EXTRA_TEXT, "");
                    targetedShare.putExtra(Intent.EXTRA_STREAM,
                            Uri.fromFile(new File(imagePath)));
                    targetedShare.setPackage(info.activityInfo.packageName);
                    context.startActivity(targetedShare);
                    break;
                }
            }
            if (!isHasApplication) {
                Toast.makeText(context,
                        "Please install " + nameApp + " application",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
