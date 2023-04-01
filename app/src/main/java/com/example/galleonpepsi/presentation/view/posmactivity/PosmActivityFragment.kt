package com.example.galleonpepsi.ui.posmactivity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.galleonpepsi.MainActivity
import com.example.galleonpepsi.MainActivity.Companion.presentAcc
import com.example.galleonpepsi.MainActivity.Companion.presentLat
import com.example.galleonpepsi.MainActivity.Companion.presentLon
import com.example.galleonpepsi.R
import com.example.galleonpepsi.databinding.FragmentPosmActivityBinding
import com.example.galleonpepsi.data.User
import com.example.galleonpepsi.data.User.Companion.user
import com.example.galleonpepsi.ui.posmactivity.responses.outletlistapiresponse.OutletListResponseBody
import com.example.galleonpepsi.ui.posmactivity.responses.outletlistapiresponse.Retail

import com.example.galleonpepsi.core.utils.CustomUtility
import com.example.galleonpepsi.core.utils.StaticTags
import com.example.galleonpepsi.presentation.view.posmactivity.PosmActivityApiInterface
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PosmActivityFragment() : Fragment() {
    var sharedPreferences: SharedPreferences? = null
    lateinit var binding: FragmentPosmActivityBinding


    lateinit var sweetAlertDialog: SweetAlertDialog
    var fullList: java.util.ArrayList<Retail> = java.util.ArrayList()
    var outletList = java.util.ArrayList<Retail>()
    val outletStatusList = arrayOf("Matched", "Not Matched", "Not Found")
    var outletStatusId = 1

    var currentOutlet: Retail? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPosmActivityBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val sharedPreferences =
            requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("formDone", false)) {
            disableAllMenuItem()
            findNavController().navigate(R.id.action_posmActivityFragment_to_pictureFragment)
        } else {

            initialize()
        }

    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences =
            requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("formDone", false)) {
            disableAllMenuItem()
//            findNavController().navigate(R.id.action_posmActivityFragment_to_pictureFragment)
        }
    }

    private fun disableAllMenuItem() {
        MainActivity.menuNav?.findItem(R.id.attendanceFragment)?.isEnabled = false
        MainActivity.menuNav?.findItem(R.id.logoutFragment)?.isEnabled = false
    }

    private fun initialize() {

        checkForUpdate()

        binding.searchOutlet.setText("")

        user!!.setValuesFromSharedPreference(
            requireActivity().getSharedPreferences(
                "user",
                AppCompatActivity.MODE_PRIVATE
            )
        )

        getOutletList()

        binding.spinnerRetailStatusType.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, outletStatusList)
        binding.spinnerRetailStatusType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                outletStatusId = position+1
                if(outletStatusId == 2){
                    binding.retailNameNew.visibility = View.VISIBLE
                }
                else
                {
                    binding.retailNameNew.setText("")
                    binding.retailNameNew.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }


        binding.searchOutlet.setOnItemClickListener { parent, view, position, id ->
            currentOutlet = binding.searchOutlet.adapter.getItem(position) as Retail?

            if(currentOutlet != null)
            {
                binding.posterImage.visibility = View.GONE
                binding.outletName.text = currentOutlet!!.RETAIL_NAME + " (" +currentOutlet!!.RETAIL_CODE+")"
                //binding.outletId.text = "Retail Code: "+currentOutlet!!.RETAIL_CODE
                binding.outletAddress.text = currentOutlet!!.AreaName

                crossfadeVisible(binding.outletInfoLayout)
                crossfadeVisible(binding.activityInputLayout)
                crossfadeVisible(binding.submitbtn)
                crossfadeVisible(binding.outletChangeLayout)

                getOutletExecutionHistory()
            }

        }

        binding.submitbtn.setOnClickListener{
            if(checkFields())
            {
                val s  = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                s.titleText = "Are you sure?"
                s.setConfirmButton("Yes", SweetAlertDialog.OnSweetClickListener {
                    s.dismissWithAnimation()
                    upload()
                })
                s.cancelText = "No"
                s.show()
            }
        }


    }

    private fun getOutletExecutionHistory() {
        val sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.titleText = "Loading"
        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.show()
        val queue = Volley.newRequestQueue(requireContext())
        val url = StaticTags.BASE_URL + "Android/get_retail_status.php"

        val sr: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                sweetAlertDialog.dismiss()
                try {
                    Log.d("response:", it)
                    val jsonObject = JSONObject(it)
                    if (jsonObject.getBoolean("success")) {
                        val ja = jsonObject.getJSONArray("retailResult")
                        val jo = ja.getJSONObject(0)
                        binding.outletStatus.text = "Execution Status: Done (" + jo.getString("ActivityCount")+")"

                    } else {
                        binding.outletStatus.text = "Execution Status: Not Done"
                    }
                } catch (e: JSONException) {
                    CustomUtility.showError(requireContext(), e.message, "Failed")
                }

            },
            Response.ErrorListener {
                sweetAlertDialog.dismiss()
                CustomUtility.showError(requireContext(), "Network problem, try again", "Failed")
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["RetailId"] = currentOutlet!!.RETAIL_CODE
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

    private fun upload() {

        val sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.titleText = "Loading"
        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.show()
        val queue = Volley.newRequestQueue(requireContext())
        val url = StaticTags.BASE_URL + "Android/insert_activity_posm.php"

        val sr: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                sweetAlertDialog.dismiss()
                try {
                    Log.d("response:", it)
                    val jsonObject = JSONObject(it)
                    if (jsonObject.getBoolean("success")) {
                        val sharedPreferences = requireActivity().getSharedPreferences(
                            "user",
                            AppCompatActivity.MODE_PRIVATE
                        )
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("formDone", true)
                        editor.putString("activityId", jsonObject.getString("activityId"))
                        editor.putString("outletId",currentOutlet!!.RETAIL_CODE)
                        editor.putString("outletName", binding.outletName.text.toString())
                        editor.putString("outletAddress", binding.outletAddress.text.toString())
                        editor.apply()

                        outletList.clear()
                        fullList.clear()


                        findNavController().navigate(R.id.action_posmActivityFragment_to_pictureFragment)

                    } else {
                        CustomUtility.showError(
                            requireContext(),
                            "Failed!",
                            jsonObject.getString("message")
                        )
                    }
                } catch (e: JSONException) {
                    CustomUtility.showError(requireContext(), e.message, "Failed")
                }

            },
            Response.ErrorListener {
                sweetAlertDialog.dismiss()
                CustomUtility.showError(requireContext(), "Network problem, try again", "Failed")
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["RetailCode"] = currentOutlet!!.RETAIL_CODE
                params["AppVersion"] = getString(R.string.version)
                params["UserId"] = user!!.userId!!

                params["ActivityLat"] = presentLat!!
                params["ActivityLon"] = presentLon!!
                params["ActivityAccuracy"] = presentAcc!!


                params["Poster"] = binding.posterQuantity.text.toString()
                params["Bunting"] = binding.buntingQuantity.text.toString()
                params["Danglar"] = binding.danglarQuantity.text.toString()
                params["Banner"] = binding.bannerQuantity.text.toString()

                params["MatchStatusId"] = outletStatusId.toString()
                if(outletStatusId == 2){
                    params["RetailNameNew"] = binding.retailNameNew.text.toString()
                }


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


    private fun checkFields(): Boolean {
        if (CustomUtility.haveNetworkConnection(requireContext())) {
            if (!CustomUtility.haveNetworkConnection(requireContext())) {
                Toast.makeText(requireContext(), "No internet connection!", Toast.LENGTH_SHORT)
                    .show()
                return false
            }
            else if(currentOutlet == null)
            {
                Toast.makeText(requireContext(), "Please select a retail first", Toast.LENGTH_SHORT).show()
                return false
            }
            else if(binding.bannerQuantity.text.toString() == "" &&
                binding.buntingQuantity.text.toString() == ""&&
                binding.danglarQuantity.text.toString() == ""&&
                binding.posterQuantity.text.toString() == "")
            {
                Toast.makeText(requireContext(), "No items", Toast.LENGTH_SHORT).show()
                return false
            }
            else if(outletStatusId == 2 && binding.retailNameNew.text.toString() == "")
            {
                Toast.makeText(requireContext(), "Please enter new name", Toast.LENGTH_SHORT).show()
                binding.retailNameNew.error = "Please enter new name"
            }
            return true
        }

        else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            return false
        }

    }


    private fun getOutletList()
    {
        outletList.clear()
        fullList.clear()
        if (CustomUtility.haveNetworkConnection(requireContext())) {
            sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
            sweetAlertDialog.titleText = "Loading"
            sweetAlertDialog.show()
            sweetAlertDialog.setCancelable(false)
            val retrofit = Retrofit.Builder()
                .baseUrl(StaticTags.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(PosmActivityApiInterface::class.java)
            val call = service.getOutletList(User.user!!.employeeId!!)

            call.enqueue(object : Callback<OutletListResponseBody> {
                override fun onResponse(
                    call: Call<OutletListResponseBody>?,
                    response: retrofit2.Response<OutletListResponseBody>?
                ) {
                    sweetAlertDialog.dismiss()
                    Log.d("response", response?.body().toString())
                    if (response != null) {
                        if (response.code() == 200) {
                            val outletListResponseBody = response.body()!!
                            if (outletListResponseBody.success) {
                                outletList.addAll(outletListResponseBody.retailList)
                                fullList.addAll(outletList)
                                val adapter = AutoCompleteAdapter(
                                    requireContext(),
                                    R.layout.fragment_posm_activity,
                                    R.id.autocomplete_item_place_label,
                                    fullList
                                )
                                binding.searchOutlet.setAdapter(adapter)

                            }
                            else {
                                val s = SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                                s.titleText = "Failed"
                                s.contentText = outletListResponseBody.message
                                s.setConfirmButton("Ok", SweetAlertDialog.OnSweetClickListener {
                                    s.dismissWithAnimation()
                                })
                                s.setCancelable(false)
                                s.show()
                                Log.d("null","session expired")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<OutletListResponseBody>?, t: Throwable?) {
                    sweetAlertDialog?.dismiss()
                    //Log.e("res", error.toString())
                    CustomUtility.showError(
                        requireContext(),
                        "Network Error, try again!",
                        "Failed"
                    )
                }
            })
        }
        else {
            CustomUtility.showError(
                requireContext(),
                "Please Check your internet connection",
                "Network Warning !!!"
            )
        }
    }


    private fun checkForUpdate() {

        val queue = Volley.newRequestQueue(requireContext())
        val url = StaticTags.BASE_URL + "api/app_version/version_check.php "

        val sr: StringRequest = object : StringRequest(Method.POST, url,
            Response.Listener {
                Log.d("response:", it)
                val jsonObject = JSONObject(it)
                if (jsonObject.getBoolean("success")) {
                    val s = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    s.titleText = "New Update"
                    s.contentText = "Please update the \n application"
                    s.setCancelable(false)
                    s.setConfirmButton("Ok", SweetAlertDialog.OnSweetClickListener {
                        val editor = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE).edit()
                        user?.clear(editor)
                        s.dismissWithAnimation()
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                jsonObject.getJSONObject("appInfo").getString("app_location_url")
                            )
                        )
                        startActivity(browserIntent)
                    })
                    s.show()

                } else {
                    //CustomUtility.showError(requireContext(), "Failed!", jsonObject.getString("message"))
                }

            },
            Response.ErrorListener {
                CustomUtility.showError(requireContext(), "Network problem, try again", "Failed")
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["UserId"] = user!!.userId!!
                params["CurrentVersion"] = getString(R.string.version)
                params["AppName"] = "bp"
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

    inner class AutoCompleteAdapter(
        context: Context?, resource: Int,
        textViewResourceId: Int, fullList: java.util.ArrayList<Retail>
    ) : ArrayAdapter<Retail>(context!!, resource, textViewResourceId, fullList),
        Filterable {
        private var fullList: java.util.ArrayList<Retail>
        private var mOriginalValues: java.util.ArrayList<Retail>?
        private var mFilter: ArrayFilter? = null
        override fun getCount(): Int {
            return fullList.size
        }

        override fun getItem(position: Int): Retail {
            return fullList[position]
        }



        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (convertView == null) {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(
                    R.layout.autocomplete_row_layout,
                    parent,
                    false
                )
            }
            val placeLabel: TextView = view!!.findViewById(R.id.autocomplete_item_place_label)
            val rowLayout: LinearLayout = view!!.findViewById(R.id.row_layout)
            val retail: Retail = getItem(position)
            placeLabel.text = retail.RETAIL_NAME + "(${retail.RETAIL_CODE})"

            return view
        }

        override fun getFilter(): Filter {
            if (mFilter == null) {
                mFilter = ArrayFilter()
            }
            return mFilter as ArrayFilter
        }

        private open inner class ArrayFilter : Filter() {
            private val lock: Any? = null
            protected override fun performFiltering(prefix: CharSequence?): FilterResults {
                val results = FilterResults()
                if (mOriginalValues == null) {
                    synchronized(lock!!) { mOriginalValues = ArrayList<Retail>(fullList) }
                }
                if (prefix == null || prefix.length == 0) {
                    synchronized(lock!!) {
                        val list: java.util.ArrayList<Retail> = ArrayList<Retail>(
                            mOriginalValues
                        )
                        results.values = list
                        results.count = list.size
                    }
                } else {
                    val prefixString = prefix.toString().toLowerCase()
                    val values: java.util.ArrayList<Retail>? = mOriginalValues
                    val count: Int = values!!.size
                    val newValues: java.util.ArrayList<Retail> = ArrayList<Retail>(count)
                    for (i in 0 until count) {
                        val item: Retail = values[i]
                        if (item.RETAIL_NAME.toLowerCase().contains(prefixString) ||
                            item.RETAIL_CODE.toLowerCase().contains(prefixString)) {
                            newValues.add(item)
                        }
                    }
                    results.values = newValues
                    results.count = newValues.size
                }
                return results
            }

            protected override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                if (results.values != null) {
                    fullList = results.values as java.util.ArrayList<Retail>
                } else {
                    fullList = java.util.ArrayList<Retail>()
                }
                /*if (results.count > 0) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }*/notifyDataSetChanged()
                clear()
                var i = 0
                val l: Int = fullList.size
                while (i < l) {
                    add(fullList[i])
                    i++
                }
                notifyDataSetInvalidated()
            }
        }

        init {
            this.fullList = fullList
            mOriginalValues = ArrayList<Retail>(fullList)
        }
    }


    private fun crossfadeVisible(view: View) {

        view.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 1000
            start()
        }
    }


    private fun deleteAppData() {
        try {
            // clearing app data
            val packageName = requireActivity().applicationContext.packageName
            val runtime = Runtime.getRuntime()
            runtime.exec("pm clear $packageName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        //requireActivity().viewModelStore.clear()
    }
}