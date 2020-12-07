package com.es.marocapp.usecase.billpayment

import android.os.Bundle
import android.view.View
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.es.marocapp.R
import com.es.marocapp.adapter.AirTimeDataAdpater
import com.es.marocapp.adapter.BillPaymentExpandableAdapter
import com.es.marocapp.adapter.BillPaymentFavoritesAdapter
import com.es.marocapp.databinding.FragmentBillPaymentMainTypeLayoutBinding
import com.es.marocapp.locale.LanguageData
import com.es.marocapp.model.billpaymentmodel.BillPaymentMenuModel
import com.es.marocapp.model.billpaymentmodel.BillPaymentSubMenuModel
import com.es.marocapp.model.responses.BillPaymentFatoratiStepTwoResponse
import com.es.marocapp.model.responses.Contact
import com.es.marocapp.model.responses.Creancier
import com.es.marocapp.model.responses.Param
import com.es.marocapp.network.ApiConstant
import com.es.marocapp.usecase.BaseFragment
import com.es.marocapp.usecase.airtime.AirTimeActivity
import com.es.marocapp.utils.Constants
import com.es.marocapp.utils.DialogUtils
import com.es.marocapp.utils.Logger
import com.google.android.material.bottomsheet.BottomSheetBehavior


class FragmentBillPaymentMain : BaseFragment<FragmentBillPaymentMainTypeLayoutBinding>() {

    private lateinit var mActivityViewModel: BillPaymentViewModel
    private lateinit var mBillPaymentFavouritesAdapter: BillPaymentFavoritesAdapter
    private var mFavoritesList: ArrayList<Contact> = arrayListOf()

    private var mTelecomBillSubMenusData: ArrayList<String> = arrayListOf()
    private lateinit var mTelecomBillSubMenusAdapter: AirTimeDataAdpater
    private lateinit var sheetBehavior: BottomSheetBehavior<ConstraintLayout>

    lateinit var listDataHeader: ArrayList<BillPaymentMenuModel>
    var listDataChild: HashMap<String, ArrayList<BillPaymentSubMenuModel>>? = HashMap<String, ArrayList<BillPaymentSubMenuModel>>()
    lateinit var mExpandableRecyclerAdapter : BillPaymentExpandableAdapter

    override fun setLayout(): Int {
        return R.layout.fragment_bill_payment_main_type_layout
    }

