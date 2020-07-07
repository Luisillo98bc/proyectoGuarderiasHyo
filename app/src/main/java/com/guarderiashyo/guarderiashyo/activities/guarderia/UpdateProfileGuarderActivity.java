package com.guarderiashyo.guarderiashyo.activities.guarderia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.Utils.CompressorBitmapImage;
import com.guarderiashyo.guarderiashyo.Utils.FileUtil;
import com.guarderiashyo.guarderiashyo.activities.client.UpdateProfileActivity;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;
import com.guarderiashyo.guarderiashyo.models.Client;
import com.guarderiashyo.guarderiashyo.models.Guarderia;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.ClientProvider;
import com.guarderiashyo.guarderiashyo.providers.GuarderiaProvider;
import com.guarderiashyo.guarderiashyo.providers.ImagesProvider;
import com.squareup.picasso.Picasso;

import java.io.File;

public class UpdateProfileGuarderActivity extends AppCompatActivity {

    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewName;
    private TextView mTextViewServicios;

    private GuarderiaProvider mGuarderProvider;
    private AuthProvider mAuthProvider;
    private ImagesProvider mImageProvider;

    private File mImageFile;
    private String mImage;
    private final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;

    private String mName;
    private String mServicios;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_guarder);
        MyToolbar.show(this, "Actualizar Perfil", true);

        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mTextViewName = findViewById(R.id.txtActua_nombre);
        mButtonUpdate = findViewById(R.id.btnActualizar);
        mTextViewServicios = findViewById(R.id.txtUpdateServicios);

        mGuarderProvider = new GuarderiaProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImagesProvider("guarder_images");

        mProgressDialog = new ProgressDialog(this);

        getGuarderInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGallery();

            }
        });

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }
    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            try {
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));

            }catch (Exception e){
                Log.d("ERROR", "Mensaje: "+e.getMessage());
            }
        }
    }

    private void getGuarderInfo(){
        mGuarderProvider.getGuarderia(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String name = dataSnapshot.child("name").getValue().toString();
                    String servicios = dataSnapshot.child("servicios").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileGuarderActivity.this).load(image).into(mImageViewProfile);

                    }

                    mTextViewName.setText(name);
                    mTextViewServicios.setText(servicios);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateProfile() {

        mName = mTextViewName.getText().toString();
        mServicios = mTextViewServicios.getText().toString();

        if (!mName.equals("")&& mImageFile != null){
            mProgressDialog.setMessage("Espera un momento...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();

        }else{
            Toast.makeText(this, "Ingresar la imagen y el nombre",Toast.LENGTH_SHORT).show();
        }

    }

    private void saveImage() {
       mImageProvider.saveImage(UpdateProfileGuarderActivity.this, mImageFile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Guarderia guarderia = new Guarderia();
                            guarderia.setImage(image);
                            guarderia.setName(mName);
                            guarderia.setServicios(mServicios);
                            guarderia.setId(mAuthProvider.getId());
                            mGuarderProvider.update(guarderia).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfileGuarderActivity.this, "Su informacion se actualizo correctamente",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }
                else{
                    Toast.makeText(UpdateProfileGuarderActivity.this, "Hubo un error al subir la imagen",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}