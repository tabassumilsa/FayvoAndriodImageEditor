package ja.burhanrashid52.photoeditor;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.oginotihiro.cropview.CropUtil;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import eu.inloop.localmessagemanager.LocalMessageManager;

import static ja.burhanrashid52.photoeditor.ImageCroper.EXTRA_CROP_BITMAP;

import static ja.burhanrashid52.photoeditor.ImageCroper.IMAGE_PATH;
import static ja.burhanrashid52.photoeditor.ImageCroper.IMAGE_ROTATE_SHOW;

public class CropImageActivity extends AppCompatActivity implements View.OnClickListener, CropImageView.OnCropImageCompleteListener, CropImageView.OnSetImageUriCompleteListener {

    private CropImageView mCropImageView;
    private ImageView resultIv;
    private ImageView btnRotateLeft;
    private ImageView btnRotateRight;
    String path;
//    String outputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        mCropImageView = findViewById(R.id.cropView);
        resultIv = findViewById(R.id.resultIv);

        btnRotateLeft = findViewById(R.id.btnRotateLeft);
        btnRotateRight = findViewById(R.id.btnRotateRight);
        btnRotateLeft.setVisibility(isRotateShow() ? View.VISIBLE : View.GONE);
        btnRotateRight.setVisibility(isRotateShow() ? View.VISIBLE : View.GONE);

        btnRotateRight.bringToFront();
        btnRotateLeft.bringToFront();
        LinearLayout btnlay = findViewById(R.id.btnlay);
        Button doneBtn = findViewById(R.id.doneBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);

        doneBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        btnRotateLeft.setOnClickListener(this);
        btnRotateRight.setOnClickListener(this);
        path = getIntent().getExtras().getString(IMAGE_PATH);
//        outputPath = getIntent().getExtras().getString(IMAGE_OUTPUT_PATH);
//        Uri source = Uri.fromFile((new File(path)));

   BitmapFactory.Options oldOptions = new BitmapFactory.Options();
        oldOptions.inJustDecodeBounds = true;
        Bitmap tempBitmap = BitmapFactory.decodeFile(new File(path).getAbsolutePath(), oldOptions);
        Bitmap bitmap;
        if(isPanoramicImage(oldOptions.outWidth, oldOptions.outHeight) || isVerticalPanorama(oldOptions.outWidth, oldOptions.outHeight)){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.outWidth = oldOptions.outWidth/8;
            bitmap = BitmapFactory.decodeFile(new File(path).getAbsolutePath(), options);
        }else{
            bitmap = BitmapFactory.decodeFile(new File(path).getAbsolutePath());
        }

        mCropImageView.setImageBitmap(bitmap);
        mCropImageView.setVisibility(View.VISIBLE);
        if(isPanoramicImage(oldOptions.outWidth, oldOptions.outHeight)){
            mCropImageView.rotateImage(90);
        }
        btnlay.setVisibility(View.VISIBLE);
    }

    public static boolean isPanoramicImage(int width, int height) {
        return width > 0 && height > 0 && ((height / width > 2)) || (width == height);
    }

    public static boolean isVerticalPanorama(int width, int height){
        return width > 0 && height > 0 && ((width / height >= 2));
    }
    private boolean isRotateShow() {
        if (getIntent().getExtras() != null) {
            return getIntent().getExtras().getBoolean(IMAGE_ROTATE_SHOW, true);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.doneBtn) {
            mCropImageView.getCroppedImageAsync();
        } else if (id == R.id.cancelBtn) {
            onBackPressed();
        } else if (id == R.id.btnRotateLeft) {
            mCropImageView.rotateImage(-90);
        } else if (id == R.id.btnRotateRight) {
            mCropImageView.rotateImage(90);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    public String savebitmap(Bitmap bitmapImage) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        CropUtil.saveOutput(CropImageActivity.this, destination, bitmapImage, 90);
//        Uri destination = Uri.fromFile(new File(outputPath));
        boolean isSave = CropUtil.saveOutput(CropImageActivity.this, destination, bitmapImage, 90);
        if (isSave)
            return destination.getPath();
        return path;

    }

    private String saveImage(Bitmap bitmap) {
//        String extension = path.substring(path.lastIndexOf("."));
        File file = new File(path);
        String fileName = file.getAbsolutePath();
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);
            File f = new File(fileName);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnCropImageCompleteListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCropImageView.setOnSetImageUriCompleteListener(null);
        mCropImageView.setOnCropImageCompleteListener(null);
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {

    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        final Bitmap croppedBitmap = result.getBitmap();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultIv.setImageBitmap(croppedBitmap);
            }
        });
        /*String destination = "";
        if (!outputPath.equalsIgnoreCase("")) {
            destination = saveImage(croppedBitmap);
        }*/
        try {
//            Intent intent = new Intent();
//            intent.putExtra(EXTRA_CROP_IMAGE, destination);
//            setResult(RESULT_OK, intent);
            LocalMessageManager.getInstance().send(EXTRA_CROP_BITMAP, croppedBitmap);
            finish();
        } catch (Exception e) {
            Log.d("Exception", e.getStackTrace().toString());
            setResult(RESULT_CANCELED);
            finish();
        }
    }

}