    override fun init(savedInstanceState: Bundle?) {
        mActivityViewModel = ViewModelProvider(activity as BillPaymentActivity).get(
            BillPaymentViewModel::class.java
        )
        mDataBinding.apply {
        }

        sheetBehavior = BottomSheetBehavior.from(mDataBinding.bottomSheetAirTime)

        mDataBinding.tvPaymentType.text = LanguageData.getStringValue("PaymentType")
        (activity as BillPaymentActivity).setHeaderTitle(
            LanguageData.getStringValue("BillPayment").toString()
        )

        mDataBinding.tvManageFavorites.text = LanguageData.getStringValue("ManageFavorites")
        if (Constants.mContactListArray.isEmpty()) {
            mDataBinding.billPaymentMangeFavGroup.visibility = View.GONE
        } else {
            mDataBinding.billPaymentMangeFavGroup.visibility = View.VISIBLE
            mFavoritesList.clear()
            for (contacts in Constants.mContactListArray) {
                var contactName = contacts.contactName
                if (contactName.contains("Telec_Internet@") || contactName.contains("Telec_PostpaidMobile@") ||
                    contactName.contains("Telec_PostpaidFix@") || contactName.contains("Util_")
                ) {
                    if (contactName.contains("Util_")) {
                        if (contactName.contains(",")) {
                            mFavoritesList.add(contacts)
                        }
                    } else {
                        mFavoritesList.add(contacts)
                    }
                }
            }

            if (mFavoritesList.isEmpty()) {
                mDataBinding.billPaymentMangeFavGroup.visibility = View.GONE
            } else {
                mDataBinding.billPaymentMangeFavGroup.visibility = View.VISIBLE

                mBillPaymentFavouritesAdapter = BillPaymentFavoritesAdapter(mFavoritesList,
                    object : BillPaymentFavoritesAdapter.BillPaymentFavoriteClickListner {
                        override fun onFavoriteItemTypeClick(selectedContact: Contact) {
                            if (selectedContact.contactName.contains("Util_")) {
                                mActivityViewModel.isBillUseCaseSelected.set(false)
                                mActivityViewModel.isFatoratiUseCaseSelected.set(true)

                                //TelecomBillPayment Fatourati Use Case
                                var contactName = selectedContact.contactName
                                contactName = contactName.substringAfter("@")
                                var name = contactName.substringBefore(",")
                                var withoutNameCommaSepratedString = contactName.substringAfter(",")
                                var result: List<String> =
                                    withoutNameCommaSepratedString.split(",").map { it.trim() }
                                /*for(value in result){
                                    Log.d("dataFromString",value)
                                }*/
                                var creancier = Creancier(result[0], result[1], "", "")
                                mActivityViewModel.fatoratiTypeSelected.set(creancier)

                                var stepTwoResponseDummy = BillPaymentFatoratiStepTwoResponse(
                                    "",
                                    Param("", result[2], ""), result[3], ""
                                )
                                mActivityViewModel.fatoratiStepTwoObserver.set(stepTwoResponseDummy)

                                var number = selectedContact.fri
                                number = number.substringBefore("@")
                                number = number.substringBefore("/")

                                mActivityViewModel.isUserSelectedFromFavorites.set(true)
                                mActivityViewModel.isQuickRechargeCallForBillOrFatouratie.set(true)

                                mActivityViewModel.transferdAmountTo = number
                                mActivityViewModel.requestForFatoratiStepFourApi(activity)

                            } else if (selectedContact.contactName.contains("Telec_Internet@")) {
                                mActivityViewModel.isBillUseCaseSelected.set(true)
                                mActivityViewModel.isFatoratiUseCaseSelected.set(false)
                                mActivityViewModel.isPostPaidMobileSelected.set(false)
                                mActivityViewModel.isPostPaidFixSelected.set(false)
                                mActivityViewModel.isInternetSelected.set(true)

                                mActivityViewModel.isUserSelectedFromFavorites.set(true)
                                mActivityViewModel.isQuickRechargeCallForBillOrFatouratie.set(true)

                                var number = selectedContact.fri
                                number = number.substringBefore("@")
                                number = number.substringBefore("/")

                                var msisdnEntered = number
                                var code = ""

                                mActivityViewModel.requestForPostPaidFinancialResourceInfoApi(
                                    activity,
                                    code,
                                    msisdnEntered
                                )

                                //TelecomBillPayment Internet Use Case
                            } else if (selectedContact.contactName.contains("Telec_PostpaidMobile@")) {
                                mActivityViewModel.isBillUseCaseSelected.set(true)
                                mActivityViewModel.isFatoratiUseCaseSelected.set(false)
                                mActivityViewModel.isPostPaidMobileSelected.set(true)
                                mActivityViewModel.isPostPaidFixSelected.set(false)
                                mActivityViewModel.isInternetSelected.set(false)

                                mActivityViewModel.isUserSelectedFromFavorites.set(true)
                                mActivityViewModel.isQuickRechargeCallForBillOrFatouratie.set(true)

                                var contactName = selectedContact.contactName
                                contactName = contactName.substringAfter("@")
                                contactName = contactName.substringAfter(",")

                                var number = selectedContact.fri
                                number = number.substringBefore("@")
                                number = number.substringBefore("/")

                                var msisdnEntered = number
                                var code = contactName

                                mActivityViewModel.requestForPostPaidFinancialResourceInfoApi(
                                    activity,
                                    code,
                                    msisdnEntered
                                )

                                //TelecomBillPayment PostPaidMobile Use Case
                            } else if (selectedContact.contactName.contains("Telec_PostpaidFix@")) {
                                mActivityViewModel.isBillUseCaseSelected.set(true)
                                mActivityViewModel.isFatoratiUseCaseSelected.set(false)
                                mActivityViewModel.isPostPaidMobileSelected.set(false)
                                mActivityViewModel.isPostPaidFixSelected.set(true)
                                mActivityViewModel.isInternetSelected.set(false)

                                mActivityViewModel.isUserSelectedFromFavorites.set(true)
                                mActivityViewModel.isQuickRechargeCallForBillOrFatouratie.set(true)

                                var contactName = selectedContact.contactName
                                contactName = contactName.substringAfter("@")
                                contactName = contactName.substringAfter(",")

                                var number = selectedContact.fri
                                number = number.substringBefore("@")
                                number = number.substringBefore("/")

                                var msisdnEntered = number
                                var code = contactName

                                mActivityViewModel.requestForPostPaidFinancialResourceInfoApi(
                                    activity,
                                    code,
                                    msisdnEntered
                                )

                                //TelecomBillPayment PostPaidFix Use Case
                            }
                        }

                        override fun onDeleteFavoriteItemTypeClick(selectedContact: Contact) {
                            mActivityViewModel.requestForDeleteFavoriteApi(
                                activity,
                                selectedContact.fri
                            )
                        }

                    })

                mDataBinding.manageFavRecycler.apply {
                    adapter = mBillPaymentFavouritesAdapter
                    layoutManager = LinearLayoutManager(
                        activity as BillPaymentActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }
            }
        }


        (activity as BillPaymentActivity).setHeaderVisibility(true)
        (activity as BillPaymentActivity).setCompanyIconToolbarVisibility(false)

        mActivityViewModel.popBackStackTo = -1

        mActivityViewModel.requestForBillPaymentCompaniesApi(activity)
//        populateTelecomBillsSubMenusList()
//        prepareDataForBillPayment()
        setStrings()
        initListner()
        subscribeObserver()

        //todo need to set these value
/*      mActivityViewModel.isBillUseCaseSelected.set(false)
        mActivityViewModel.isFatoratiUseCaseSelected.set(true)
        mActivityViewModel.isQuickRechargeCallForBillOrFatouratie.set(false)*/


    }

    private fun setStrings() {
        mDataBinding.tvPaymentType.text = LanguageData.getStringValue("PaymentType")
        (activity as BillPaymentActivity).setHeaderTitle(
            LanguageData.getStringValue("BillPayment").toString()
        )

        mDataBinding.tvManageFavorites.text = LanguageData.getStringValue("ManageFavorites")
        mDataBinding.btnCancel.text = LanguageData.getStringValue("BtnTitle_Cancel")
    }

    private fun prepareDataForBillPayment() {
/*
        listDataHeader = arrayListOf()
        listDataHeader.apply {
            this!!.add(BillPaymentMenuModel("Telecom Bill Payment",R.drawable.telecom_bill_updated_icon))
            this!!.add(BillPaymentMenuModel("Water And Electricity",R.drawable.water_electricity_update_icon))
        }

        val myTelecomBillSubMenus : ArrayList<BillPaymentSubMenuModel> = arrayListOf()
        myTelecomBillSubMenus.apply {
            add(BillPaymentSubMenuModel("Post Paid Mobile",R.drawable.default_no_company_icon))
            add(BillPaymentSubMenuModel("Post Paid Fixe",R.drawable.default_no_company_icon))
            add(BillPaymentSubMenuModel("Internet",R.drawable.default_no_company_icon))
        }

        val myWaterAndElectricitySubMenus : ArrayList<BillPaymentSubMenuModel> = arrayListOf()
        myWaterAndElectricitySubMenus.apply {
            add(BillPaymentSubMenuModel("Redal",R.drawable.redal_company_icon))
            add(BillPaymentSubMenuModel("Redama",R.drawable.radeema_company_icon))
        }

        listDataChild?.put(listDataHeader?.get(0)?.companyTilte!!, myTelecomBillSubMenus)
        listDataChild?.put(listDataHeader?.get(1)?.companyTilte!!, myWaterAndElectricitySubMenus)
*/

        mExpandableRecyclerAdapter = BillPaymentExpandableAdapter(activity as BillPaymentActivity, listDataHeader, listDataChild
        )

        // setting list adapter
        mDataBinding.paymentTypeRecycler.setAdapter(mExpandableRecyclerAdapter)

        // Listview Group click listener
        mDataBinding.paymentTypeRecycler.setOnGroupClickListener { parent, v, groupPosition, id ->
            /*Toast.makeText(activity,
                "Group Clicked " + listDataHeader[groupPosition].companyTilte,
                Toast.LENGTH_SHORT).show()*/
            Logger.debugLog("BillPaymentTesting",listDataHeader[groupPosition].companyTilte)

            false
        }

        // Listview Group expanded listener
        mDataBinding.paymentTypeRecycler.setOnGroupExpandListener {
           /* Toast.makeText(activity,
                listDataHeader[it].companyTilte + " Expanded",
                Toast.LENGTH_SHORT).show()*/
            Logger.debugLog("BillPaymentTesting",listDataHeader[it].companyTilte)
        }

        // Listview Group collasped listener
        mDataBinding.paymentTypeRecycler.setOnGroupCollapseListener {
            /*Toast.makeText(activity,
                listDataHeader[it].companyTilte + " Collapsed",
                Toast.LENGTH_SHORT).show()*/
            Logger.debugLog("BillPaymentTesting",listDataHeader[it].companyTilte)
        }

        // Listview on child click listener
        mDataBinding.paymentTypeRecycler.setOnChildClickListener(object  : ExpandableListView.OnChildClickListener{
            override fun onChildClick(
                parent: ExpandableListView?,
                v: View?,
                groupPosition: Int,
                childPosition: Int,
                id: Long
            ): Boolean {
                /*Toast.makeText(
                    activity,
                    listDataHeader[groupPosition].companyTilte
                            + " : "
                            + listDataChild?.get(listDataHeader[groupPosition].companyTilte)?.get(
                        childPosition)?.subCompanyTitle, Toast.LENGTH_SHORT)
                    .show()*/

                Logger.debugLog("BillPaymentTesting",listDataHeader[groupPosition].companyTilte
                        + " : "
                        + listDataChild?.get(listDataHeader[groupPosition].companyTilte)?.get(
                    childPosition)?.subCompanyTitle)

                if(listDataChild?.get(listDataHeader[groupPosition].companyTilte)?.get(
                        childPosition)?.subCompanyTitle.equals(Constants.KEY_FOR_POST_PAID_TELECOM_BILL)){
                    val state =
                        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                            BottomSheetBehavior.STATE_COLLAPSED
                        else
                            BottomSheetBehavior.STATE_EXPANDED
                    sheetBehavior.state = state

                    mActivityViewModel.isBillUseCaseSelected.set(true)
                    mActivityViewModel.isFatoratiUseCaseSelected.set(false)
                    mActivityViewModel.isQuickRechargeCallForBillOrFatouratie.set(false)
                }else{
                    mActivityViewModel.isBillUseCaseSelected.set(false)
                    mActivityViewModel.isFatoratiUseCaseSelected.set(true)
                    mActivityViewModel.isQuickRechargeCallForBillOrFatouratie.set(false)

                    var selectedCreancer = listDataChild?.get(listDataHeader[groupPosition].companyTilte)?.get(
                        childPosition)?.subCompanyTitle
                    if(!mActivityViewModel.getBillPaymentCompaniesResponseObserver.get()?.bills.isNullOrEmpty()){
                        var billList = mActivityViewModel.getBillPaymentCompaniesResponseObserver.get()?.bills
                        for(i in billList!!.indices){
                            if(!billList[i].name.equals(Constants.KEY_FOR_POST_PAID_TELECOM_BILL)){
                                var billCompaniesList = mActivityViewModel.getBillPaymentCompaniesResponseObserver.get()?.bills?.get(i)?.companies
                                for(j in billCompaniesList?.indices!!){
                                    if(selectedCreancer?.equals(billCompaniesList[j].nomCreancier)!!){
                                        mActivityViewModel.fatoratiTypeSelected.set(Creancier(billCompaniesList[j].codeCreance,billCompaniesList[j].codeCreancier,
                                            billCompaniesList[j].nomCreance,billCompaniesList[j].nomCreancier))
                                        Logger.debugLog("BillPaymentTesting",mActivityViewModel.fatoratiTypeSelected.get().toString())
                                        (activity as BillPaymentActivity).navController.navigate(R.id.action_fragmentBillPaymentMain_to_fragmentBillPaymentMsisdn)
                                    }
                                }
                            }
                        }
                    }
                }
                return false
            }

        })
    }

    private fun initListner() {
        sheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                /*
                BottomSheetBehavior.STATE_EXPANDED -> "Close Persistent Bottom Sheet"
                BottomSheetBehavior.STATE_COLLAPSED -> "Open Persistent Bottom Sheet"
                else -> "Persistent Bottom Sheet"*/
            }

        })

        //TO Open Bottom Sheet
        /*val state =
            if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_COLLAPSED
            else
                BottomSheetBehavior.STATE_EXPANDED
        sheetBehavior.state = state*/

        mDataBinding.btnCancel.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        mDataBinding.bottomSheetAirTime.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun populateTelecomBillsSubMenusList() {
        /*mTelecomBillSubMenusData.apply {
            add(LanguageData.getStringValue("PostpaidMobile").toString())
            add(LanguageData.getStringValue("PostpaidFix").toString())
            add(LanguageData.getStringValue("Internet").toString())
        }*/
        mTelecomBillSubMenusAdapter =
            AirTimeDataAdpater(
                mTelecomBillSubMenusData,
                object : AirTimeDataAdpater.AirTimeDataClickLisnter {
                    override fun onSelectedAirTimeData(selectedTelecomBillSubMenu: String) {

                        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                        if (selectedTelecomBillSubMenu.equals(
                                LanguageData.getStringValue("PostpaidMobile")
                            )
                        ) {
                            /*Toast.makeText(
                                activity,
                                "Telecom Bill Mobile Selected",
                                Toast.LENGTH_SHORT
                            ).show()*/

                            //Post Paid MObile
                            mActivityViewModel.billTypeSelected.set(LanguageData.getStringValue("PostpaidMobile"))
                            mActivityViewModel.billTypeSelectedIcon = R.drawable.postpaid_blue
                            mActivityViewModel.isPostPaidMobileSelected.set(true)
                            mActivityViewModel.isPostPaidFixSelected.set(false)
                            mActivityViewModel.isInternetSelected.set(false)

                            (activity as BillPaymentActivity).navController.navigate(R.id.action_fragmentBillPaymentMain_to_fragmentBillPaymentMsisdn)


                        } else if (selectedTelecomBillSubMenu.equals(
                                LanguageData.getStringValue("PostpaidFix")
                            )
                        ) {
                            /*Toast.makeText(
                                activity,
                                "Telecom Bill Fixe Selected",
                                Toast.LENGTH_SHORT
                            ).show()*/

                            //Post Paid Fixe
                            mActivityViewModel.billTypeSelected.set(LanguageData.getStringValue("PostpaidFix"))
                            mActivityViewModel.billTypeSelectedIcon = R.drawable.postpaid_fix_blue
                            mActivityViewModel.isPostPaidMobileSelected.set(false)
                            mActivityViewModel.isPostPaidFixSelected.set(true)
                            mActivityViewModel.isInternetSelected.set(false)

                            (activity as BillPaymentActivity).navController.navigate(R.id.action_fragmentBillPaymentMain_to_fragmentBillPaymentMsisdn)


                        } else if (selectedTelecomBillSubMenu.equals(
                                LanguageData.getStringValue("Internet")
                            )
                        ) {
                            /*Toast.makeText(
                                activity,
                                "Telecom Bill Internet Selected",
                                Toast.LENGTH_SHORT
                            ).show()*/

                            //Internet
                            mActivityViewModel.billTypeSelected.set(LanguageData.getStringValue("Internet"))
                            mActivityViewModel.billTypeSelectedIcon = R.drawable.internet_blue
                            mActivityViewModel.isPostPaidMobileSelected.set(false)
                            mActivityViewModel.isPostPaidFixSelected.set(false)
                            mActivityViewModel.isInternetSelected.set(true)

                            (activity as BillPaymentActivity).navController.navigate(R.id.action_fragmentBillPaymentMain_to_fragmentBillPaymentMsisdn)
                        }
                    }
                })

        mDataBinding.billPaymentSubUseCaseRecycler.apply {
            adapter = mTelecomBillSubMenusAdapter
            layoutManager = LinearLayoutManager(activity as BillPaymentActivity)
        }
    }

    private fun subscribeObserver() {
        mActivityViewModel.getFatoratiStepFourResponseListner.observe(this@FragmentBillPaymentMain,
            Observer {
                if (it.responseCode.equals(ApiConstant.API_SUCCESS)) {
                    if (it.params == null || it.params.isNullOrEmpty() || it.params.size < 1) {
                        //  DialogUtils.showErrorDialoge(activity, it.message)
                        val btnTxt = LanguageData.getStringValue("BtnTitle_OK")
                        val titleTxt = LanguageData.getStringValue("Error")
                        DialogUtils.showCustomDialogue(
                            activity,
                            btnTxt,
                            it.message,
                            titleTxt,
                            object : DialogUtils.OnCustomDialogListner {
                                override fun onCustomDialogOkClickListner() {

                                }
                            })
                    } else {
                        (activity as BillPaymentActivity).navController.navigate(R.id.action_fragmentBillPaymentMain_to_fragmentPostPaidBillDetails)
                    }
                } else {
                    DialogUtils.showErrorDialoge(activity, it.description)
                }
            }
        )

        mActivityViewModel.getPostPaidResourceInfoResponseListner.observe(this@FragmentBillPaymentMain,
            Observer {
                if (it.responseCode.equals(ApiConstant.API_SUCCESS)) {
                    if (it.response.custId != null) {
                        mActivityViewModel.custId = it.response.custId
                    }
                    if (it.response.custname != null) {
                        mActivityViewModel.custname = it.response.custname
                    }
                    mActivityViewModel.totalamount = it.response.totalamount
                    (activity as BillPaymentActivity).navController.navigate(R.id.action_fragmentBillPaymentMain_to_fragmentPostPaidBillDetails)
                } else {
                    DialogUtils.showErrorDialoge(activity, it.description)
                }
            }
        )

        mActivityViewModel.errorText.observe(this@FragmentBillPaymentMain, Observer {
            DialogUtils.showErrorDialoge(activity, it)
        }
        )

        mActivityViewModel.getDeleteFavoritesResponseListner.observe(this@FragmentBillPaymentMain,
            Observer {
                if(it.responseCode.equals(ApiConstant.API_SUCCESS)){
                    if(!it.contactList.isNullOrEmpty()){
                        Constants.mContactListArray.clear()
                        Constants.mContactListArray.addAll(it.contactList)
                        mDataBinding.billPaymentMangeFavGroup.visibility = View.VISIBLE

                        mFavoritesList.clear()
                        for(contacts in Constants.mContactListArray){
                            var contactName = contacts.contactName
                            if(contactName.contains("Telec_Internet@") || contactName.contains("Telec_PostpaidMobile@") ||
                                contactName.contains("Telec_PostpaidFix@") || contactName.contains("Util_")){
                                mFavoritesList.add(contacts)
                            }
                        }

                        if(mFavoritesList.isEmpty()){
                            mDataBinding.billPaymentMangeFavGroup.visibility = View.GONE
                        }else{
                            mDataBinding.billPaymentMangeFavGroup.visibility = View.VISIBLE
                            mBillPaymentFavouritesAdapter.notifyDataSetChanged()
                        }
                    }else{
                        Constants.mContactListArray.clear()
                        mDataBinding.billPaymentMangeFavGroup.visibility = View.GONE
                    }
                }else{
                    DialogUtils.showErrorDialoge(activity,it.description)
                }
            }
        )

        mActivityViewModel.getBillPaymentCompaniesResponseListner.observe(this@FragmentBillPaymentMain, Observer {
            if(it.responseCode.equals(ApiConstant.API_SUCCESS)){
                if(!it.bills.isNullOrEmpty()){
                    listDataHeader = arrayListOf()
                    for(i in it.bills.indices){
                        if(it.bills[i].name.equals(Constants.KEY_FOR_POST_PAID_TELECOM_BILL)){
                            listDataHeader.add(BillPaymentMenuModel(LanguageData.getStringValue("Bill").toString(),R.drawable.telecom_bill_updated_icon))

                            //Adding SubMenu
                            var arrayListOfSubMenu : ArrayList<BillPaymentSubMenuModel> = arrayListOf()
                            arrayListOfSubMenu.add(BillPaymentSubMenuModel(it.bills[i].name,""))

                            listDataChild?.put(LanguageData.getStringValue("Bill").toString(),arrayListOfSubMenu)

                            mTelecomBillSubMenusData.clear()
                            for(companyIndex in it.bills[i].companies.indices){
                                mTelecomBillSubMenusData.add(LanguageData.getStringValue(it.bills[i].companies[companyIndex].nomCreancier)
                                    .toString())
                            }
                        }else{
                            listDataHeader.add(BillPaymentMenuModel(it.bills[i].name,R.drawable.water_electricity_update_icon))
                            //Addding Sub Menu's
                            var arrayListOfSubMenu : ArrayList<BillPaymentSubMenuModel> = arrayListOf()
                            for(companyIndex in it.bills[i].companies.indices){
                                var logo = ""
                                if(it.bills[i].companies[companyIndex].logo.isNullOrEmpty()){
                                    logo = ""
                                }else{
                                    logo = it.bills[i].companies[companyIndex].logo
                                }
                                arrayListOfSubMenu.add(BillPaymentSubMenuModel(it.bills[i].companies[companyIndex].nomCreancier,logo))
                            }
                            listDataChild?.put(it.bills[i].name,arrayListOfSubMenu)
                        }
                    }

                    prepareDataForBillPayment()
                    populateTelecomBillsSubMenusList()
                }

//                listDataChild?.put(listDataHeader?.get(0)?.companyTilte!!, myTelecomBillSubMenus)
                /*listDataHeader = arrayListOf()
                listDataHeader.apply {
                    this!!.add(BillPaymentMenuModel("Telecom Bill Payment",R.drawable.telecom_bill_updated_icon))
                    this!!.add(BillPaymentMenuModel("Water And Electricity",R.drawable.water_electricity_update_icon))
                }*/
            }else{
                DialogUtils.showErrorDialoge(activity,it.description)
            }
        })
    }

}