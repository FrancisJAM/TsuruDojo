package francisco.alvim.tsuru.dojo.activity

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.data.NavigationButton
import francisco.alvim.tsuru.dojo.database.AppDatabase
import francisco.alvim.tsuru.dojo.fragment.*
import kotlinx.android.synthetic.main.activity_tsuru_dojo.*

class TsuruDojoActivity : AppCompatActivity() {

    private val viewModel: TsuruDojoViewModel by viewModels()
    private val navigationButtons by lazy {
        listOf<Pair<NavigationButton, ImageButton>>(
            Pair(NavigationButton.MONTHLY_PAYMENTS, btnSectionPayments),
            Pair(NavigationButton.STUDENTS, btnSectionStudents),
            Pair(NavigationButton.EVENTS, btnSectionEvents),
            Pair(NavigationButton.BALANCE, btnSectionBalance)
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tsuru_dojo)
        /**
         * init database, so as to build it if it is not existing yet.
         */
        AppDatabase.invoke(applicationContext)
        goToNavigation(NavigationButton.MONTHLY_PAYMENTS)
        setupButtons()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.apply{
            openEventPage.observe(this@TsuruDojoActivity, Observer {
                it.onFirstRun { openFragment(EventPaymentsFragment()) }
            })
            openStudentNotes.observe(this@TsuruDojoActivity, Observer {
                it.onFirstRun { openFragment(StudentNotesFragment()) }
            })
            onBackClick.observe(this@TsuruDojoActivity, Observer {
                it.onFirstRun { onBackPressed() }
            })
        }
    }

    private fun setupButtons(){
        btnSectionPayments.setOnClickListener {
            goToNavigation(NavigationButton.MONTHLY_PAYMENTS)
            viewModel.updateSpinnerNames()
        }
        btnSectionStudents.setOnClickListener {
            goToNavigation(NavigationButton.STUDENTS)
        }
        btnSectionEvents.setOnClickListener {
            goToNavigation(NavigationButton.EVENTS)
        }
        btnSectionBalance.setOnClickListener {
            goToNavigation(NavigationButton.BALANCE)
        }
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.mainFragment)) {
            is EventPaymentsFragment -> {
                openFragment(EventsFragment())
            }

            is StudentNotesFragment -> {
                openFragment(StudentFragment())
            }

            else -> {
                super.onBackPressed()
            }
        }
    }


    private fun goToNavigation(navigation: NavigationButton){
        val fragment = when(navigation) {
            NavigationButton.MONTHLY_PAYMENTS -> MonthPaymentFragment()
            NavigationButton.STUDENTS -> StudentFragment()
            NavigationButton.EVENTS -> EventsFragment()
            NavigationButton.BALANCE -> BalanceFragment()
        }
        openFragment(fragment)
        selectNavigationSelection(navigation)
    }

    private fun selectNavigationSelection(navigation: NavigationButton){
        navigationButtons.forEach {
            it.second.setActivated(it.first == navigation)
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.commit(true) { replace(mainFragment.id, fragment) }
    }

}
