package com.example.flowers

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.MaskFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flowers.databinding.ActivityMainBinding
import java.lang.Exception
import java.text.SimpleDateFormat

class MainActivity : BaseActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val PERM_STORAGE = 9
    val PERM_CAMERA = 10
    val REQ_CAMERA = 11
    val REQ_GALLARY = 12
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        requirePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),PERM_STORAGE)

    }
    fun initView() {
        binding.camerabtn.setOnClickListener{
            requirePermissions(arrayOf(Manifest.permission.CAMERA),PERM_CAMERA)
            openCamera()
        }
        binding.gallarybtn.setOnClickListener{
            openGallary()
        }

    }

    override fun permissionGranted(requestCode: Int) {
        when(requestCode) {
            PERM_STORAGE -> initView()

            PERM_CAMERA -> openCamera()

        }

    }
    override fun permissionDenied(requestCode: Int) {
        when(requestCode){
            PERM_STORAGE ->{
                Toast.makeText(this,"스토리지 권한승인 실패",Toast.LENGTH_SHORT).show()
                finish()
            }
            REQ_CAMERA -> Toast.makeText(this,"카메라 권한승인 실패",Toast.LENGTH_SHORT).show()

        }

    }

    var realUri : Uri? = null

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createImageUri(newfileName(),"image/jpg")?.let { uri ->
            realUri = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
            startActivityForResult(intent, REQ_CAMERA)
        }
    }

    fun openGallary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQ_GALLARY)
    }

    fun createImageUri(filename: String, mimeType:String) : Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
    }

    fun newfileName() : String {
        val sdf =SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    fun loadBitmap(photoUri:Uri) : Bitmap? {
        try {
            return if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else{
                MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
        }catch(e:Exception){
            e.printStackTrace()
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK) {
            when(requestCode){
                REQ_CAMERA -> {
                    realUri?.let{ uri ->
                        val bitmap = loadBitmap(uri)
                        binding.imageView.setImageBitmap(bitmap)
                        realUri = null

                    }

                }
                REQ_GALLARY -> {
                    data?.data?.let { uri ->
                        binding.imageView.setImageURI(uri)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            99 -> {
                if(grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    openCamera()
                } else{
                    Toast.makeText(this,"권한을 승인하지 않으면 앱이 종료됩니다.",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}