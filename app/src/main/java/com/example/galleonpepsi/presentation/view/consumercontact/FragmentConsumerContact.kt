package com.example.galleonpepsi.presentation.view.consumercontact

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
import com.example.galleonpepsi.data.outlet.OutletResult
import com.example.galleonpepsi.databinding.FragmentConsumerContactBinding
import com.example.galleonpepsi.presentation.view.posmactivity.PosmActivityApiInterface
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class FragmentConsumerContact() : Fragment() {
    var sharedPreferences: SharedPreferences? = null
    lateinit var binding: FragmentConsumerContactBinding

    var outletDetails: OutletResult? = null

    var operatorList = arrayOf("017", "013", "019", "014", "016", "018", "015")
    var isCorrectPrimaryNumber: Boolean = false

    var educationTypeList =
        arrayListOf<String>("Below Primary", "Above primary - below SSC", "SSC/ HSC/ Equivalent Pass", "Bachelor - National University", "Bachelor - Public / Private University",
        "Technical Graduate (MBBS/Engineering)", "Masters or above"
            )
    var educationType = ""

    var occupationTypeList =
        arrayListOf<String>("Private Service", "Govt. Service", "Business", "Technical Profession", "Student", "Homemaker", "Looking for job")
    var occupationType = ""

    var outletType: String = ""

    var sex = ""

    var hasMessageDelivered = ""
    var trialStatus = ""
    var isPtr1Litre = false
    var isPtr2Litre = false

    var ptr1LitreCount = "0"
    var ptr2LitreCount = "0"

    var hasWatchAv = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConsumerContactBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val s: SharedPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val v = s.getString("outletDetails", null)

        if(v != null)
        {
            outletDetails = Gson().fromJson(v, OutletResult::class.java)
            binding.outletName.text = outletDetails!!.name
            //binding.outletId.text = "Outlet Id: "+ outletDetails!!.id
            if(outletDetails!!.address != null)
            binding.outletAddress.text = outletDetails!!.address
        }
        else
        {
            CustomUtility.showError(requireContext(),"Please select an retail from the previous page","Retail Missing")
        }
        initialize()

    }


    private fun initialize() {



        binding.submitbtn.setOnClickListener{
           /* if(checkFields())
            {
                val s  = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                s.titleText = "Are you sure?"
                s.setConfirmButton("Yes", SweetAlertDialog.OnSweetClickListener {
                    s.dismissWithAnimation()
                    upload()
                })
                s.cancelText = "No"
                s.show()
            }*/
        }


    }


/*
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
*/


/*
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
         */
/*   else if(binding.bannerQuantity.text.toString() == "" &&
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
            }*//*

            return true
        }

        else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            return false
        }

    }
*/


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