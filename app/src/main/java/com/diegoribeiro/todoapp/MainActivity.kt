package com.diegoribeiro.todoapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.diegoribeiro.todoapp.feature.DatePickerFragment
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(findNavController(R.id.navHostFragment))

    }
    override fun onSupportNavigateUp(): Boolean{
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun showReviewDialog() {
        val reviewManager = ReviewManagerFactory.create(this)
        //val reviewManager = FakeReviewManager(context)
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d("***ReviewManager", task.result.toString())
                reviewManager.launchReviewFlow(this, task.result)
            }
        }
    }

}