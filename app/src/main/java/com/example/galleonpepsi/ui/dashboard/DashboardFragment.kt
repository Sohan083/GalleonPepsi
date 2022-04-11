package com.example.galleonpepsi.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.galleonpepsi.R
import com.example.galleonpepsi.databinding.FragmentDashboardBinding
import com.example.galleonpepsi.model.User
import com.example.galleonpepsi.ui.dashboard.response.DashboardResponseBody
import com.example.galleonpepsi.ui.posmactivity.PosmActivityApiInterface
import com.example.galleonpepsi.ui.posmactivity.responses.outletlistapiresponse.OutletListResponseBody
import com.example.galleonpepsi.utils.CustomUtility
import com.example.galleonpepsi.utils.StaticTags
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardFragment : Fragment() {
    lateinit var sweetAlertDialog: SweetAlertDialog
    lateinit var binding: FragmentDashboardBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDashboardData()

    }

    private fun getDashboardData() {
        if (CustomUtility.haveNetworkConnection(requireContext())) {
            sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
            sweetAlertDialog.titleText = "Loading"
            sweetAlertDialog.show()
            sweetAlertDialog.setCancelable(false)
            val retrofit = Retrofit.Builder()
                .baseUrl(StaticTags.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(DashboardApiInterface::class.java)
            val call = service.getDashboardData(User.user!!.employeeId!!)

            call.enqueue(object : Callback<DashboardResponseBody> {
                override fun onResponse(
                    call: Call<DashboardResponseBody>?,
                    response: retrofit2.Response<DashboardResponseBody>?
                ) {
                    sweetAlertDialog.dismiss()
                    Log.d("response", response?.body().toString())
                    if (response != null) {
                        if (response.code() == 200) {
                            val dashboardResponseBody = response.body()!!
                            if (dashboardResponseBody.success) {
                                binding.todayExecution.setText(dashboardResponseBody.dashboardData.executionToday)
                                binding.lifetimeExecution.setText(dashboardResponseBody.dashboardData.executionTillDate)
                            }
                            else {
                                val s = SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                                s.titleText = "Failed"
                                s.contentText = dashboardResponseBody.message
                                s.setConfirmButton("Ok", SweetAlertDialog.OnSweetClickListener {
                                    s.dismissWithAnimation()
                                })
                                s.setCancelable(false)
                                s.show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DashboardResponseBody>?, t: Throwable?) {
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

}