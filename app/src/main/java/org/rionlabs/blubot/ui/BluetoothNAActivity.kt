package org.rionlabs.blubot.ui

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.rionlabs.blubot.R
import org.rionlabs.blubot.databinding.ActivityBluetoothNaBinding


class BluetoothNAActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothNaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_na)

        setSupportActionBar(binding.toolbar)

        binding.apply {
            blNaGithubIssueLink.apply {
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener { openGitHubIssues() }
            }

            blNaDevEmailLink.apply {
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener { openEmailClient() }
            }
        }
    }

    private fun openGitHubIssues() {
        val url = getString(R.string.github_issues_url)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun openEmailClient() {
        val devEmail = getString(R.string.developer_email)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(devEmail))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        intent.type = "message/rfc822"
        startActivity(Intent.createChooser(intent, "Send Email"))
    }
}