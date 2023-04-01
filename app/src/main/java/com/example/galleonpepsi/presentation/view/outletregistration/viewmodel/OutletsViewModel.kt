package com.example.galleonpepsi.presentation.view.outletregistration.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.galleonpepsi.data.outlet.OutletForSubmitData
import com.example.galleonpepsi.data.outlet.OutletResult
import com.example.galleonpepsi.domain.reposotories.OutletsRepository

import kotlinx.coroutines.launch

class OutletsViewModel : ViewModel() {
    val repository = OutletsRepository()
    private val _outletsList = MutableLiveData<List<OutletResult>>()
    val outletsList: LiveData<List<OutletResult>>
        get() = _outletsList

    private val _selectedOutlet = MutableLiveData<OutletResult>()
    val selectedOutlet: LiveData<OutletResult>
        get() = _selectedOutlet

    fun getOutletsList(outletName: String) {
        viewModelScope.launch {
//            val response = repository.getOutletsList(outletName)
//            if (response.success) {
//                _outletsList.postValue(response.outletResult)
//            } else {
//                // Handle error case
//            }
        }
    }

    fun setSelectedOutlet(outlet: OutletResult) {
        _selectedOutlet.value = outlet
    }

    fun clearSelectedOutlet() {
        _selectedOutlet.value = null
    }

//    fun updateOutlet(outlet: Outlet) {
//        viewModelScope.launch {
//            val response = repository.updateOutlet(outlet)
//            if (response.isSuccessful) {
//                // Handle success case
//            } else {
//                // Handle error case
//            }
//        }
//    }

    fun createOutlet(outletForSubmitData: OutletForSubmitData) {
        viewModelScope.launch {
//            val response = repository.createOutlet(outlet)
//            if (response.success) {
//                // Handle success case
//            } else {
//                // Handle error case
//            }
        }
    }

//    fun uploadOutletForm(file: File) {
//        viewModelScope.launch {
//            val response = repository.uploadOutletForm(file)
//            if (response.isSuccessful) {
//                // Handle success case
//            } else {
//                // Handle error case
//            }
//        }
//    }
}
