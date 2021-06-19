package com.diegoribeiro.todoapp

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(findNavController(R.id.navHostFragment))

        supportActionBar?.title = Html.fromHtml("<font color=\"red\">" + "TAREFAS" + "</font>")
        supportActionBar?.elevation = 0f
    }
    override fun onSupportNavigateUp(): Boolean{
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }



}

