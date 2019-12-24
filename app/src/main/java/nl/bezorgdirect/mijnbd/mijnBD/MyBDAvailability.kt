package nl.bezorgdirect.mijnbd.mijnBD

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_my_bdavailability.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.Availability
import nl.bezorgdirect.mijnbd.api.AvailabilityPost
import nl.bezorgdirect.mijnbd.encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.recyclerviews.AvailabilityAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private lateinit var linearLayoutManager: LinearLayoutManager

class MyBDAvailability : AppCompatActivity() {

    private var availabilities = ArrayList<Availability>()
    private var activeCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdavailability)

        val custom_toolbar_title: TextView = this.findViewById(R.id.custom_toolbar_title)
        custom_toolbar_title.text = getString(R.string.lbl_mybdpersonalia)


        addAvailabilityLayout.visibility = View.GONE

        var listitems = AvailabilityAdapter(availabilities)
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

        val btn_addAvailability: FloatingActionButton = findViewById(R.id.btn_addAvailability)
        val btn_cancel: Button = findViewById(R.id.btn_cancel)
        val btn_submit: Button = findViewById(R.id.btn_submit)
        val lbl_error: TextView = findViewById(R.id.lbl_error)

        lbl_error.visibility = View.GONE

        btn_addAvailability.setOnClickListener{
            //addAvailabilityLayout.visibility = View.VISIBLE
            addDialog()
        }
        btn_cancel.setOnClickListener{
            addAvailabilityLayout.visibility = View.GONE
        }
        btn_submit.setOnClickListener{
            if(txt_dateInput.text != null && txt_startTimeInput.text != null && txt_endTimeInput.text != null) {
                val date = txt_dateInput.text.toString()
                val startTime = txt_startTimeInput.text.toString()
                val endTime = txt_endTimeInput.text.toString()
                val availabilityPost =
                    AvailabilityPost(date = date, startTime = startTime, endTime = endTime)
                //postAvailabilities(applicationContext, availabilityPost)
            }
            else lbl_error.visibility = View.VISIBLE
        }


        linearLayoutManager = LinearLayoutManager(this)
        list_availabilities.layoutManager = linearLayoutManager
        list_availabilities.adapter = listitems

        getAvailabilities(applicationContext)


    }

    private fun getAvailabilities(context: Context){
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

        val service = getApiService()
        val decryptedToken = getDecryptedToken(this)

        service.availablitiesGet(auth = decryptedToken).enqueue(object : Callback<ArrayList<Availability>> {
            override fun onResponse(
                call: Call<ArrayList<Availability>>,
                response: Response<ArrayList<Availability>>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!

                    availabilities = values
                    list_availabilities.adapter = AvailabilityAdapter(availabilities)
                    //list_availabilities.adapter?.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }

    private fun postAvailabilities(context: Context, availabilityPost: AvailabilityPost){
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:7071/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val sharedPref: SharedPreferences = context.getSharedPreferences("mybd", Context.MODE_PRIVATE)
        val encryptedToken = sharedPref.getString("T", "")

        val keyStoreWrapper = KeyStoreWrapper(context, "mybd")
        val Key = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("BD_KEY")
        var token = ""
        if(encryptedToken != "" && Key != null)
        {
            val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
            token = cipherWrapper.decrypt(encryptedToken!!, Key?.private)
        }
        else
        {
            return
        }

        var availabilityparam = arrayListOf<AvailabilityPost>()
        availabilityparam.add(availabilityPost)

        service.availablitiesPost(auth = token, availabilityPost = availabilityparam).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(context, "Availability added!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }
    fun deleteDialog()
    {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("Do you want to close this application ?")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> finish()
            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("AlertDialogExample")
        // show alert dialog
        alert.show()
    }
    fun addDialog()
    {
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(false)
        dialog .setContentView(R.layout.dialog_add_availability)
        val yesBtn = dialog .findViewById(R.id.btn_confirmAvailability) as Button
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }
        val hourPicker = dialog.findViewById(R.id.pkr_hour) as NumberPicker
        val minPicker = dialog.findViewById(R.id.pkr_min) as NumberPicker
        val dayPicker = dialog.findViewById(R.id.pkr_date) as NumberPicker
        val hours = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
        val mins = arrayOf("00", "15", "30", "45")
        val dates = getDates()

        hourPicker.minValue = 0
        hourPicker.maxValue = hours.size-1
        hourPicker.displayedValues = hours

        minPicker.minValue = 0
        minPicker.maxValue = mins.size-1
        minPicker.displayedValues = mins

        dayPicker.minValue = 0
        dayPicker.maxValue = dates.size-1
        println(dates.size)
        val dateArray = arrayOfNulls<String>(dates.size)
        dayPicker.displayedValues = dates.toArray(dateArray)

        dialog.show()
    }

    fun getDates() : ArrayList<String>
    {
        val fromatter = SimpleDateFormat("MM-dd")
        val today = Date()

        val dates = ArrayList<String>()
        for (i in 0..62)
        {
            val day = Date(today.time + (1000 * 60 * 60 * 24) * i)
            dates.add(fromatter.format(day))
        }
        return dates
    }

}
