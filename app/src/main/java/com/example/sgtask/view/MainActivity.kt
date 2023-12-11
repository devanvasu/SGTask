package com.example.sktask

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sktask.databinding.ActivityMainBinding
import com.example.sktask.repository.ImageRepository
import com.example.sktask.response.RetrofitClient
import com.example.sktask.retrofit.ImageUploadService
import com.example.sktask.viewmodel.ImageViewModel
import com.example.sktask.viewmodel.ImageViewModelFactory
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 101
    private lateinit var viewModel: ImageViewModel
    private var imageFile: File? = null
    var binding: ActivityMainBinding? = null

    private var selectedImageUri: Uri? = null

    lateinit var picturePath: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);

        val apiService = RetrofitClient.getInstance().create(ImageUploadService::class.java)
        val imageRepository = ImageRepository(apiService)
        val viewModelFactory = ImageViewModelFactory(imageRepository)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ImageViewModel::class.java)

        binding?.submitButton?.setOnClickListener {
            viewModel.uploadImage(picturePath,"test")
            binding?.progress?.visibility = View.VISIBLE
        }
        viewModel.uploadMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            binding?.progress?.visibility = View.GONE

        })
        binding?.chooseButton?.setOnClickListener {

            if(checkAndRequestPermissions(this)){
                chooseImage(this);
            }
        }
        binding?.previewButton?.setOnClickListener { showPreview() }

    }

    private fun showPreview() {
        selectedImageUri?.let {
            binding?.imagePreview?.setImageURI(it)
            binding?.imagePreview?.visibility = ImageView.VISIBLE
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                     val selectedImage = data.extras!!["data"] as Bitmap
                    convertBitmapImage(selectedImage,System.currentTimeMillis().toString())
                    picturePath = imageFile.toString()
                    selectedImageUri = imageFile?.toUri()
                }

                1 -> if (resultCode == RESULT_OK && data != null) {
                     selectedImageUri = data.data
                    binding?.fileDetailsTextView?.text = "File Name: ${getFileName(selectedImageUri!!)}\nFile Type: ${contentResolver.getType(selectedImageUri!!)}"
                    binding?.fileDetailsTextView?.visibility = TextView.VISIBLE
                    binding?.previewButton?.visibility = Button.VISIBLE
                    binding?.submitButton?.visibility = Button.VISIBLE
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImageUri != null) {
                        val cursor =
                            contentResolver.query(selectedImageUri!!, filePathColumn, null, null, null)
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                             picturePath = cursor.getString(columnIndex)
                            binding?.imagePreview?.setImageBitmap(BitmapFactory.decodeFile(picturePath))
                            cursor.close()
                        }
                    }
                }
            }
        }
    }
    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val name = cursor?.getString(nameIndex!!)
        cursor?.close()
        return name ?: "Unknown"
    }

    fun checkAndRequestPermissions(context: Activity?): Boolean {
            val WExtstorePermission = ContextCompat.checkSelfPermission(
                context!!,
                WRITE_EXTERNAL_STORAGE
            )
            val RExtstorePermission = ContextCompat.checkSelfPermission(
                context!!,
                READ_EXTERNAL_STORAGE
            )
            val cameraPermission = ContextCompat.checkSelfPermission(
                context,
                CAMERA
            )
            val listPermissionsNeeded: MutableList<String> = ArrayList()
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(CAMERA)
            }
            if (RExtstorePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(READ_EXTERNAL_STORAGE)
            }
            if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(
                    context, listPermissionsNeeded.toTypedArray(),
                    REQUEST_ID_MULTIPLE_PERMISSIONS
                )
                return false
            } else {
                return true
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    applicationContext,
                    "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT
                )
                    .show()
            } else if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    applicationContext,
                    "FlagUp Requires Access to Your Storage.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                chooseImage(this@MainActivity)
            }
        }
    }

    private fun chooseImage(context: Context) {
        val optionsMenu = arrayOf<CharSequence>(
            "Take Photo",
            "Choose from Gallery",
            "Exit"
        )
        val builder = AlertDialog.Builder(context)
        builder.setItems(
            optionsMenu
        ) { dialogInterface, i ->
            if (optionsMenu[i] == "Take Photo") {
                // Open the camera and get the photo
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, 0)
            } else if (optionsMenu[i] == "Choose from Gallery") {
                // choose from  external storage
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, 1)
            } else if (optionsMenu[i] == "Exit") {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    private fun convertBitmapImage(bitmap: Bitmap, name: String) {
        val imagePath = File(this.filesDir, name.ifEmpty { "${System.currentTimeMillis()}.jpg" })
        try {
            val os = FileOutputStream(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            imageFile = imagePath
            os.flush()
            os.close()
        } catch (e: Exception) {
            println("error in creating file " + e.message)
        }
    }

}