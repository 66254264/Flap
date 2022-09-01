package me.yifeiyuan.flapdev.testcases

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import me.yifeiyuan.flap.ext.HeaderFooterAdapter
import me.yifeiyuan.flap.ext.SwipeDragHelper
import me.yifeiyuan.flapdev.R

private const val TAG = "HeaderFooterTestcase"

/**
 * 测试 Header + Footer 功能
 *
 * Created by 程序亦非猿 on 2022/8/1.
 */
class HeaderFooterTestcase : BaseTestcaseFragment() {

    override fun onInit(view: View) {
        super.onInit(view)

        val headerFooterAdapter = HeaderFooterAdapter(adapter)

        val headerView = LayoutInflater.from(activity).inflate(R.layout.header_layout, null, false)
        headerFooterAdapter.setupHeaderView(headerView)

        val footerView = LayoutInflater.from(activity).inflate(R.layout.footer_layout, null, false)
        headerFooterAdapter.setupFooterView(footerView)

        //需要处理偏移量
        adapter.doOnItemClick { recyclerView, childView, position ->
            Log.d(TAG, "doOnItemClick called with: childView = $childView, position = $position")
            val component = recyclerView.getChildViewHolder(childView)
            if (headerFooterAdapter.isHeader(component)) {
                toast("点击了 position = $position，是 Header!")
                return@doOnItemClick
            }
            if (headerFooterAdapter.isFooter(component)) {
                toast("点击了 position = $position，是 Footer!")
                return@doOnItemClick
            }
            val realPosition = if (position == 0) position else position - headerFooterAdapter.getHeaderCount()
            toast("点击了 position = $position，model=${adapter.getItemData(realPosition)}")
        }

        recyclerView.adapter = headerFooterAdapter

        SwipeDragHelper(headerFooterAdapter)
                .withDragEnable(true)
                .withSwipeEnable(true)
                .withDragFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN)
                .withSwipeFlags(ItemTouchHelper.START or ItemTouchHelper.END)
                .withSwipeBackground(ColorDrawable(Color.parseColor("#ff0000")))
                .onItemSwiped {
                    toast("滑动删除了一个 item , position=$it")
                }
                .onItemMoved { fromPosition, toPosition ->
                    toast("移动交换了 $fromPosition to $toPosition")
                }
                .onDragStarted { viewHolder, adapterPosition ->
                    Log.d(TAG, "开始拖动 position=$adapterPosition")
                    toast("开始拖动 position=$adapterPosition")
                }
                .onDragReleased { viewHolder, adapterPosition ->
                    Log.d(TAG, "拖动结束 position=$adapterPosition")
                    toast("拖动结束 position=$adapterPosition")
                }
                .onSwipeStarted { viewHolder, adapterPosition ->
                    Log.d(TAG, "滑动开始 position=$adapterPosition")
                    toast("滑动开始 position=$adapterPosition")
                }
                .onSwipeReleased { viewHolder, adapterPosition ->
                    Log.d(TAG, "滑动结束 position=$adapterPosition")
                    toast("滑动结束 position=$adapterPosition")
                }
                .attachToRecyclerView(recyclerView)
    }
}