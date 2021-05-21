package com.diegoribeiro.todoapp.data.models

data class ToDoDateTime(
    var day: Int = 0,
    var month: Int = 0,
    var year: Int = 0,
    var hour: Int = 0,
    var minute: Int = 0
) {
    fun isDateReady(): Boolean{
        return day != 0 && month != 0 && year != 0
    }

    fun isTimeReady(): Boolean{
        return hour != 0
    }
    fun getDateTime(): String{
        return "${String.format("%02d", day)}/" +
                "${String.format("%02d", month)}/" +
                "$year - " +
                "${String.format("%02d", hour)}:" +
                "${String.format("%02d", minute)}"
    }

    fun getDate(): String{
        return "${String.format("%02d", day)}/" +
                "${String.format("%02d", month)}/" +
                "$year"
    }

    fun getTime(): String{
        return  "${String.format("%02d", hour)}:" +
                "${String.format("%02d", minute)}"
    }
}
