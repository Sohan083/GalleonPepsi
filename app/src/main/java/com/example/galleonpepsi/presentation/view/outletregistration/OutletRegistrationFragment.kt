package com.example.galleonpepsi.presentation.view.outletregistration

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.galleonpepsi.MainActivity
import com.example.galleonpepsi.R
import com.example.galleonpepsi.core.CoreFragment
import com.example.galleonpepsi.core.utils.CustomUtility
import com.example.galleonpepsi.core.utils.GPSLocation
import com.example.galleonpepsi.databinding.FragmentOutletRegistrationBinding
import com.example.galleonpepsi.data.api.OutletsApi
import com.example.galleonpepsi.core.utils.StaticTags
import com.example.galleonpepsi.data.outlet.OutletForSubmitData
import com.example.galleonpepsi.data.outlet.OutletListResponse
import com.example.galleonpepsi.data.outlet.OutletSubmitResponse
import com.example.galleonpepsi.presentation.view.login.viewmodel.LoginViewModel
import com.example.galleonpepsi.presentation.view.outletregistration.response.brand.BrandListResponse
import com.example.galleonpepsi.presentation.view.outletregistration.response.drink.DrinkListResponse


import com.google.gson.Gson
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class OutletRegistrationFragment() : CoreFragment() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: FragmentOutletRegistrationBinding

    var sweetAlertDialog: SweetAlertDialog? = null


    private var gpsLocation: GPSLocation? = null
    lateinit var userViewModel: LoginViewModel

    var operatorList = arrayOf("017", "013", "019", "014", "016", "018", "015")
    var isCorrectPrimaryNumber: Boolean = false

    var retailTypeList =
        arrayListOf<String>("Grocery", "Departmental Store", "Restaurant", "Fast Food Retail", "Other")
    var brandList = arrayListOf<String>()

    var drinkList = arrayListOf<String>()

    var outletType: String = ""

    var isNew = true
    var searchedOutlet: com.example.galleonpepsi.data.outlet.OutletResult? = null
    var isShopFascia = false
    var shopFasciaType1: String = "0"
    var shopFasciaBrand1: String = "0"
    var shopFasciaDrink1: String = "0"

    var shopFasciaType2: String = "0"
    var shopFasciaBrand2: String = "0"
    var shopFasciaDrink2: String = "0"

    var contactPersonPhone: String = ""

    var isDrink1Selected = false
    var isDrink2selected = false

    var brandIdMap = mutableMapOf<Int, String?>()
    var drinkIdMap = mutableMapOf<Int, String?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOutletRegistrationBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("registrationDone", false)) {
            findNavController().navigate(R.id.action_outletRegistrationFragment_to_pictureregistrationFragment)

        }
        userViewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)
        userViewModel.updateSessionData()

        userViewModel.userData.observe(requireActivity()) {
            if (it != null) {
                initialize()
                //Log.d("dsa",userViewModel.userData.value.toString())

            }
        }


    }

    private fun initialize() {


        gpsLocation = GPSLocation(requireContext())
        gpsLocation!!.GPS_Start()
        gpsLocation!!.setLocationChangedListener(object : GPSLocation.LocationChangedListener {
            override fun locationChangeCallback(lat: String?, lon: String?, acc: String?) {
                MainActivity.presentLat = lat
                MainActivity.presentLon = lon
                MainActivity.presentAcc = acc
                binding.gpstext.text = "GPS Accuracy: " + MainActivity.presentAcc
            }

        })

        getBrandList(0)
        getDrinkList(0)
        binding.searchBtn.setOnClickListener {
            if (binding.searchOutlet.text.isNullOrEmpty()) {
                binding.searchOutlet.error = "Kindly specify outlet code"
            } else {
                searchOutlet(binding.searchOutlet.text.toString())
            }
        }
        binding.contactPhoneEdittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                if (!isCorrectPhoneNumber(s.toString())) {
                    binding.contactPhoneEdittext.error = "Number must be correct"
                    isCorrectPrimaryNumber = false
                    //setCallGone()
                } else {
                    isCorrectPrimaryNumber = true
                    contactPersonPhone = s.toString()
                    //checkDuplicate(outletContactNumber!!)
                }

            }
        })

        binding.isShopFasciaRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.shop_fascia_yes_radio_button -> {
                    isShopFascia = true
                    binding.posm1layout.visibility = View.VISIBLE
                    binding.posm2layout.visibility = View.VISIBLE
                }
                R.id.shop_fascia_no_radio_button -> {
                    isShopFascia = false
                    binding.posm1layout.visibility = View.GONE
                    binding.posm2layout.visibility = View.GONE
                }
            }
        }


        binding.shopFasciaType1RadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.shop_fascia_light_radio_button1 -> {
                    shopFasciaType1 = "1"
                    binding.brandLayout1.visibility = View.VISIBLE
                    //getBrandList(1)
                }
                R.id.shop_fascia_non_light_radio_button1 -> {
                    shopFasciaType1 = "0"
                    binding.brandLayout1.visibility = View.VISIBLE

                    //getBrandList(1)
                }
                R.id.shop_fascia_non_radio_button1 -> {
                    shopFasciaType1 = "-1"
                    //getBrandList(2)
                    binding.brandLayout1.visibility = View.GONE
                    shopFasciaBrand1 = "0"
                    shopFasciaDrink1 = "0"
                }
            }
        }

        binding.spinnerOutletTyper.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, retailTypeList)

        binding!!.spinnerOutletTyper.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    outletType = retailTypeList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }

        binding.spinnerBrand1.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, brandList)

        binding!!.spinnerBrand1.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    shopFasciaBrand1 = brandIdMap[position].toString()

                    if (shopFasciaBrand1 == "1") {
                        binding.labelSpecifyDrink1.visibility = View.VISIBLE
                        binding.spinnerDrink1RelativeLayout.visibility = View.VISIBLE
                        //getDrinkList(1)
                    } else {
                        binding.labelSpecifyDrink1.visibility = View.GONE
                        binding.spinnerDrink1RelativeLayout.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }

        binding.shopFasciaType2RadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.shop_fascia_light2_radio_button -> {
                    shopFasciaType2 = "1"
                    binding.brandLayout2.visibility = View.VISIBLE

                    //getBrandList(2)
                }
                R.id.shop_fascia_non_light2_radio_button2 -> {
                    shopFasciaType2 = "0"
                    //getBrandList(2)
                    binding.brandLayout2.visibility = View.VISIBLE

                }
                R.id.shop_fascia_non_radio_button2 -> {
                    shopFasciaType2 = "-1"
                    //getBrandList(2)
                    binding.brandLayout2.visibility = View.GONE
                    shopFasciaBrand2 = "0"
                    shopFasciaDrink2 = "0"
                }
            }
        }

        binding.spinnerBrand2.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, brandList)

        binding!!.spinnerBrand2.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    shopFasciaBrand2 = brandIdMap[position].toString()
                    if (shopFasciaBrand2 == "1") {
                        binding.labelSpecifyDrink2.visibility = View.VISIBLE
                        binding.spinnerDrink2RelativeLayout.visibility = View.VISIBLE

                    } else {
                        binding.labelSpecifyDrink2.visibility = View.GONE
                        binding.spinnerDrink2RelativeLayout.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }

        binding.spinnerDrink1.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, drinkList)

        binding!!.spinnerDrink1.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    shopFasciaDrink1 = drinkIdMap[position].toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }

        binding.spinnerDrink2.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, drinkList)

        binding.spinnerDrink2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                shopFasciaDrink2 = drinkIdMap[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }


        binding.outletTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.new_outlet_radio_button -> {
                    //showNewOutletForm()
                    isNew = true
                    binding.searchLayout.visibility = View.GONE
                    binding.newOutletForm.visibility = View.VISIBLE
                    binding.submitBtn.visibility = View.VISIBLE
                    //resetForm()
                    clearForm(isNew)
                }
                R.id.update_outlet_radio_button -> {
                    isNew = false

                    binding.searchLayout.visibility = View.VISIBLE
                    binding.submitBtn.visibility = View.VISIBLE
                    //resetForm()
                    binding.newOutletForm.visibility = View.GONE
                    clearForm(isNew)
                }
            }
        }
        binding.submitBtn.setOnClickListener {
            // val selectedOutlet = viewModel.selectedOutlet.value
//            if (selectedOutlet != null) {
//
//            }

            if (validateForm()) {
                val s = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                s.titleText = "Are You Sure?"
                s.setCancelButton("No", SweetAlertDialog.OnSweetClickListener {
                    s.dismissWithAnimation()
                })
                s.setConfirmButton("Yes") {
                    s.dismissWithAnimation()

                    upload()
                }
                s.show()

            }
        }



        binding.outletNameEdittext.addTextChangedListener {
            // viewModel.getOutletsList(it.toString())
        }

        /* viewModel.outletsList.observe(viewLifecycleOwner) { outlets ->
             adapter.addAll(outlets)
             adapter.notifyDataSetChanged()
         }

         viewModel.selectedOutlet.observe(viewLifecycleOwner) { outlet ->
             if (outlet == null) {
                 binding.outletNameEdittext.setText("")
                 binding.distributionSpinner.setSelection(0)
                 binding.outletAddressEdittext.setText("")
             }
         }*/
    }

    private fun clearForm(isNew: Boolean) {
        binding.outletNameEdittext.setText("")
        binding.searchOutlet.setText("")
        binding.outletAddressEdittext.setText("")
        binding.contactEdittext.setText("")
        binding.contactPhoneEdittext.setText("")
        binding.remarkEdittext.setText("")
        binding.isShopFasciaRadioGroup.clearCheck()
        binding.shopFasciaType1RadioGroup.clearCheck()
        binding.shopFasciaType2RadioGroup.clearCheck()
        binding.brandLayout1.visibility = View.GONE
        binding.brandLayout2.visibility = View.GONE
        binding.posm1layout.visibility = View.GONE
        binding.posm2layout.visibility = View.GONE
        if(!isNew) binding.newOutletForm.visibility = View.GONE
        else binding.newOutletForm.visibility = View.VISIBLE
        isShopFascia = false
        binding.spinnerBrand1.adapter = ArrayAdapter(
            requireContext(), R.layout.spinner_item, brandList
        )
        binding.spinnerBrand2.adapter = ArrayAdapter(
            requireContext(), R.layout.spinner_item, brandList
        )
        binding.spinnerDrink1.adapter = ArrayAdapter(
            requireContext(), R.layout.spinner_item, drinkList
        )
        binding.spinnerDrink2.adapter = ArrayAdapter(
            requireContext(), R.layout.spinner_item, drinkList
        )
        binding.spinnerOutletTyper.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, retailTypeList)
        shopFasciaBrand1 = "0"
        shopFasciaBrand2 = "0"
        shopFasciaDrink1 = "0"
        shopFasciaDrink2 = "0"
        isCorrectPrimaryNumber = false
        contactPersonPhone = "0"
    }

    private fun upload() {
        val updatedOutletForSubmitData = OutletForSubmitData(
            userViewModel.userData.value!!.user_id!!,
            if (isNew) "" else searchedOutlet!!.id!!,
            binding.outletNameEdittext.text.toString(),
            binding.outletAddressEdittext.text.toString(),
            MainActivity.presentLat.toString(),
            MainActivity.presentLon.toString(),
            MainActivity.presentAcc.toString(),
            binding.remarkEdittext.text.toString(),
            getString(R.string.version),
            outletType,
            binding.contactEdittext.text.toString(),
            binding.contactPhoneEdittext.text.toString(),
            if (isShopFascia) "1" else "0",
            shopFasciaType1,
            shopFasciaBrand1,
            shopFasciaDrink1,
            shopFasciaType2,
            shopFasciaBrand2,
            shopFasciaDrink2,
        )

        val sweetAlertDialog =
            SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.titleText = "Loading"
        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.show()
        val queue = Volley.newRequestQueue(requireContext())
        val url = StaticTags.BASE_URL + "outlet/insert_outlet.php"

        val sr: StringRequest = object : StringRequest(Method.POST, url, Response.Listener {
            sweetAlertDialog.dismiss()
            try {
                Log.d("response:", it)
                val outletSubmitResponse =
                    Gson().fromJson(it, OutletSubmitResponse::class.java)
                if (outletSubmitResponse.success) {
                    val sharedPreferences = requireActivity().getSharedPreferences(
                        "user", AppCompatActivity.MODE_PRIVATE
                    )
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("registrationDone", true)
                    //editor.putString("activityId", jsonObject.getString("activityId"))
                    editor.putString("outletId", outletSubmitResponse.outletId)
                    editor.putString(
                        "outletName", binding.outletNameEdittext.text.toString()
                    )
                    editor.putString(
                        "outletAddress", binding.outletAddressEdittext.text.toString()
                    )
                    editor.apply()
                    findNavController().navigate(R.id.action_outletRegistrationFragment_to_pictureregistrationFragment)
                    //clearForm()
                    //CustomUtility.showSuccess(requireContext(),"","Success")
                } else {
                    CustomUtility.showError(
                        requireContext(), outletSubmitResponse.message, "Failed"
                    )
                }

            } catch (e: JSONException) {
                CustomUtility.showError(requireContext(), e.message, "Failed")
            }

        }, Response.ErrorListener {
            sweetAlertDialog.dismiss()
            CustomUtility.showError(
                requireContext(), "Network problem, try again", "Failed"
            )
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                params["UserId"] = updatedOutletForSubmitData.UserId
                if (!isNew) params["OutletId"] = updatedOutletForSubmitData.OutletId
                //paramrs["OutletType"] = updatedOutletForSubmitData.out
                params["OutletName"] = updatedOutletForSubmitData.OutletName
                params["Address"] = updatedOutletForSubmitData.Address
                params["OutletType"] = updatedOutletForSubmitData.OutletType
                params["LatValue"] = updatedOutletForSubmitData.LatValue
                params["LonValue"] = updatedOutletForSubmitData.LonValue
                params["Accuracy"] = updatedOutletForSubmitData.Accuracy
                params["Remarks"] = updatedOutletForSubmitData.Remarks
                params["AppVersion"] = updatedOutletForSubmitData.AppVersion

                params["ContactPersonName"] = updatedOutletForSubmitData.ContactName
                params["ContactPersonMobile"] = updatedOutletForSubmitData.ContactPersonMobile

                params["HasShopFascia"] = updatedOutletForSubmitData.HasShopFascia

                params["Posm1HasLightBox"] = updatedOutletForSubmitData.Posm1HasLightBox

                if(updatedOutletForSubmitData.Posm1HasLightBox != "-1")
                {
                    if (updatedOutletForSubmitData.Posm1HasLightBox == "1") {
                        params["Posm1BrandId"] = updatedOutletForSubmitData.Posm1BrandId
                        if (updatedOutletForSubmitData.Posm1BrandId == "1") params["Posm1DrinkId"] =
                            updatedOutletForSubmitData.Posm1DrinkId
                    }
                }
                else{
                    params["Posm1BrandId"] = "0"
                    params["Posm1DrinkId"] = "0"
                }


                params["Posm2HasLightBox"] = updatedOutletForSubmitData.Posm2HasLightBox


                if(updatedOutletForSubmitData.Posm2HasLightBox != "-1")
                {
                    if (updatedOutletForSubmitData.Posm2HasLightBox == "1") {
                        params["Posm2BrandId"] = updatedOutletForSubmitData.Posm2BrandId
                        if (updatedOutletForSubmitData.Posm2BrandId == "1")
                            params["Posm2DrinkId"] = updatedOutletForSubmitData.Posm2DrinkId
                    }
                }
                else
                {
                    params["Posm2BrandId"] = "0"
                    params["Posm2DrinkId"] = "0"
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

    private fun getBrandList(i: Int) {
        /*if (brandList.size > 1) {
            if (i == 1) {
                binding.spinnerBrand1.adapter = ArrayAdapter(
                    requireContext(), R.layout.spinner_item, brandList
                )
            } else {
                binding.spinnerBrand2.adapter = ArrayAdapter(
                    requireContext(), R.layout.spinner_item, brandList
                )
            }
            return
        }*/
        if (CustomUtility.haveNetworkConnection(requireContext())) {
            val ss = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
            ss!!.titleText = "Loading"
            ss!!.show()
            ss!!.setCancelable(false)
            val retrofit = Retrofit.Builder().baseUrl(StaticTags.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()

            val service = retrofit.create(OutletsApi::class.java)
            val call = service.getBrandList(userViewModel.userData.value!!.user_id!!)

            call.enqueue(object : Callback<BrandListResponse> {
                override fun onResponse(
                    call: Call<BrandListResponse>?, response: retrofit2.Response<BrandListResponse>?
                ) {
                    ss?.dismiss()
                    Log.d("response", response?.body().toString())
                    if (response != null) {
                        if (response.code() == 200) {
                            val brandListResponse = response.body()!!
                            if (brandListResponse.success) {
                                val itemList = brandListResponse.resultList

                                for ((ind, i) in itemList.withIndex()) {
                                    brandList.add(i.name)
                                    brandIdMap[ind] = i.id
                                }
                                binding.spinnerBrand1.adapter = ArrayAdapter(
                                    requireContext(), R.layout.spinner_item, brandList
                                )
                                binding.spinnerBrand2.adapter = ArrayAdapter(
                                    requireContext(), R.layout.spinner_item, brandList
                                )
                                /*if (i == 1) {
                                    binding.spinnerBrand1.adapter = ArrayAdapter(
                                        requireContext(), R.layout.spinner_item, brandList
                                    )
                                } else {
                                    binding.spinnerBrand2.adapter = ArrayAdapter(
                                        requireContext(), R.layout.spinner_item, brandList
                                    )
                                }*/


                                //setupViewModel()
                            } else {
                                CustomUtility.showError(
                                    requireContext(), brandListResponse.message, "Failed"
                                )
                            }
                        }
                    } else {
                        CustomUtility.showError(
                            requireContext(), "Network Error, Try Again", "Failed"
                        )
                    }
                }

                override fun onFailure(call: Call<BrandListResponse>?, t: Throwable?) {
                    ss?.dismiss()
                    //Log.e("res", error.toString())
                    CustomUtility.showError(
                        requireContext(), "Network Error, try again!", "Failed"
                    )
                }
            })
        } else {
            CustomUtility.showError(
                requireContext(), "Please Check your internet connection", "Network Warning !!!"
            )
        }
    }

    private fun getDrinkList(i: Int) {
    /*    if (drinkList.size > 1) {
            if (i == 1) {
                binding.spinnerDrink1.adapter = ArrayAdapter(
                    requireContext(), R.layout.spinner_item, drinkList
                )
            } else {
                binding.spinnerDrink2.adapter = ArrayAdapter(
                    requireContext(), R.layout.spinner_item, drinkList
                )
            }
            return
        }*/
        if (CustomUtility.haveNetworkConnection(requireContext())) {
            val ss = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
            ss!!.titleText = "Loading"
            ss!!.show()
            ss!!.setCancelable(false)
            val retrofit = Retrofit.Builder().baseUrl(StaticTags.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()

            val service = retrofit.create(OutletsApi::class.java)
            val call = service.getDrinkList(userViewModel.userData.value!!.user_id!!)

            call.enqueue(object : Callback<DrinkListResponse> {
                override fun onResponse(
                    call: Call<DrinkListResponse>?, response: retrofit2.Response<DrinkListResponse>?
                ) {
                    ss?.dismiss()
                    Log.d("response", response?.body().toString())
                    if (response != null) {
                        if (response.code() == 200) {
                            val drinkListResponse = response.body()!!
                            if (drinkListResponse.success) {
                                val itemList = drinkListResponse.resultList

                                for ((ind, i) in itemList.withIndex()) {
                                    if (i.name != null) {
                                        drinkList.add(i.name)
                                        drinkIdMap[ind] = i.id
                                    }

                                }
                                binding.spinnerDrink1.adapter = ArrayAdapter(
                                    requireContext(), R.layout.spinner_item, drinkList
                                )
                                binding.spinnerDrink2.adapter = ArrayAdapter(
                                    requireContext(), R.layout.spinner_item, drinkList
                                )
                             /*   if (i == 1) {
                                    binding.spinnerDrink1.adapter = ArrayAdapter(
                                        requireContext(), R.layout.spinner_item, drinkList
                                    )
                                } else {
                                    binding.spinnerDrink2.adapter = ArrayAdapter(
                                        requireContext(), R.layout.spinner_item, drinkList
                                    )
                                }*/


                                //setupViewModel()
                            } else {
                                CustomUtility.showError(
                                    requireContext(), drinkListResponse.message, "Failed"
                                )
                            }
                        }
                    } else {
                        CustomUtility.showError(
                            requireContext(), "Network Error, Try Again", "Failed"
                        )
                    }
                }

                override fun onFailure(call: Call<DrinkListResponse>?, t: Throwable?) {
                    ss?.dismiss()
                    //Log.e("res", error.toString())
                    CustomUtility.showError(
                        requireContext(), "Network Error, try again!", "Failed"
                    )
                }
            })
        } else {
            CustomUtility.showError(
                requireContext(), "Please Check your internet connection", "Network Warning !!!"
            )
        }
    }

    private fun searchOutlet(code: String) {
        val s = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        s!!.titleText = "Loading"
        s!!.show()
        s!!.setCancelable(false)

        val retrofit = Retrofit.Builder().baseUrl(StaticTags.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()

        val service = retrofit.create(OutletsApi::class.java)
        val call = service.getOutlet(userViewModel.userData.value!!.user_id!!, code)

        call.enqueue(object : Callback<OutletListResponse> {
            override fun onResponse(
                call: Call<OutletListResponse>?, response: retrofit2.Response<OutletListResponse>?
            ) {
                s?.dismiss()
                Log.d("response", response?.body().toString())
                if (response != null) {
                    if (response.code() == 200) {
                        val outletListResponse = response.body()!!


                        if (outletListResponse.success) {
                            val outletList = outletListResponse.outletResult
                            searchedOutlet = outletList[0]
                            showNewOutletForm()
                            updateForm()
                        } else {
                            CustomUtility.showError(
                                requireContext(), outletListResponse.message, "Failed"
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<OutletListResponse>?, t: Throwable?) {
                s?.dismiss()
                Log.e("res", t.toString())
                CustomUtility.showError(
                    requireContext(), "Network Error, try again!", "Failed"
                )
            }
        })

    }

    private fun updateForm() {

        binding.outletNameEdittext.setText(searchedOutlet!!.name)
        if(searchedOutlet!!.address != null)
        binding.outletAddressEdittext.setText(searchedOutlet!!.address)
        if(searchedOutlet!!.contact_person_name!= null) binding.contactEdittext.setText(searchedOutlet!!.contact_person_name)

        if(searchedOutlet!!.contact_person_mobile != null)
        {
            binding.contactPhoneEdittext.setText(searchedOutlet!!.contact_person_mobile)
            if (!isCorrectPhoneNumber(searchedOutlet!!.contact_person_mobile.toString())) {
                binding.contactPhoneEdittext.error = "Number must be correct"
                isCorrectPrimaryNumber = false
                //setCallGone()
            } else {
                isCorrectPrimaryNumber = true
                contactPersonPhone = searchedOutlet!!.contact_person_mobile.toString()
                //checkDuplicate(outletContactNumber!!)
            }
        }
        if(searchedOutlet!!.retail_type!= null) {
            val i = retailTypeList.indexOf(searchedOutlet!!.retail_type!!)
            if(i > -1)
            binding.spinnerOutletTyper.setSelection(retailTypeList.indexOf(searchedOutlet!!.retail_type!!))
        }
        if(searchedOutlet!!.has_shop_fascia!= null && searchedOutlet!!.has_shop_fascia != "-1")
        {
            if(searchedOutlet!!.has_shop_fascia!! == "1")
            {
                binding.shopFasciaYesRadioButton.isChecked = true
            }
            else {
                binding.shopFasciaNoRadioButton.isChecked = true
            }
        }
        if(searchedOutlet!!.posm1_has_light_box!= null && searchedOutlet!!.posm1_has_light_box != "-1")
        {

            if(searchedOutlet!!.posm1_has_light_box!! == "1")
            {
                binding.shopFasciaLightRadioButton1.isChecked = true
            }
            else {
                binding.shopFasciaNonLightRadioButton1.performClick()
            }

            if(searchedOutlet!!.posm1_brand_id!= null) {
                var ind = getKey(brandIdMap, searchedOutlet!!.posm1_brand_id!!)
                if(ind!=null)
                    binding.spinnerBrand1.setSelection(ind)

                if(searchedOutlet!!.posm1_brand_id == "1")
                {
                    if(searchedOutlet!!.posm1_drink_id != null){
                        ind = getKey(drinkIdMap, searchedOutlet!!.posm1_drink_id)
                        if(ind != null)
                            binding.spinnerDrink1.setSelection(ind)
                    }

                }
            }
        }



        if(searchedOutlet!!.posm2_has_light_box!= null && searchedOutlet!!.posm2_has_light_box != "-1")
        {
            if(searchedOutlet!!.posm2_has_light_box!! == "1")
            {
                binding.shopFasciaLight2RadioButton.isChecked = true
            }
            else {
                binding.shopFasciaNonLight2RadioButton2.isChecked = true
            }
            if(searchedOutlet!!.posm2_brand_id!= null) {
                var ind = getKey(brandIdMap, searchedOutlet!!.posm2_brand_id!!)
                if(ind!=null)
                    binding.spinnerBrand2.setSelection(ind)

                if(searchedOutlet!!.posm2_brand_id == "1")
                {

                    if(searchedOutlet!!.posm2_drink_id != null){
                        ind = getKey(drinkIdMap, searchedOutlet!!.posm2_drink_id)
                        if(ind != null)
                            binding.spinnerDrink2.setSelection(ind)
                    }

                }
            }

        }


        binding.remarkEdittext.setText(searchedOutlet!!.remarks)

    }

    private fun validateForm(): Boolean {

        var isValid = true
        if (!isNew && searchedOutlet == null) {
            CustomUtility.showError(
                requireContext(), "You cannot update it. Please search first", "Required"
            )
            return false
        }
        if (binding.outletNameLabel.text.isNullOrEmpty()) {
            binding.outletNameLabel.error = "Outlet name is required"
            return false
        }


        if (binding.outletAddressEdittext.text.isNullOrEmpty()) {
            binding.outletAddressEdittext.error = "Outlet address is required"
            return false
        }

        if (binding.contactEdittext.text.isNullOrEmpty()) {
            binding.contactEdittext.error = "Contact person name is required"
            return false
        }

        if (binding.contactPhoneEdittext.text.isNullOrEmpty()) {
            binding.contactPhoneEdittext.error = "Contact person number is required"
            return false
        }
        if (!isCorrectPrimaryNumber) {
            binding.contactPhoneEdittext.error = "Contact's phone number must be correct"
            return false
        }
        if (binding.isShopFasciaRadioGroup.checkedRadioButtonId == -1) {
            CustomUtility.showError(
                requireContext(), "Please select is there any Shop Fascia?", "Required"
            )
            return false
        }
        if(MainActivity.presentAcc.isNullOrEmpty())
        {
            CustomUtility.showError(
                requireContext(), "Please wait for the GPS?", "Required"
            )
            return false
        }
        return isValid
    }

    private fun showNewOutletForm() {
        //binding.outletSearchAutocompleteTextview.visibility = View.GONE
        binding.formLayout.visibility = View.VISIBLE
        binding.newOutletForm.visibility = View.VISIBLE

        crossfadeVisible(binding.formLayout)

    }


    private fun crossfadeVisible(view: View) {

        view.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 1000
            start()
        }
    }




    private fun resetForm() {
        isNew = true
        binding.outletNameEdittext.setText("")
        binding.outletAddressEdittext.setText("")
        binding.remarkEdittext.setText("")
        binding.searchOutlet.setText("")
        binding.submitBtn.visibility = View.GONE
        //binding.outletInfoLayout.visibility = View.GONE
        binding.formLayout.visibility = View.GONE
        binding.searchLayout.visibility = View.GONE
        binding.newOutletForm.visibility = View.GONE
        binding.isShopFasciaRadioGroup.clearCheck()
        //binding.posterImage.visibility = View.VISIBLE
        binding.outletTypeRadioGroup.clearCheck()
        searchedOutlet = null
        //newOutletDistributionSpinner.setSelection(0)
        //selectedOutletNameTv.text = ""
        //selectedOutletAddressTv.text = ""
        //selectedOutletDistributionTv.text = ""
        //outletSearchAutoComplete.setText("")
    }



    private fun <K, V> getKey(map: Map<K, V>, target: V): K? {
        for ((key, value) in map) {
            if (target == value) {
                return key
            }
        }
        return null
    }

    private fun isCorrectPhoneNumber(phone: String): Boolean {
        if ((phone == "") || (phone.length != 11)) {
            return false
        }
        val code2 = phone.substring(0, 3)
        for (op: String in operatorList) {
            if ((op == code2)) {
                return true
            }
        }
        return false
    }


    override fun onStop() {
        super.onStop()
        //requireActivity().viewModelStore.clear()
    }
}