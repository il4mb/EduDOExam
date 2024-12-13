package com.il4mb.edudoexam.ui.dashboard.store

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.il4mb.edudoexam.api.BuyPayload
import com.il4mb.edudoexam.api.Client
import com.il4mb.edudoexam.api.PriceList
import com.il4mb.edudoexam.api.ProductEndpoints
import com.il4mb.edudoexam.api.ResponsePriceList
import com.il4mb.edudoexam.api.response.Response
import com.il4mb.edudoexam.api.response.ResponseError

class StoreViewModel: ViewModel() {

    private val _priceList: MutableLiveData<PriceList> = MutableLiveData()
    val priceList: LiveData<PriceList> = _priceList

    private fun setPriceList(priceList: PriceList) {
        _priceList.value = priceList
    }

    fun fetchPriceList(activity: FragmentActivity, onSuccess: (PriceList) -> Unit = {}, onError: (ResponseError) -> Unit = {}) {
        Client<ProductEndpoints, ResponsePriceList>(activity, ProductEndpoints::class.java)
            .onSuccess {
                setPriceList(it.data)
                onSuccess(it.data)
            }
            .onError(onError)
            .fetch { it.getProducts() }
    }


    fun buyPackage(activity: FragmentActivity, userId: String, buyPayload: BuyPayload, onSuccess: () -> Unit = {}, onError: (ResponseError) -> Unit = {}) {

        Client<ProductEndpoints, Response>(activity, ProductEndpoints::class.java)
            .onSuccess {
                onSuccess()
            }
            .onError(onError)
            .fetch { it.processPurchase(
                userId = userId,
                body = buyPayload
            ) }
    }
}
