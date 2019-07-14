package io.github.stbtpersonal.wkschedule

import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainController(private val mainActivity: MainActivity) {
    val waniKaniInterface: WaniKaniInterface = WaniKaniInterface(this.mainActivity)
    val keyValueStore: KeyValueStore = KeyValueStore(this.mainActivity)

    init {
        this.mainActivity.findViewById<Button>(R.id.LoginButton).setOnClickListener { login() }
        Toast.makeText(this.mainActivity, "apiKey! ${this.keyValueStore.apiKey}", Toast.LENGTH_SHORT).show()
    }

    private fun login() {
        val loginEditText = this.mainActivity.findViewById<EditText>(R.id.LoginEditText)
        val apiKey = loginEditText.text.toString()

        this.waniKaniInterface.getUser(
            apiKey,
            { response ->
                Toast.makeText(this.mainActivity, "Success! $response", Toast.LENGTH_SHORT).show()
                this.keyValueStore.apiKey = apiKey
            },
            { error ->
                Toast.makeText(this.mainActivity, "Failed! ${error.networkResponse.statusCode}", Toast.LENGTH_SHORT)
                    .show()
                loginEditText.text.clear()
            })
    }
}