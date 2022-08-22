package me.yifeiyuan.flapdev.testcases

import android.util.Log
import android.view.View
import me.yifeiyuan.flap.FlapAdapter
import me.yifeiyuan.flap.delegate.adapterDelegate
import me.yifeiyuan.flap.ext.bindButton
import me.yifeiyuan.flap.ext.bindTextView
import me.yifeiyuan.flapdev.R
import me.yifeiyuan.flapdev.components.SimpleTextModel
import me.yifeiyuan.flapdev.components.TestAllModel
import me.yifeiyuan.flapdev.mockMultiTypeModels

private const val TAG = "LayoutDelegateDSLTest"

/**
 * Created by 程序亦非猿 on 2022/8/4.
 */
class LayoutDelegateDSLTestcase : BaseTestcaseFragment() {

    override fun onInit(view: View) {
        super.onInit(view)

        val simpleTextDelegate = adapterDelegate<SimpleTextModel>(R.layout.flap_item_simple_text) {
            onBind { model ->
                bindTextView(R.id.tv_content) {
                    text = model.content
                }
            }

            onBind { model, position, payloads, adapter ->
                bindTextView(R.id.tv_content) {
                    text = model.content
                }
            }

            onClick { model, position, adapter ->
                toast("onClick() called with: component = $this, model = $model, position = $position")
            }

            onLongClick { model, position, adapter ->
                toast("onLongClick() called with: component = $this, model = $model, position = $position")
                true
            }

            onViewAttachedToWindow {
                Log.d(TAG, "simpleTextDelegate onViewAttachedToWindow() called ,position=$position")
            }

            onViewDetachedFromWindow {
                Log.d(TAG, "simpleTextDelegate onViewDetachedFromWindow() called ,position=$position")
            }

            onViewRecycled {
                Log.d(TAG, "onViewRecycled() called")
            }

            onFailedToRecycleView {
                Log.d(TAG, "onFailedToRecycleView() called")
                false
            }

            onResume {
                Log.d(TAG, "simpleTextDelegate onResume() called")
            }

            onPause {
                Log.d(TAG, "simpleTextDelegate onPause() called")
            }

            onStop {
                Log.d(TAG, "simpleTextDelegate onStop() called")
            }

            onDestroy {
                Log.d(TAG, "simpleTextDelegate onDestroy() called")
            }
        }

        val testAllDelegate = adapterDelegate<TestAllModel>(R.layout.component_test_all_feature) {

            onBind { model, position, payloads, adapter ->
                bindButton(R.id.testFireEvent) {
                    setOnClickListener {
                        toast("testAllDelegate clicked button")
                    }
                }
            }

            onClick { model, position, adapter ->
                toast("onClick() called with: component = $this, model = $model, position = $position")
            }

            onLongClick { model, position, adapter ->
                toast("onLongClick() called with: component = $this, model = $model, position = $position")
                true
            }

            onViewAttachedToWindow {
                Log.d(TAG, "testAllDelegate onViewAttachedToWindow() called ,position=$position")
            }

            onViewDetachedFromWindow {
                Log.d(TAG, "testAllDelegate onViewDetachedFromWindow() called ,position=$position")
            }

            onViewRecycled {

            }

            onFailedToRecycleView {
                false
            }

            onResume {
                Log.d(TAG, "testAllDelegate onResume() called")
            }

            onPause {
                Log.d(TAG, "testAllDelegate onPause() called")
            }
        }

        adapter.registerAdapterDelegates(simpleTextDelegate, testAllDelegate)
    }

    override fun createAdapter(): FlapAdapter {
        return super.createAdapter().apply {
            clearAdapterDelegates()
        }
    }

    override fun isLongClickEnable(): Boolean {
        return false
    }

    override fun isClickEnable(): Boolean {
        return false
    }

    override fun createRefreshData(size: Int): MutableList<Any> {
        return mockMultiTypeModels()
    }
}