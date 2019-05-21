package com.example.a20gosu_proj
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class flags (val context: Context, var listItemsTxt: Array<String>) : BaseAdapter() {


    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.flag_spinner, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        // setting adapter item height programatically.

        val params = view.layoutParams
        params.height = 60
        view.layoutParams = params

        vh.label.text = listItemsTxt.get(position)
        return view
    }

    override fun getItem(position: Int): Any? {

        return null

    }

    override fun getItemId(position: Int): Long {

        return 0

    }

    override fun getCount(): Int {
        return listItemsTxt.size
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView

        init {
            this.label = row?.findViewById(R.id.flagText) as TextView
        }
    }
}