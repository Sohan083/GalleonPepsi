package com.example.galleonpepsi.presentation.view.outletregistration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings.Global.getString
import android.provider.Settings.Secure.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.galleonpepsi.MainActivity
import com.example.galleonpepsi.R
import com.example.galleonpepsi.core.utils.CustomUtility
import com.example.galleonpepsi.core.utils.StaticTags
import com.example.galleonpepsi.data.User
import com.example.galleonpepsi.databinding.FragmentOutletUpdatePictureBinding
import com.example.galleonpepsi.presentation.view.login.viewmodel.LoginViewModel
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FragmentRegistrationPicture: Fragment() {
    private var binding: FragmentOutletUpdatePictureBinding? = null
    var sweetAlertDialog: SweetAlertDialog? = null
    var imageString: String? = null; var imageType: String? = null
    var retailImageDone = false
    var outletId:String? = null
    lateinit var userViewModel: LoginViewModel

    var currentPath: String? = null
    var sharedPreferences: SharedPreferences? = null

    //private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(savedInstanceState != null)
        {
            currentPath = savedInstanceState.getString("currentPath", null)
        }
        binding = FragmentOutletUpdatePictureBinding.inflate(inflater, container, false)
        return  binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        userViewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)
        userViewModel.updateSessionData()

        userViewModel.userData.observe(requireActivity()) {
            if(it!= null)
            {
                initialize()
                //Log.d("dsa",userViewModel.userData.value.toString())

            }
        }



    }

    private fun initialize()
    {
        disableAllMenuItem()

        val sharedPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        retailImageDone = sharedPreferences.getBoolean("registrationImageDone", false)

        if(retailImageDone)
        {
            binding!!.fullRetailImageBtn.setBackgroundResource(R.drawable.ic_camera_done)
            binding!!.fullRetailImageUploadBtn.setBackgroundResource(R.drawable.ic_upload_done)
        }

        outletId = sharedPreferences.getString("outletId",null)
        binding!!.outletName.text = sharedPreferences.getString("outletName","")
        binding!!.outletId.text = "Outlet ID: "+sharedPreferences.getString("outletId","N/a")
        binding!!.outletAddress.text = sharedPreferences.getString("outletAddress","")


        //getImageStatus()

        binding!!.fullRetailImageBtn.setOnClickListener {
            dispatchTakePictureIntent(StaticTags.RETAIL_IMAGE_CAPTURE_CODE)

        }


        binding!!.endBtn.setOnClickListener{


            if(checkFields())
            {


                val confirm = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                confirm.setTitle("Are you sure?")
                confirm.setConfirmButton("Yes") {
                    confirm.dismissWithAnimation()
                    val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("registrationDone",false)
                    editor.putBoolean("registrationImageDone",false)

                    editor.apply()
                    requireActivity().finish()
                    requireActivity().startActivity(requireActivity().intent)

                }
                confirm.setCancelButton("No") { confirm.dismissWithAnimation() }
                confirm.show()

            }

        }
    }

    private fun checkFields(): Boolean {

        if(!retailImageDone) {
            CustomUtility.showWarning(requireContext(), "Retail image missing","Mandatory Image")
            return false
        }

        return true
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPath = absolutePath
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentPath", currentPath)
    }

    private fun dispatchTakePictureIntent(code: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    //...
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.galleonpepsi.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, code)
                }
            }
        }
    }





    //after finishing camera intent whether the picture was save or not
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == StaticTags.EXECUTION_IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {

            if(currentPath == null)
            {
                Toast.makeText(requireContext(),"Taking image failed. Try again!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val file = File(currentPath)
                if (file.exists())
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, Uri.fromFile(file))
                    imageString = CustomUtility.imageToString(bitmap)
                    binding!!.fullRetailImageBtn.setBackgroundResource(R.drawable.ic_camera_done)
                    uploadPicture(outletId!! , imageString!!, StaticTags.EXECUTION_IMAGE_CAPTURE_CODE, "Android/insert_activity_posm_execution_image.php")
                }
            }

        }
        else if (requestCode == StaticTags.RETAIL_IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            if(currentPath == null)
            {
                Toast.makeText(requireContext(),"Taking image failed. Try again!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val file = File(currentPath)
                if (file.exists())
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, Uri.fromFile(file))
                    imageString = CustomUtility.imageToString(bitmap)
                    binding!!.fullRetailImageBtn.setBackgroundResource(R.drawable.ic_camera_done)
                    uploadPicture(outletId!! , imageString!!, StaticTags.RETAIL_IMAGE_CAPTURE_CODE, "outlet/insert_outlet_picture.php")
                }
            }


        }


    }

    private fun uploadPicture(s: String, s1: String, code: Int, url_last: String ) {


        val sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.titleText = "Loading"
        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.show()
        val queue = Volley.newRequestQueue(requireContext())
        val url = StaticTags.BASE_URL + url_last

        val sr: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                sweetAlertDialog.dismiss()
                Log.d("response:", it)
                val jsonObject = JSONObject(it)
                if(jsonObject.getBoolean("success"))
                {
                    val ss = SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                    ss.titleText = "Success"
                    ss.setCancelable(false)
                    ss.setConfirmButton("Ok") {
                        ss.dismiss()
                    }
                    ss.show()
                    val sharedPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    when (code) {

                        StaticTags.RETAIL_IMAGE_CAPTURE_CODE -> {
                            binding!!.fullRetailImageUploadBtn.setBackgroundResource(R.drawable.ic_upload_done)
                            editor.putBoolean("registrationImageDone", true)
                            retailImageDone = true
                        }
                    }
                    editor.apply()
                }

                else
                {
                    CustomUtility.showError(requireContext(), "Failed, try again", jsonObject.getString("message"))
                }

            },
            Response.ErrorListener {
                sweetAlertDialog.dismiss()
                CustomUtility.showError(requireContext(), it.message.toString(), "Failed")
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["UserId"] = userViewModel.userData.value!!.user_id!!
                params["ImageData"] = imageString!!
                params["OutletId"] = outletId!!
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/x-www-form-urlencoded"
                return params
            }
        }
        queue.add(sr)

    }


    private fun disableAllMenuItem() {
        MainActivity.menuNav?.findItem(R.id.attendanceFragment)?.isEnabled = false

    }
}