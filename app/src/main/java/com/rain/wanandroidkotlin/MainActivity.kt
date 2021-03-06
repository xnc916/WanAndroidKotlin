package com.rain.wanandroidkotlin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.navigation.NavigationView
import com.rain.wanandroidkotlin.base.BaseActivity
import com.rain.wanandroidkotlin.eventbus.UpdateUserInfo
import com.rain.wanandroidkotlin.ui.activity.*
import com.rain.wanandroidkotlin.ui.fragment.DemoFragment
import com.rain.wanandroidkotlin.ui.fragment.HomeFragment
import com.rain.wanandroidkotlin.ui.fragment.SystemFragment
import com.rain.wanandroidkotlin.ui.fragment.WxFragment
import com.rain.wanandroidkotlin.util.*
import com.rain.wanandroidkotlin.util.glide.GlideApp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    var fragmentList: ArrayList<Fragment>? = null
    var lastSelect = 0

    private val naviListener = BottomNavigationView.OnNavigationItemSelectedListener {
        when (it.itemId) {
            R.id.navigation_home -> {
                selectFragment(0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                selectFragment(1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_wx -> {
                selectFragment(2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_demo -> {
                selectFragment(3)
                return@OnNavigationItemSelectedListener true
            }
            else -> return@OnNavigationItemSelectedListener false
        }
    }

    private fun selectFragment(current: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        val lastFragment = fragmentList!!.get(lastSelect)
        val currentFragment = fragmentList!!.get(current)
        lastSelect = current
        transaction.hide(lastFragment)
        if (!currentFragment.isAdded) {
            transaction.add(R.id.content_main, currentFragment)
        }
        transaction.show(currentFragment).commitAllowingStateLoss()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            scrollToTop()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // label 始终可见
        bottom_navigation.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
        bottom_navigation.setOnNavigationItemSelectedListener(naviListener)

        initData()

        setUserInfo()

    }

    // 用户信息发生变化时调用
    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun updateUserInfo(event:UpdateUserInfo) {
        setUserInfo()
    }

    private fun setUserInfo() {
        val isLogin = SharedPreferenceUtil.get(this, Constant.ISLOGIN, false) as Boolean
        val headerView = nav_view.getHeaderView(0)
        if (!isLogin) {
            headerView.userName.text = "登录"
            headerView.userName.setOnClickListener {
                JumpUtil.overlay(this, LoginActivity::class.java)
            }
            headerView.userImage.setBackgroundResource(R.mipmap.ic_launcher_round)
        } else {
            headerView.userName.text = SharedPreferenceUtil.get(this, Constant.USERNAME, "") as String
            // todo 设置用户头像
            GlideApp.with(this)
                    .load("")
                    .circleCrop()
                    .into(headerView.userImage)
        }
    }

    private fun scrollToTop() {
        when (lastSelect) {
            0 -> (fragmentList?.get(0) as HomeFragment).scrollToTop()
            1 -> (fragmentList?.get(1) as SystemFragment).scrollToTop()
            2 -> (fragmentList?.get(2) as WxFragment).scrollChildToTop()
            3 -> (fragmentList?.get(3) as DemoFragment).scrollChildToTop()
        }
    }

    private fun initData() {
        initFragment()
        selectFragment(0)
    }

    private fun initFragment() {
        fragmentList = ArrayList()
        fragmentList?.add(HomeFragment.getInstance())
        fragmentList?.add(SystemFragment.getInstance())
        fragmentList?.add(WxFragment.getInstance())
        fragmentList?.add(DemoFragment.getInstance())
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_hot -> {
                JumpUtil.overlay(this, HotWebsiteActivity::class.java)
                return true
            }
            R.id.action_search -> {
                JumpUtil.overlay(this, SearchActivity::class.java)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_collect -> {
                JumpUtil.overlay(this,CollectionListActivity::class.java)
            }

            R.id.nav_setting -> {

            }
            R.id.nav_about_us -> {
                JumpUtil.overlay(this, AboutUsActivity::class.java)
            }

            R.id.nav_loginout -> {
                SharedPreferenceUtil.clear(this)
                clearCookies()
                ToastUtil.showToast(getString(R.string.logout_ok))
                setUserInfo()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
