package io.github.stbtpersonal.wkschedule

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        this.findViewById<Button>(R.id.LoginButton).setOnClickListener { logIn() }
    }

    private fun logIn() {
        val apiKey = this.findViewById<EditText>(R.id.LoginEditText).text.toString()

        val waniKaniInterface = WaniKaniInterface(this)
        waniKaniInterface.getUser(
            apiKey,
            { response -> Toast.makeText(this, "Success! $response", Toast.LENGTH_SHORT).show() },
            { error -> Toast.makeText(this, "Failed! ${error.networkResponse.statusCode}", Toast.LENGTH_SHORT).show() })
    }
}
