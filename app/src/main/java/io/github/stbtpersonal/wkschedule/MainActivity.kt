package io.github.stbtpersonal.wkschedule

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    private var mainController: MainController? = null // Reference held to avoid garbage collection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        this.mainController = MainController(this)
    }
}
