package com.example.galleonpepsi.presentation.view.consumercontact

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.galleonpepsi.R
import com.example.galleonpepsi.core.utils.CustomUtility
import com.example.galleonpepsi.core.utils.StaticTags
import com.example.galleonpepsi.data.api.OutletsApi
import com.example.galleonpepsi.data.outlet.OutletListResponse
import com.example.galleonpepsi.data.outlet.OutletResult
import com.example.galleonpepsi.databinding.FragmentOutletListBinding
import com.example.galleonpepsi.presentation.view.login.viewmodel.LoginViewModel
import com.google.gson.Gson

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class OutletListFragment: Fragment() {

    lateinit var binding: FragmentOutletListBinding
    var recyclerView: RecyclerView? = null
    lateinit var mAdapter: OutleListDataAdapter
    private val dataList: java.util.ArrayList<OutletResult> = java.util.ArrayList<OutletResult>()
    private val mainOutletList: java.util.ArrayList<OutletResult> = java.util.ArrayList<OutletResult>()
    private val newOutletList: java.util.ArrayList<OutletResult> = java.util.ArrayList<OutletResult>()
    private val hookedOutletList: java.util.ArrayList<OutletResult> = java.util.ArrayList<OutletResult>()
    private val customerOutletList: java.util.ArrayList<OutletResult> = java.util.ArrayList<OutletResult>()

    var hookedOutletChecked = true
    var customerOutletChecked = true

    var bundle = Bundle()

    var sweetAlertDialog: SweetAlertDialog? = null
    lateinit var userViewModel: LoginViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOutletListBinding.inflate(inflater,container, false)
        userViewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.searchOutlet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                getNewSearchedList(charSequence)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        dataList.clear()
        recyclerView = binding.outletListRecycler
        mAdapter = OutleListDataAdapter(dataList, requireContext())
        recyclerView!!.setItemViewCacheSize(10)
        recyclerView!!.isDrawingCacheEnabled = true
        recyclerView!!.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(requireActivity().applicationContext, 1)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = mAdapter

        getOutletList()

    }


        private fun getNewSearchedList(s: CharSequence) {
            val newList: ArrayList<OutletResult> = ArrayList<OutletResult>()
            if (s == "") {
                dataList.clear()
                dataList.addAll(mainOutletList)
            } else {
                for (i in mainOutletList.indices) {
                    if (mainOutletList[i].name!!.lowercase(Locale.getDefault())
                            .contains(s.toString().lowercase(Locale.getDefault()))
                        || mainOutletList[i].id!!.lowercase(Locale.getDefault())
                            .contains(s.toString().lowercase(Locale.getDefault()))
                        || mainOutletList[i].address!!.lowercase(Locale.getDefault())
                            .contains(s.toString().lowercase(Locale.getDefault()))
//                        || mainOutletList[i].area_name.lowercase(Locale.getDefault())
//                            .contains(s.toString().lowercase(Locale.getDefault()))
//                        || mainOutletList[i].zone_name.lowercase(Locale.getDefault())
//                            .contains(s.toString().lowercase(Locale.getDefault()))
                    ) {
                        newList.add(mainOutletList[i])
                    }
                }
            }
            dataList.clear()
            dataList.addAll(newList)
            mAdapter.notifyDataSetChanged()
        }

    private fun getOutletList() {
        dataList.clear()
        mainOutletList.clear()

        if (CustomUtility.haveNetworkConnection(requireContext())) {
            val s = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
            s!!.titleText = "Loading"
            s!!.show()
            s!!.setCancelable(false)
            val retrofit = Retrofit.Builder()
                .baseUrl(StaticTags.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(OutletsApi::class.java)
            val call = service.getOutletList(userViewModel.userData.value!!.user_id!!, userViewModel.userData.value!!.user_id!!)

            call.enqueue(object : Callback<OutletListResponse> {
                override fun onResponse(
                    call: Call<OutletListResponse>?,
                    response: retrofit2.Response<OutletListResponse>?
                ) {
                    s!!.dismiss()
                    Log.d("response", response?.body().toString())
                    if (response != null) {
                        if (response.code() == 200) {
                            val outletResponseBody = response.body()!!
                            if (outletResponseBody.success) {
                                dataList.clear()
                                mainOutletList.clear()
                                val itemList = outletResponseBody.outletResult
                                mainOutletList.addAll(itemList)
                                dataList.addAll(itemList)
                                Log.d("size",itemList.size.toString() +","+mainOutletList.size.toString()+ " ," + dataList.size.toString())
                                mAdapter.notifyDataSetChanged()


                            }
                            else {
                                val s = SweetAlertDialog(requireContext(),SweetAlertDialog.ERROR_TYPE)
                                s.titleText = "Failed"
                                s.contentText = outletResponseBody.message
                                s.setConfirmButton("Ok", SweetAlertDialog.OnSweetClickListener {
                                    s.dismissWithAnimation()
                                    /* val editor = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE).edit()
                                     User.user?.clear(editor)
                                     activity!!.finish()
                                     val intent = Intent(requireContext(), LoginActivity::class.java)
                                     startActivity(intent)*/
                                })
                                s.setCancelable(false)
                                s.show()
                                Log.d("null","session expired")
                            }
                        }
                    }
                    else {
                        val s = SweetAlertDialog(requireContext(),SweetAlertDialog.ERROR_TYPE)
                        s.titleText = "Failed"
                        s.setConfirmButton("Ok", SweetAlertDialog.OnSweetClickListener {
                            s.dismissWithAnimation()
                            /* val editor = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE).edit()
                             User.user?.clear(editor)
                             activity!!.finish()
                             val intent = Intent(requireContext(), LoginActivity::class.java)
                             startActivity(intent)*/
                        })
                        s.setCancelable(false)
                        s.show()
                        Log.d("null","session expired")
                    }
                }

                override fun onFailure(call: Call<OutletListResponse>?, t: Throwable?) {
                    s?.dismiss()
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



    inner class OutleListDataAdapter(dataList: java.util.ArrayList<OutletResult>, context: Context) :
        RecyclerView.Adapter<OutleListDataAdapter.MyViewHolder>() {
        var mc = context

        private val dataList: java.util.ArrayList<OutletResult> = dataList
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.retailer_row_layout, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val data = dataList[position]
            holder.outletName.text = data.name
            holder.outletId.text = "Outlet Id: " + data.id
            /*holder.outletAddress.text = "${data.area_name}, ${data.zone_name}"
            if (data.outlet_status_id != null) {
                when (data.outlet_status_id) {
                    "2" -> {
                        holder.outletStatusLayout.setBackgroundResource(R.color.indigo)
                    }
                    "3" -> {
                        holder.outletStatusLayout.setBackgroundResource(R.color.eebee_blue)
                    }
                    else -> {
                        holder.outletStatusLayout.setBackgroundResource(R.color.white)
                    }
                }
            }*/

            holder.rowLayout.setOnClickListener {
                val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("outletDetails", Gson().toJson(data))
                editor.apply()
                findNavController().navigate(R.id.action_outletListFragment_to_fragmentConsumerContact2)
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class MyViewHolder(convertView: View) : RecyclerView.ViewHolder(convertView) {
            var outletName: TextView = convertView.findViewById(R.id.outlet_name)
            var outletId: TextView = convertView.findViewById(R.id.outletId)
            var outletAddress: TextView = convertView.findViewById(R.id.outlet_address)
            var outletStatusLayout: AppCompatTextView = convertView.findViewById(R.id.outletStatusLayout)
            var rowLayout: ConstraintLayout = convertView.findViewById(R.id.retail_row_layout)
        }

    }



}