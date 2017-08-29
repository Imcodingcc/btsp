package cn.leither.btsp

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * Created by lvqiang on 17-8-22.
 */

class CommonAdapter<T>(private val context: Context, private val list: List<Any>, private val layoutId: Int//单布局
                       , private val variableId: Int) : BaseAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var binding: ViewDataBinding? = null
        if (convertView == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, parent, false)
        } else {
            binding = DataBindingUtil.getBinding(convertView)
        }
        binding!!.setVariable(variableId, list[position])
        return binding.root
    }
}