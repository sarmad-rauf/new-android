package com.es.marocapp.usecase.favorites

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.es.marocapp.R
import com.es.marocapp.adapter.ViewFavoritesAdapter
import com.es.marocapp.databinding.FragmentFavoritesViewBinding
import com.es.marocapp.locale.LanguageData
import com.es.marocapp.model.responses.Contact
import com.es.marocapp.network.ApiConstant
import com.es.marocapp.usecase.BaseFragment
import com.es.marocapp.utils.Constants
import com.es.marocapp.utils.DialogUtils

class ViewFavoritesFragment : BaseFragment<FragmentFavoritesViewBinding>(),
    FavoritesPaymentClickListener {

    private lateinit var mActivitViewModel: FavoritesViewModel
    private lateinit var mFavoriteAdapter : ViewFavoritesAdapter
    private var mContactList : ArrayList<Contact> = arrayListOf()

    override fun setLayout(): Int {
        return R.layout.fragment_favorites_view
    }

    override fun init(savedInstanceState: Bundle?) {
        mActivitViewModel = ViewModelProvider(activity as FavoritesActivity).get(FavoritesViewModel::class.java)

        mDataBinding.apply {
            viewmodel = mActivitViewModel
            listner = this@ViewFavoritesFragment
        }

        if(mActivitViewModel.isPaymentSelected.get()!!){
            if(mActivitViewModel.isFatoratiUsecaseSelected.get()!!){

            }else{

            }
        }else{

        }

        mActivitViewModel.popBackStackTo = R.id.favoritesAddOrViewFragment
        (activity as FavoritesActivity).setHeader(LanguageData.getStringValue("View").toString())
        setStrings()
        populateFavoritesList(getContactList())
        subscribeObserver()
    }

    private fun subscribeObserver() {
        mActivitViewModel.errorText.observe(this@ViewFavoritesFragment, Observer {
            DialogUtils.showErrorDialoge(activity,it)
        })

        mActivitViewModel.getDeleteFavoritesResponseListner.observe(this@ViewFavoritesFragment,
            Observer {
                if(it.responseCode.equals(ApiConstant.API_SUCCESS)){
                    if(!it.contactList.isNullOrEmpty()){
                        Constants.mContactListArray.clear()
                        Constants.mContactListArray.addAll(it.contactList)
                        mContactList.clear()
                        mContactList.addAll(getContactList())
                        mFavoriteAdapter.notifyDataSetChanged()
                        if(mContactList.isEmpty()){
                            mDataBinding.cvViewFavorites.visibility = View.GONE
                        }else{
                            mDataBinding.cvViewFavorites.visibility = View.VISIBLE

                        }
//                        DialogUtils.showSuccessDialog(activity,it.description,object : DialogUtils.OnConfirmationDialogClickListner{
//                            override fun onDialogYesClickListner() {
////                                (activity as FavoritesActivity).navController.popBackStack(R.id.favoriteTypesFragment,false)
//                            }
//                        })
                    }
                }else{
                    DialogUtils.showErrorDialoge(activity,it.description)
                }
            }
        )
    }

    private fun getContactList(): ArrayList<Contact> {
        var mList : ArrayList<Contact> = arrayListOf()
        var mContact = Constants.mContactListArray
        if(mActivitViewModel.isPaymentSelected.get()!!){
            if(mActivitViewModel.isFatoratiUsecaseSelected.get()!!){
                for(contact in mContact){
                    if(contact.fri.contains(Constants.getFatoratiAlias(""))){
                        mList.add(contact)
                    }
                }
            }else{
                for(contact in mContact){
                    if(!contact.fri.contains(Constants.getFatoratiAlias(""))){
                        mList.add(contact)
                    }
                }
            }
        }else{
            for(contact in mContact){
                if(!contact.fri.contains(Constants.getFatoratiAlias(""))){
                    mList.add(contact)
                }
            }
        }
        return mList
    }

    private fun populateFavoritesList(mContacts : ArrayList<Contact>) {
        mContactList.clear()
        mContactList.addAll(mContacts)

        if(mContactList.isEmpty()){
            mDataBinding.cvViewFavorites.visibility = View.GONE
        }else{
            mDataBinding.cvViewFavorites.visibility = View.VISIBLE

        }

        mFavoriteAdapter = ViewFavoritesAdapter(mContactList,object : ViewFavoritesAdapter.ViewFavoritesClickListner{
            override fun onFavoritesItemClickListner(contact: Contact) {
                mActivitViewModel.requestForDeleteFavoriteApi(activity,contact.fri)
            }
        })

        mDataBinding.viewFavoritesRecycler.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mFavoriteAdapter
        }
    }

    private fun setStrings() {
        mDataBinding.viewFavoritesTitle.text = LanguageData.getStringValue("HereYouCanViewOrRemoveAccounts")
    }

    override fun onNextButtonClick(view: View) {

    }

    override fun onDeleteButtonClick(view: View) {
    }

}