package me.yifeiyuan.flapdev

import android.content.Intent
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import me.yifeiyuan.flap.FlapAdapter
import me.yifeiyuan.flap.ktmodule.KtComponentModel
import me.yifeiyuan.flapdev.components.customviewtype.CustomModel
import me.yifeiyuan.flapdev.components.databindingsample.SimpleDataBindingModel
import me.yifeiyuan.flapdev.components.generictest.GenericModel
import me.yifeiyuan.flapdev.components.simpleimage.SimpleImageModel
import me.yifeiyuan.flapdev.components.simpletext.SimpleTextModel
import me.yifeiyuan.flapdev.showcase.refresh.HomeFragment
import me.yifeiyuan.flapdev.showcase.selection.SlideshowFragment
import me.yifeiyuan.flapdev.showcase.swipe.GalleryFragment
import me.yifeiyuan.ktx.foundation.othermodule.JavaModuleModel
import me.yifeiyuan.ktx.foundation.othermodule.vb.VBModel
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_app_bar_open_drawer_description, R.string.navigation_drawer_close)
        drawerLayout.setDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { it ->
            when (it.itemId) {
                R.id.nav_home -> {
                    replace(HomeFragment::class.java)
                }
                R.id.nav_gallery -> {
                    replace(GalleryFragment::class.java)
                }
                R.id.nav_slideshow -> {
                    replace(SlideshowFragment::class.java)
                }
            }
            drawerLayout.close()
            true
        }

        replace(HomeFragment::class.java)

    }

    private fun <T : Fragment> replace(fragmentClass: Class<T>, args: Bundle? = null) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragmentClass, args).commitAllowingStateLoss()
    }

    private fun createSimpleTestCase(recyclerView: RecyclerView) {
        val adapter = FlapAdapter()
        val models: MutableList<Any> = ArrayList()
        models.add(SimpleTextModel("Android"))
        models.add(SimpleTextModel("Java"))
        models.add(SimpleTextModel("Kotlin"))
        models.add(SimpleImageModel())
        models.add(SimpleImageModel())
        models.add(SimpleImageModel())
        models.add(SimpleImageModel())
        adapter.data = models
        recyclerView.adapter = adapter
    }

//    private fun createAdvanceTestCase(recyclerView: RecyclerView) {
//        val models = mockModels()
//        val adapter: FlapAdapter = ShowcaseAdapter()
//        adapter.setUseComponentPool(true)
//                .setLifecycleEnable(true)
//                .setLifecycleOwner(this)
//                .data = models
//        recyclerView.adapter = adapter
//    }

    private fun mockModels(): List<Any> {
        val models: MutableList<Any> = ArrayList()
        models.add(SimpleTextModel("Flap（灵动）"))
        models.add(SimpleTextModel("一个基于 RecyclerView 的页面组件化框架"))
        models.add(SimpleTextModel("—— by 程序亦非猿"))
        models.add(SimpleImageModel())
        models.add(CustomModel())
        models.add(GenericModel())
        models.add(SimpleDataBindingModel())
        models.add(JavaModuleModel())
        models.add(KtComponentModel())
        models.add(VBModel())
        return models
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.main, menu);
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.differ -> startActivity(Intent(this@MainActivity, DifferActivity::class.java))
            R.id.kotlin -> startActivity(Intent(this@MainActivity, KotlinTestActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}