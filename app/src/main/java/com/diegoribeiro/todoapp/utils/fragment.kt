package com.diegoribeiro.todoapp.utils

import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.dmribeiro.pokedex_app.utils.FragmentViewBindingDelegate

inline fun <reified T : ViewBinding> Fragment.viewBinding() =
    FragmentViewBindingDelegate(T::class.java)
