package com.joegruff.viacoinaddressscanner.helpers

import android.location.Address
import android.os.Handler
import android.os.Message
import android.util.Log
import com.joegruff.viacoinaddressscanner.R
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*
import kotlin.concurrent.schedule

class AddressObject : AsyncObserver {
    val JSON_ADDRESS: String = "address"
    val JSON_TITLE = "title"
    val JSON_AMOUNT = "amount"
    val JSON_TIMESTAMP = "timestamp"
    val JSON_OLD_AMOUNT = "oldamount"

    var address = ""
    var title = ""
    var amount = 0.0
    var isUpdating = false
    var isValid = false
    var oldestAmount = 0.0
    var oldestTimestamp = 0.0
    var delegate: AsyncObserver? = null
    var hasBeenInitiated = false


    constructor(jsonObject: JSONObject) {
        address = jsonObject.getString(JSON_ADDRESS)
        title = jsonObject.getString(JSON_TITLE)
        amount = jsonObject.getDouble(JSON_AMOUNT)
        oldestAmount = jsonObject.getDouble(JSON_OLD_AMOUNT)
        oldestTimestamp = jsonObject.getDouble(JSON_TIMESTAMP)
        hasBeenInitiated = true
        isValid = true
        oneminuteupdate()
    }

    constructor(add: String) {
        address = add
        oneminuteupdate()
    }

    fun toJSON(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(JSON_ADDRESS, address)
        jsonObject.put(JSON_TITLE, title)
        jsonObject.put(JSON_AMOUNT, amount)
        jsonObject.put(JSON_OLD_AMOUNT, oldestAmount)
        jsonObject.put(JSON_TIMESTAMP, oldestTimestamp)
        return jsonObject
    }


    override fun processbegan() {
        isUpdating = true
        try {
            delegate?.processbegan()
        } catch (e: Exception) {

        }

    }

    fun oneminuteupdate() {
        update()
        Handler().postDelayed({
            oneminuteupdate()
        }, 60000)


    }

    fun update(){
        Log.d("update","isupdating = "+isUpdating)
        if (!isUpdating) {
            isUpdating = true
            GetInfoFromWeb(this, address).execute()
        }
    }

    override fun processfinished(output: String?) {
        try {
            delegate?.processfinished(output)
        } catch (e: Exception) {

        }
        if (output != null) {
            val token = JSONTokener(output).nextValue()
            if (token is JSONObject) {
                val addressString = token.getString("addrStr")
                val amountString = token.getString("balance")
                if (address == addressString) {
                    amount = amountString.toDouble()
                    Log.d("addressobject", "process finished " + output)
                    if (!isValid) {
                        isValid = true

                    }
                }
            }
        }
        //Handler().postDelayed({isUpdating = false},5000)
        isUpdating = false
    }
}