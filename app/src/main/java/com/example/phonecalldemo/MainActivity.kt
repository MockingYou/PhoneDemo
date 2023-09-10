package com.example.phonecalldemo

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.phonecalldemo.APIs.GetApartments
import com.example.phonecalldemo.APIs.MyApi
import com.example.phonecalldemo.apartments.Apartment
import com.example.phonecalldemo.apartments.ApartmentsAdapter
import com.example.phonecalldemo.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private var socketRepository:SocketRepository?=null
    private val BASE_URL = "http://10.0.2.2:3000"
    private lateinit var binding: ActivityMainBinding
    private lateinit var apartmentsAdapter: ApartmentsAdapter
    private var selectedAp: Apartment? = null
    private var apartments: List<Apartment> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initApartments()
        fetchDataAndDisplayApartments()

        binding.apartmentsShow.setOnClickListener {
            fetchDataAndDisplayApartments()
            binding.apartmentsRecycler.visibility = View.VISIBLE
        }

        binding.callBtn.setOnClickListener {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                ).request{ allGranted, _ ,_ ->
                    if (allGranted){
                        startActivity(
                            Intent(this,CallActivity::class.java)
                                .putExtra("targetUserName", selectedAp?.name.toString())
                        )
                    } else {
                        Toast.makeText(this,"you should accept all permissions", Toast.LENGTH_LONG).show()
                    }
                }

        }

    }
    private fun initApartments() {
        apartmentsAdapter = ApartmentsAdapter { apartment ->
            selectedAp = apartment
            binding.chooseApartmentText.text = apartment.name
            binding.apartmentsRecycler.visibility = View.GONE
        }
        binding.apartmentsRecycler.adapter = apartmentsAdapter
        apartmentsAdapter.submitList(apartments)

    }
    suspend fun fetchData(): Deferred<List<GetApartments>> = GlobalScope.async(Dispatchers.IO) {
        try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val apiService = retrofit.create(MyApi::class.java)

            val response = apiService.getApartments()
            if (response.isSuccessful) {
                val responseBody = response.body()
                responseBody ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // Handle the exception here, such as logging or showing an error message
            Log.e("FetchDataException", "Error fetching data: ${e.message}")
            emptyList()
        }
    }
    private fun fetchDataAndDisplayApartments() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                Log.e("FetchedApartments", "Fetching apartments...")
                // Fetch data using your fetchData() method
                val fetchedData = fetchData().await()

                apartments = fetchedData.map {
                    Apartment(it.apnumber, it.owner)
                }

                // Update the adapter with the fetched data
                apartmentsAdapter.submitList(apartments)

                // Make the RecyclerView visible
                binding.apartmentsRecycler.visibility = View.VISIBLE
            } catch (e: Exception) {
                // Handle any errors or exceptions here...
                Log.e("FetchDataException", "Error fetching data: ${e.message}")
                // You may want to show an error message to the user here.
            }
        }
    }
}