package com.example.demostripe

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import com.stripe.exception.StripeException
import com.stripe.model.Charge
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {
    //View attribute
    private var editTextCardNumber: EditText? = null
    private var editTextCardExpMonth: EditText? = null
    private var editTextCardExpYear: EditText? = null
    private var editTextCvc: EditText? = null
    private var editTextCostoumerName: EditText? = null
    private var editTextPhone: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextAmount: EditText? = null
    private var buttonPay: Button? = null

    //
    var context: Context? = null
    var progressDialog: ProgressDialog? = null

    ///AlertDialog dialog;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this.applicationContext
        //dialog = new AlertDialog(context);
        //runOnUiThread();
        //alert("shdflhdslf","dsfkls");
        //dialog.show();
        initializeView()
        initializeClickListner()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initializeClickListner() {
        buttonPay!!.setOnClickListener {
            if (validate()) {
                progressDialog!!.show()
                val cardNumber = editTextCardNumber!!.text.toString()
                val cardExpMonth =
                    Integer.valueOf(editTextCardExpMonth!!.text.toString())
                val cardExpYear =
                    Integer.valueOf(editTextCardExpYear!!.text.toString())
                val cardCVC = editTextCvc!!.text.toString()
                val card = Card(
                    cardNumber,
                    cardExpMonth,
                    cardExpYear,
                    cardCVC
                )
                if (!card.validateCard()) {
                    progressDialog!!.dismiss()
                    val msg =
                        "Your Card is not validate. \n Please Enter Valid Card Info"
                    alert(msg, "Card Not Valid")
                    //Toast.makeText(context,"Your Card is not validate. \n Please Enter Valid Card Info",Toast.LENGTH_LONG).show();
                } else {
                    //pk_test_8iKVwsSxm54QIveAPOJmLYYa
                    //pk_test_iFlqr6GriMtkKGnbNiEeVTEb00zXzS5E2S
                    val stripe = Stripe(
                        context!!,
                        "pk_test_iFlqr6GriMtkKGnbNiEeVTEb00zXzS5E2S"
                    )
                    stripe.createToken(card,
                        object : TokenCallback {
                            override fun onError(error: Exception) {
                                progressDialog!!.dismiss()
                                Toast.makeText(
                                    context,
                                    error.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            override fun onSuccess(token: Token) {
                                //sk_test_qPIWrxe3QefpEO2vRgdFkyFv
                                //sk_test_x7fFaDQiMESbiGJUo03LVTKv00kyjV3GaY
                                com.stripe.Stripe.apiKey =
                                    "sk_test_x7fFaDQiMESbiGJUo03LVTKv00kyjV3GaY"
                                val amount = (java.lang.Float.valueOf(
                                    editTextAmount!!.text.toString()
                                ) * 100).toInt()
                                val timestamp =
                                    Timestamp(System.currentTimeMillis())
                                val params: MutableMap<String, Any> =
                                    HashMap()
                                params["amount"] = amount
                                params["currency"] = "usd"
                                params["description"] = "Test Charge"
                                params["source"] = token.id
                                var metadata: MutableMap<String?, String> =
                                    HashMap()
                                metadata["order_id"] = "test_order_" + timestamp.time
                                metadata["customer_name"] = editTextCostoumerName!!.text.toString()
                                metadata["phone"] = editTextPhone!!.text.toString()
                                metadata["email"] = editTextEmail!!.text.toString()
                                metadata["receipt_email"] = editTextEmail!!.text.toString()
                                params["metadata"] = metadata
                                val requastToCharge =
                                    RequestToCharge()
                                val requastToChargeDtails = RequestToChargeDetails()
                                var charge: Charge? = null
                                try {
                                    val crgid = requastToCharge.execute(params.toMap()).get()!!//params
                                    charge = requastToChargeDtails.execute(crgid).get()
                                } catch (e: InterruptedException) {
                                    progressDialog!!.dismiss()
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        e.localizedMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: ExecutionException) {
                                    progressDialog!!.dismiss()
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        e.localizedMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                if (charge != null) {
                                    val metData: Map<String, String> =
                                        HashMap()
                                    metadata = charge.metadata
                                    val sb = StringBuilder()
                                    sb.append("Successfully Charged from your card \n")
                                    sb.append(
                                        """
                                            Customer Name:${metadata["customer_name"]}
                                            
                                            """.trimIndent()
                                    )
                                    sb.append(
                                        """
                                            Amount:${charge.amount.toDouble() / 100}
                                            
                                            """.trimIndent()
                                    )
                                    sb.append(
                                        """
                                            Order Id:${metadata["order_id"]}
                                            
                                            """.trimIndent()
                                    )
                                    sb.append(
                                        """
                                            Email:${metadata["email"]}
                                            
                                            """.trimIndent()
                                    )
                                    //String msg = "Successfully Charged from your card. \n Your Charged Id is : " ;
                                    alert(sb.toString(), "Success")
                                } else {
                                    val msg = "An Error ! \n Please Try again"
                                    alert(msg, "Failed")
                                }
                                //Toast.makeText(context,"Success :" +crgid,Toast.LENGTH_LONG).show();
                            }
                        })
                }
            }
        }
    }

    private fun validate(): Boolean {
        if (editTextCardNumber!!.text.toString().isEmpty()) {
            val errorMessage = "Please Enter Your Card Number"
            editTextCardNumber!!.error = errorMessage
            editTextCardNumber!!.requestFocus()
            return false
        }
        if (editTextEmail!!.text.toString().isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                editTextEmail!!.text.toString()
            ).matches()
        ) {
            val errorMessage = "Please Enter Your Valid Email"
            editTextEmail!!.error = errorMessage
            editTextEmail!!.requestFocus()
            return false
        }
        if (editTextCardExpMonth!!.text.toString().isEmpty()) {
            val errorMessage = "Please Enter Card Exp. Date"
            editTextEmail!!.error = errorMessage
            editTextEmail!!.requestFocus()
            return false
        }
        if (editTextCardExpYear!!.text.toString().isEmpty()) {
            val errorMessage = "Please Enter Card Exp. Date"
            editTextEmail!!.error = errorMessage
            editTextEmail!!.requestFocus()
            return false
        }
        if (editTextCvc!!.text.toString().isEmpty()) {
            val errorMessage = "Please Enter Card Exp. Date"
            editTextEmail!!.error = errorMessage
            editTextEmail!!.requestFocus()
            return false
        }
        if (editTextCostoumerName!!.text.toString().isEmpty()) {
            val errorMessage = "Please Enter Your Name"
            editTextEmail!!.error = errorMessage
            editTextEmail!!.requestFocus()
            return false
        }
        if (editTextPhone!!.text.toString().isEmpty()) {
            val errorMessage = "Please Enter Your Phone Number"
            editTextEmail!!.error = errorMessage
            editTextEmail!!.requestFocus()
            return false
        }
        return if (editTextAmount!!.text.toString().isEmpty()) {
            val errorMessage = "Please Enter Amount To Pay"
            editTextEmail!!.error = errorMessage
            editTextEmail!!.requestFocus()
            false
        } else true
    }

    private fun initializeView() {
        editTextCardNumber = findViewById(R.id.editTextCardNumber) as EditText
        editTextCardExpMonth = findViewById(R.id.editTextCardExpMonth) as EditText
        editTextCardExpYear = findViewById(R.id.editTextCardExpYear) as EditText
        editTextCvc = findViewById(R.id.editTextCvc) as EditText
        editTextCostoumerName = findViewById(R.id.editTextCustomerName) as EditText
        editTextPhone = findViewById(R.id.editTextPhone) as EditText
        editTextEmail = findViewById(R.id.editTextEmail) as EditText
        editTextAmount = findViewById(R.id.editTextAmount) as EditText
        buttonPay = findViewById(R.id.buttonPay) as Button
        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Please Wait....")
        progressDialog!!.setTitle("Stripe")
    }

    private fun alert(message: String, titel: String) {
        val builder: AlertDialog.Builder
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder(
                this@MainActivity,
                android.R.style.Theme_Material_Dialog_Alert
            )
        } else {
            AlertDialog.Builder(this@MainActivity)
        }
        builder.setTitle(titel)
            .setMessage(message)
            .setPositiveButton(
                "OK"
            ) { dialog, which -> dialog.dismiss() }
            .setIcon(android.R.drawable.alert_dark_frame)
            .show()
    }

    private inner class RequestToCharge :
        AsyncTask<Map<String?, Any?>?, Void?, String?>() {
        protected override fun doInBackground(vararg p0: Map<String?, Any?>?): String? {
            var result: String? = null
            try {
                val charge = Charge.create(p0[0])
                Log.i("crg_id", charge.id)
                result = charge.id
                //Toast.makeText(context,"Success :" +charge.getId(),Toast.LENGTH_LONG).show();
            } catch (e: StripeException) {
                e.printStackTrace()
                //result = e.getLocalizedMessage();
                Log.e("Error Crg :", e.localizedMessage)
            }
            return result
        }
    }

    private inner class RequestToChargeDetails :
        AsyncTask<String?, Void?, Charge?>() {
        protected override fun doInBackground(vararg p0: String?): Charge? {
            var charge: Charge?
            try {
                Log.i("Charge Id:", p0[0])
                charge = Charge.retrieve(p0[0])
            } catch (e: StripeException) {
                charge = null
                e.printStackTrace()
            }
            return charge
        }

        override fun onPostExecute(charge: Charge?) {
            super.onPostExecute(charge)
            progressDialog!!.dismiss()
        }
    }
}