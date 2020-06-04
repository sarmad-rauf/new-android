package com.es.marocapp.usecase.login.login


import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.es.marocapp.R
import com.es.marocapp.adapter.LanguageCustomSpinnerAdapter
import com.es.marocapp.databinding.FragmentLoginBinding
import com.es.marocapp.model.responses.GetAccountHolderInformationResponse
import com.es.marocapp.model.responses.GetOptResponse
import com.es.marocapp.model.responses.ValidateOtpAndUpdateAliasesResponse
import com.es.marocapp.network.ApiConstant
import com.es.marocapp.usecase.BaseFragment
import com.es.marocapp.usecase.login.LoginActivity
import com.es.marocapp.usecase.login.LoginActivityViewModel
import com.es.marocapp.utils.Constants
import com.es.marocapp.utils.DialogUtils
import kotlinx.android.synthetic.main.layout_login_header.view.*


class LoginNumberFragment : BaseFragment<FragmentLoginBinding>(),
    AdapterView.OnItemSelectedListener,
    LoginClickListener {

    lateinit var mActivityViewModel: LoginActivityViewModel
    lateinit var mActivity: LoginActivity
    lateinit var mLanguageSpinnerAdapter: LanguageCustomSpinnerAdapter

    override fun setLayout(): Int {
        return R.layout.fragment_login
    }

    override fun init(savedInstanceState: Bundle?) {
        mActivityViewModel = ViewModelProvider(activity as LoginActivity).get(
            LoginActivityViewModel::class.java
        )

        mDataBinding.apply {
            viewmodel = mActivityViewModel
            listener = this@LoginNumberFragment
        }

        mDataBinding.root.txtHeaderTitle.text = getString(R.string.enter_your_number)

        mDataBinding.root.languageSpinner.visibility = View.VISIBLE
        mDataBinding.root.languageSpinner.onItemSelectedListener = this

        mActivity = activity as LoginActivity

        val languageItems = arrayOf("English", "Arabic", "Spanish", "Urdu")
        mLanguageSpinnerAdapter =
            LanguageCustomSpinnerAdapter(activity as LoginActivity, languageItems)
        mDataBinding.root.languageSpinner.apply {
            adapter = mLanguageSpinnerAdapter
        }

        //todo also here remove lenght-2 check in max line
        mDataBinding.inputPhoneNumber.filters =
            arrayOf<InputFilter>(LengthFilter(Constants.APP_MSISDN_LENGTH.toInt() - 2))
        subscribe()

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onLoginButtonClick(view: View) {
        //For Proper Flow un Comment all this section
        //TODO need to implement proper check for lenght of number
        if (mDataBinding.inputPhoneNumber.text.toString() == "" || mDataBinding.inputPhoneNumber.text.length < Constants.APP_MSISDN_LENGTH.toInt() - 2) {
            mDataBinding.inputLayoutPhoneNumber.error = "Please Enter Valid Mobile Number"
            mDataBinding.inputLayoutPhoneNumber.isErrorEnabled = true
        } else {
            mDataBinding.inputLayoutPhoneNumber.error = ""
            mDataBinding.inputLayoutPhoneNumber.isErrorEnabled = false

            var userMsisdn = mDataBinding.inputPhoneNumber.text.toString()
            if (userMsisdn.startsWith("0", false)) {
                mDataBinding.inputLayoutPhoneNumber.error = ""
                mDataBinding.inputLayoutPhoneNumber.isErrorEnabled = false
                var userMSISDNwithPrefix = userMsisdn.removePrefix("0")
                userMSISDNwithPrefix = Constants.APP_MSISDN_PREFIX + userMSISDNwithPrefix
                userMSISDNwithPrefix = userMSISDNwithPrefix.removePrefix("+")
                Constants.CURRENT_USER_MSISDN = userMSISDNwithPrefix
                Constants.CURRENT_NUMBER_DEVICE_ID =
                    userMSISDNwithPrefix + "-" + Constants.CURRENT_DEVICE_ID
                mActivityViewModel.requestForGetAccountHolderInformationApi(
                    context,
                    userMSISDNwithPrefix
                )
            } else {
                mDataBinding.inputLayoutPhoneNumber.error = "Please Enter Valid Mobile Number"
                mDataBinding.inputLayoutPhoneNumber.isErrorEnabled = true
            }
        }

        //For Checking Flow or New Screen UnComment This Line
//        (activity as LoginActivity).startNewActivityAndClear(activity as LoginActivity,MainActivity::class.java)
    }

    override fun onForgotPinClick(view: View) {
    }

    override fun onSignUpClick(view: View) {
//        mActivityViewModel.isSignUpFlow.set(true)
//        mActivity.navController.navigate(R.id.action_loginFragment_to_signUpNumberFragment)
    }

    fun subscribe() {
        mActivityViewModel.errorText.observe(this@LoginNumberFragment, Observer {
            DialogUtils.showErrorDialoge(activity as LoginActivity,it)
        })

        val mAccountHolderInfoResonseObserver = Observer<GetAccountHolderInformationResponse> {
            if (it.responseCode == ApiConstant.API_SUCCESS) {
                mActivityViewModel.isSignUpFlow.set(false)
                var deviceID = it.deviceId
                deviceID = deviceID.removePrefix("ID:")
                deviceID = deviceID.removeSuffix("@device/ALIAS")

                deviceID = deviceID.trim()

                if (deviceID.equals(Constants.CURRENT_NUMBER_DEVICE_ID)) {
                    checkUserRegsitrationAndActicationSenario(it)
                } else {
                    mActivityViewModel.accountHolderInfoResponse = it

                    mActivityViewModel.previousDeviceId = deviceID
                    mActivityViewModel.requestForGetOtpApi(activity)
                }
            } else {
                mActivityViewModel.isSignUpFlow.set(true)
                mActivity.navController.navigate(R.id.action_loginFragment_to_signUpDetailFragment)
            }
        }

        val mGetOtpResponseListner = Observer<GetOptResponse> {
            if (it.responseCode.equals(ApiConstant.API_SUCCESS)) {
                mActivityViewModel.requestForVerifyOtpAndUpdateAliaseAPI(
                    activity,
                    mActivityViewModel.previousDeviceId,
                    Constants.CURRENT_NUMBER_DEVICE_ID,
                    "11111"
                )
            }else{
                DialogUtils.showErrorDialoge(activity as LoginActivity,it.description)
            }
        }

        val mValidateOtpandAliasesResponseListner = Observer<ValidateOtpAndUpdateAliasesResponse> {
            if (it.responseCode.equals(ApiConstant.API_SUCCESS)) {
                checkUserRegsitrationAndActicationSenario(mActivityViewModel.accountHolderInfoResponse)
            }else{
                DialogUtils.showErrorDialoge(activity as LoginActivity,it.description)
            }
        }

        mActivityViewModel.getAccountHolderInformationResponseListner.observe(
            this,
            mAccountHolderInfoResonseObserver
        )
        mActivityViewModel.getOTPResponseListner.observe(this, mGetOtpResponseListner)
        mActivityViewModel.getValidateOtpAndUpdateAliasResponseListner.observe(
            this,
            mValidateOtpandAliasesResponseListner
        )
    }

    fun checkUserRegsitrationAndActicationSenario(response: GetAccountHolderInformationResponse) {
        if (response.accountHolderStatus.equals("ACTIVE", true)) {
            if (response.credentialList.credentials.isNotEmpty()) {

                for (i in response.credentialList.credentials.indices) {
                    if (response.credentialList.credentials[i].credentialtype.equals(
                            "password",
                            true
                        ) && response.credentialList.credentials[i].credentialstatus.equals(
                            "ACTIVE",
                            true
                        )
                    ) {
                        // this check means that User is Register and in active state with password set for his account so direct login api is called
                        //LoginwithCert APi is called
                        mActivityViewModel.activeUserWithoutPasswordType.set(false)
                        mActivityViewModel.activeUserWithoutPassword.set(false)

                        (activity as LoginActivity).navController.navigate(R.id.action_loginFragment_to_signUpNumberFragment)
                    } else {
                        // Create Crednetial Api is Called
                        //this check means user is register with state Active but didn't registered Password as his account having credetial type pin

                        mActivityViewModel.activeUserWithoutPasswordType.set(true)
                        mActivityViewModel.activeUserWithoutPassword.set(false)

                        (activity as LoginActivity).navController.navigate(R.id.action_loginFragment_to_setYourPinFragment)
                    }
                }
            }else{
                // Create Crednetial Api is Called
                //this check means user is register with state Active but didn't registered Password as his account having credetial type pin

                mActivityViewModel.activeUserWithoutPasswordType.set(true)
                mActivityViewModel.activeUserWithoutPassword.set(false)

                (activity as LoginActivity).navController.navigate(R.id.action_loginFragment_to_setYourPinFragment)
            }
        } else {
            //activation Api is called on next screens
            // This Check Means User Register Itself verifies OTP but Close App before setting his/her pin so user is redirected to setup Pin Fragment
            mActivityViewModel.activeUserWithoutPassword.set(true)
            mActivityViewModel.activeUserWithoutPasswordType.set(false)

            (activity as LoginActivity).navController.navigate(R.id.action_loginFragment_to_setYourPinFragment)
        }
    }

}
