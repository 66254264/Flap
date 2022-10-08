package me.yifeiyuan.flap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import me.yifeiyuan.flap.delegate.AdapterDelegate
import me.yifeiyuan.flap.delegate.AdapterDelegateManager
import me.yifeiyuan.flap.delegate.IAdapterDelegateManager
import me.yifeiyuan.flap.hook.AdapterHookManager
import me.yifeiyuan.flap.hook.IAdapterHookManager
import me.yifeiyuan.flap.service.AdapterServiceManager
import me.yifeiyuan.flap.service.IAdapterServiceManager

/**
 * 负责代理部分 Adapter API 实现
 *
 * Created by 程序亦非猿 on 2022/9/27.
 *
 * @since 3.1.5
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class FlapDelegation : IAdapterHookManager by AdapterHookManager(), IAdapterDelegateManager by AdapterDelegateManager(), IAdapterServiceManager by AdapterServiceManager() {

    companion object {
        private const val TAG = "FlapDelegation"

        /**
         * 当 Adapter.data 中存在一个 Model 没有对应的 AdapterDelegate.delegate()==true 时抛出
         */
        internal class AdapterDelegateNotFoundException(errorMessage: String) : Exception(errorMessage)
    }

    /**
     * Components 监听的生命周期对象，一般是 Activity
     * 默认取的是 RecyclerView.Context
     */
    internal var lifecycleOwner: LifecycleOwner? = null

    /**
     * Components 是否监听生命周期事件
     */
    internal var lifecycleEnable = true

    private val viewTypeDelegateCache: MutableMap<Int, AdapterDelegate<*, *>?> = mutableMapOf()
    private val delegateViewTypeCache: MutableMap<AdapterDelegate<*, *>, Int> = mutableMapOf()

    var fallbackDelegate: AdapterDelegate<*, *>? = null

    init {
        adapterHooks.addAll(Flap.adapterHooks)
        adapterDelegates.addAll(Flap.adapterDelegates)
        adapterServices.putAll(Flap.adapterServices)

        fallbackDelegate = Flap.globalFallbackAdapterDelegate
    }

    internal fun onCreateViewHolder(adapter: FlapAdapter, parent: ViewGroup, viewType: Int, layoutInflater: LayoutInflater): Component<*> {
        val delegate = getDelegateByViewType(viewType)
        dispatchOnCreateViewHolderStart(adapter, delegate, viewType)
        val component = delegate.onCreateViewHolder(layoutInflater, parent, viewType)
        dispatchOnCreateViewHolderEnd(adapter, delegate, viewType, component)
        return component
    }

    private fun dispatchOnCreateViewHolderStart(adapter: FlapAdapter, delegate: AdapterDelegate<*, *>, viewType: Int) {
        try {
            adapterHooks.forEach {
                it.onCreateViewHolderStart(adapter, delegate, viewType)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dispatchOnCreateViewHolderEnd(
            adapter: FlapAdapter,
            delegate: AdapterDelegate<*, *>,
            viewType: Int,
            component: Component<*>
    ) {
        try {
            adapterHooks.forEach {
                it.onCreateViewHolderEnd(adapter, delegate, viewType, component)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDelegateByViewType(viewType: Int): AdapterDelegate<*, *> {
        return viewTypeDelegateCache[viewType] ?: fallbackDelegate
        ?: throw AdapterDelegateNotFoundException("找不到 viewType = $viewType 对应的 Delegate，请先注册，或设置默认的 Delegate")
    }

    internal fun onBindViewHolder(
            adapter: FlapAdapter,
            itemData: Any,
            component: Component<*>,
            position: Int,
            payloads: MutableList<Any>
    ) {
        try {
            val delegate = getDelegateByViewType(component.itemViewType)
            dispatchOnBindViewHolderStart(adapter, delegate, component, itemData, position, payloads)
            delegate.onBindViewHolder(
                    component,
                    itemData,
                    position,
                    payloads,
                    adapter
            )
            dispatchOnBindViewHolderEnd(adapter, delegate, component, itemData, position, payloads)
            tryAttachLifecycleOwner(component)
        } catch (e: Exception) {
            e.printStackTrace()
            FlapDebug.e(TAG, "onBindViewHolder: Error = ", e)
        }
    }

    /**
     * Attaches the component to lifecycle if need.
     *
     * @param component The component we are going to bind.
     */
    private fun tryAttachLifecycleOwner(component: Component<*>) {
        if (lifecycleEnable) {
            if (lifecycleOwner == null) {
                throw NullPointerException("lifecycleOwner == null,无法监听生命周期,请先调用 FlapAdapter#setLifecycleOwner()")
            }
            lifecycleOwner!!.lifecycle.addObserver(component)
        }
    }

    private fun dispatchOnBindViewHolderStart(
            adapter: FlapAdapter,
            delegate: AdapterDelegate<*, *>,
            component: Component<*>,
            itemData: Any,
            position: Int,
            payloads: MutableList<Any>
    ) {
        try {
            adapterHooks.forEach {
                it.onBindViewHolderStart(adapter, delegate, component, itemData, position, payloads)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dispatchOnBindViewHolderEnd(
            adapter: FlapAdapter,
            delegate: AdapterDelegate<*, *>,
            component: Component<*>,
            data: Any,
            position: Int,
            payloads: MutableList<Any>
    ) {
        try {
            adapterHooks.forEach {
                it.onBindViewHolderEnd(adapter, delegate, component, data, position, payloads)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun getItemViewType(position: Int, itemData: Any): Int {

        var itemViewType: Int

        val delegate: AdapterDelegate<*, *>? = adapterDelegates.firstOrNull {
            it.delegate(itemData)
        } ?: fallbackDelegate

        if (delegateViewTypeCache.containsKey(delegate)) {
            return delegateViewTypeCache[delegate]!!
        }

        itemViewType = delegate?.getItemViewType(itemData)
                ?: throw AdapterDelegateNotFoundException("找不到对应的 AdapterDelegate，请先注册或设置默认 AdapterDelegate ，position=$position , itemData=$itemData")

        if (itemViewType == 0) {
            itemViewType = generateItemViewType()
        }
        FlapDebug.d(TAG, "getItemViewType() called with: position = $position , itemViewType = $itemViewType")
        viewTypeDelegateCache[itemViewType] = delegate
        delegateViewTypeCache[delegate] = itemViewType
        return itemViewType
    }

    private fun generateItemViewType(): Int {
        val viewType = ViewTypeGenerator.generateViewType()
        if (viewTypeDelegateCache.containsKey(viewType)) {
            return generateItemViewType()
        }
        return viewType
    }

    internal fun getItemId(position: Int, itemData: Any): Long {
        val delegate = getDelegateByViewType(getItemViewType(position, itemData))
        return delegate.getItemId(itemData, position)
    }

    internal fun onViewRecycled(adapter: FlapAdapter, component: Component<*>) {
        val delegate = getDelegateByViewType(component.itemViewType)
        delegate.onViewRecycled(adapter, component)
    }

    internal fun onFailedToRecycleView(adapter: FlapAdapter, component: Component<*>): Boolean {
        val delegate = getDelegateByViewType(component.itemViewType)
        return delegate.onFailedToRecycleView(adapter, component)
    }

    internal fun onViewAttachedToWindow(adapter: FlapAdapter, component: Component<*>) {
        val delegate = getDelegateByViewType(component.itemViewType)
        delegate.onViewAttachedToWindow(adapter, component)

        dispatchOnViewAttachedToWindow(adapter, delegate, component)
    }

    private fun dispatchOnViewAttachedToWindow(adapter: FlapAdapter, delegate: AdapterDelegate<*, *>, component: Component<*>) {
        try {
            adapterHooks.forEach {
                it.onViewAttachedToWindow(adapter, delegate, component)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun onViewDetachedFromWindow(adapter: FlapAdapter, component: Component<*>) {
        val delegate = getDelegateByViewType(component.itemViewType)
        delegate.onViewDetachedFromWindow(adapter, component)

        dispatchOnViewDetachedFromWindow(adapter, delegate, component)
    }

    private fun dispatchOnViewDetachedFromWindow(adapter: FlapAdapter, delegate: AdapterDelegate<*, *>, component: Component<*>) {
        try {
            adapterHooks.forEach {
                it.onViewDetachedFromWindow(adapter, delegate, component)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun onAttachedToRecyclerView(adapter: FlapAdapter, recyclerView: RecyclerView) {
        //当没设置 lifecycleOwner 尝试获取 context 作为 LifecycleOwner
        if (lifecycleOwner == null && recyclerView.context is LifecycleOwner) {
            FlapDebug.d(TAG, "onAttachedToRecyclerView，FlapAdapter 自动设置了 recyclerView.context 为 LifecycleOwner")
            lifecycleOwner = recyclerView.context as LifecycleOwner
        }
        dispatchOnAttachedToRecyclerView(adapter, recyclerView)
    }

    private fun dispatchOnAttachedToRecyclerView(adapter: FlapAdapter, recyclerView: RecyclerView) {
        try {
            adapterHooks.forEach {
                it.onAttachedToRecyclerView(adapter, recyclerView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun onDetachedFromRecyclerView(adapter: FlapAdapter, recyclerView: RecyclerView) {
        dispatchOnDetachedFromRecyclerView(adapter, recyclerView)
    }

    private fun dispatchOnDetachedFromRecyclerView(adapter: FlapAdapter, recyclerView: RecyclerView) {
        try {
            adapterHooks.forEach {
                it.onDetachedFromRecyclerView(adapter, recyclerView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}