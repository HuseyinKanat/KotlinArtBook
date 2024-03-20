package com.example.artbook

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.example.artbook.databinding.ActivityArtGalleryBinding
import com.example.artbook.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception

class ArtGallery : AppCompatActivity() {
    private lateinit var binding: ActivityArtGalleryBinding
    private  lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var  permissionLauncher : ActivityResultLauncher<String>
    private lateinit var  database : SQLiteDatabase
    private var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtGalleryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
        registerLauncher()
        val intent=intent
        var info = intent.getStringExtra("info")
        if(info.equals("new")){
            binding.btnSave.visibility= View.VISIBLE
            binding.etArtName.setText("")
            binding.etArtistName.setText("")
            binding.etYear.setText("")
            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.ic_empty)
            binding.imageView.setImageBitmap(selectedImageBackground)
        }
        else{
            println("old come")
            binding.btnSave.visibility= View.INVISIBLE
            var selectedId =intent.getIntExtra("id",0)
            val cursor =database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()) )
            val artNameIx= cursor.getColumnIndex("artName")
            val artistNameIx = cursor.getColumnIndex("artistName")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")
            while(cursor.moveToNext()){
                binding.etArtName.setText(cursor.getString(artNameIx))
                binding.etArtistName.setText(cursor.getString(artistNameIx))
                binding.etYear.setText(cursor.getString(yearIx))
                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)


            }
            cursor.close()

        }
    }
    fun selectImage(view: View){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    //Rationale
                    Snackbar.make(view,"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
        }
        else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //Rationale
                    Snackbar.make(view,"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }
                else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }

        }


    }

    fun saveArt(view: View){
        val artName = binding.etArtName.text.toString()
        val artistName = binding.etArtistName.text.toString()
        val year = binding.etYear.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()
            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artName VARCHAR, artistName VARCHAR, year VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO arts (artName,artistName,year,image) VALUES(?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)
                statement.execute()






            }catch (e : Exception){
                println("fail")
                e.printStackTrace()
            }
            val intent = Intent(this@ArtGallery, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // All previous activities pop from stack
            startActivity(intent)

        }
    }
    private  fun makeSmallerBitmap(image : Bitmap, maximumSize:Int):Bitmap{
        var width = image.width
        var height = image.height
        val bitmapRatio: Double = width.toDouble()/height.toDouble()
        if(bitmapRatio> 0){
            //landscape
            width= maximumSize
            val scaledHeight =width/bitmapRatio
            height= scaledHeight.toInt()
        }
        else{
            //portrait
            height= maximumSize
            val scaledWidth =height * bitmapRatio
            width= scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }


    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode== RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult!= null){
                    val imageData = intentFromResult.data
                    if (imageData!= null){
                        try {
                            if(Build.VERSION.SDK_INT>= 28){
                                val source = ImageDecoder.createSource(this@ArtGallery.contentResolver,imageData)
                                selectedBitmap= ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                            else{
                                selectedBitmap= MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)

                            }
                        }
                        catch (e: Exception){
                            e.printStackTrace()
                        }
                    }


                }
            }

        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if (result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else{
                Toast.makeText(this@ArtGallery, "Permission needed",Toast.LENGTH_LONG).show()
            }

        }
    }
}