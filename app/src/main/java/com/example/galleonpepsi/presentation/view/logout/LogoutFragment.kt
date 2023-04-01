package com.example.galleonpepsi.ui.logout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.galleonpepsi.R


import com.example.galleonpepsi.data.User
import com.example.galleonpepsi.presentation.view.login.viewmodel.LoginViewModel

class LogoutFragment : Fragment() {

    lateinit var userViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)
        var user: User? = null
        user = User.instance
        val sharedPreferences = requireActivity().getSharedPreferences(
            "user",
            AppCompatActivity.MODE_PRIVATE
        )
        if(sharedPreferences.getBoolean("formDone",false))
        {
            val log = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            log.titleText = "Please complete existing form first"
            log.setConfirmClickListener {
                log.dismissWithAnimation()
                //FragmentManager fm = getParentFragmentManager();
                //fm.popBackStack();
                requireActivity().onBackPressed()
            }
            log.setCancelClickListener {
                log.dismissWithAnimation()
                requireActivity().onBackPressed()
            }
            log.cancelText = "No"
            log.confirmText = "Ok"
            log.show()
        }
        else
        {
            val log = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            log.titleText = "Are you sure to Sign Out?"
            log.setConfirmClickListener {
                val editor = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE).edit()
                //editor.clear();
                //editor.apply();
                user?.clear(editor)
                deleteAppData()
                log.dismissWithAnimation()
                requireActivity().finish()
            }
            log.setCancelClickListener {
                log.dismissWithAnimation()
                //FragmentManager fm = getParentFragmentManager();
                //fm.popBackStack();
                requireActivity().onBackPressed()
            }
            log.cancelText = "No"
            log.confirmText = "Ok"
            log.show()
        }

    }
    private fun deleteAppData() {
        try {

            userViewModel.logout()

           /* // clearing app data
            val packageName = requireActivity().applicationContext.packageName
            val runtime = Runtime.getRuntime()
            runtime.exec("pm clear $packageName")*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}