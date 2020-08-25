package com.example.tracky.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tracky.R
import com.example.tracky.other.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //if our main activity is destroyed and user clicks on notification, then this onCreateWill be called
        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(toolbar)
        //setting navigation component with Bottom Navigation
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP */}

        //Since we have 5 fragments and we only wanna show 3 of them in bottomNav
        //So ..
        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->

                //id of the destination we navigated to
                when (destination.id) {

                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment  -> bottomNavigationView.visibility =
                        View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }

    //if our main activity isn't destroyed and user clicks on notification, then this onNewIntent be called
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }
}