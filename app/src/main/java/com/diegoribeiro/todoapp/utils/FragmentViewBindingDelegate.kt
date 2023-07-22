package com.dmribeiro.pokedex_app.utils

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class FragmentViewBindingDelegate<T : ViewBinding>(
    private val bindingClass: Class<T>
) : ReadOnlyProperty<Fragment, T>, DefaultLifecycleObserver {

    private var binding: T? = null

    @androidx.annotation.MainThread
    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        binding = null
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (binding == null) {
            binding = bindingClass.getMethod("inflate", LayoutInflater::class.java)
                .invoke(null, thisRef.layoutInflater) as T

            thisRef.viewLifecycleOwner.lifecycle.addObserver(this)
        }

        return binding!!
    }
}