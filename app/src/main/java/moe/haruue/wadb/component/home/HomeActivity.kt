package moe.haruue.wadb.component.home

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import moe.haruue.wadb.R
import moe.haruue.wadb.app.AppBarFragmentActivity

class HomeActivity : AppBarFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
                    .commit()
        }
    }
}
