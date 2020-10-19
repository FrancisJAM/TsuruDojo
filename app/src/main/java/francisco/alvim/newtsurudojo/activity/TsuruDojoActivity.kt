package francisco.alvim.newtsurudojo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.database.AppDatabase
import francisco.alvim.newtsurudojo.fragment.*
import kotlinx.android.synthetic.main.activity_tsuru_dojo.*

class TsuruDojoActivity : AppCompatActivity() {

    lateinit var viewModel: TsuruDojoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tsuru_dojo)
        viewModel = ViewModelProviders.of(this).get(TsuruDojoViewModel::class.java)
        /**
         * init database, so as to build it if it is not existing yet.
         */
        val database = AppDatabase.invoke(applicationContext)
        supportFragmentManager.beginTransaction().replace(mainFragment.id,MonthPaymentFragment()).commitAllowingStateLoss()
        btnSectionPayments.setActivated(true)
        btnSectionEvents.setActivated(false)
        btnSectionStudents.setActivated(false)
        btnSectionBalance.setActivated(false)
        setupButtons()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.openEventPage.observe(this, Observer {
            if (it.isFirstRun){
                supportFragmentManager.beginTransaction().replace(mainFragment.id, EventPaymentsFragment()).commitAllowingStateLoss()
            }
        })
        viewModel.onbackClick.observe(this, Observer {
            if (it.isFirstRun){
                onBackPressed()
            }
        })
    }

    private fun setupButtons(){
        btnSectionPayments.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(mainFragment.id,MonthPaymentFragment()).commitAllowingStateLoss()
            btnSectionPayments.setActivated(true)
            btnSectionEvents.setActivated(false)
            btnSectionStudents.setActivated(false)
            btnSectionBalance.setActivated(false)
            viewModel.updateSpinnerNames()
        }
        btnSectionEvents.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(mainFragment.id,EventsFragment()).commitAllowingStateLoss()
            btnSectionPayments.setActivated(false)
            btnSectionEvents.setActivated(true)
            btnSectionStudents.setActivated(false)
            btnSectionBalance.setActivated(false)
        }
        btnSectionStudents.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(mainFragment.id,StudentFragment()).commitAllowingStateLoss()
            btnSectionPayments.setActivated(false)
            btnSectionEvents.setActivated(false)
            btnSectionStudents.setActivated(true)
            btnSectionBalance.setActivated(false)
        }
        btnSectionBalance.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(mainFragment.id,BalanceFragment()).commitAllowingStateLoss()
            btnSectionPayments.setActivated(false)
            btnSectionEvents.setActivated(false)
            btnSectionStudents.setActivated(false)
            btnSectionBalance.setActivated(true)
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.mainFragment)
        if (fragment is EventPaymentsFragment){
            supportFragmentManager.beginTransaction().replace(mainFragment.id,EventsFragment()).commitAllowingStateLoss()
        } else {
            super.onBackPressed()
        }
    }
}
