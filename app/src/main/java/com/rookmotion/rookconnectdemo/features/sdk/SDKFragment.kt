package com.rookmotion.rookconnectdemo.features.sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rookmotion.rookconnectdemo.databinding.FragmentSdkBinding
import com.rookmotion.rookconnectdemo.di.ViewModelFactory
import com.rookmotion.rookconnectdemo.extension.repeatOnResume
import com.rookmotion.rookconnectdemo.extension.serviceLocator

class SDKFragment : Fragment() {

    private var _binding: FragmentSdkBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SDKViewModel> { ViewModelFactory(serviceLocator) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSdkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repeatOnResume {
            viewModel.configuration.collect { binding.setConfigurationState.text = it }
        }

        binding.setConfiguration.setOnClickListener { viewModel.setConfiguration() }

        repeatOnResume {
            viewModel.initialize.collect { binding.initializeState.text = it }
        }

        binding.initialize.setOnClickListener { viewModel.initialize() }

        repeatOnResume {
            viewModel.user.collect { binding.updateUserState.text = it }
        }

        binding.updateUser.setOnClickListener {
            val userID = getUserID()

            if (userID != null) {
                viewModel.updateUserID(userID)
            }
        }

        repeatOnResume {
            viewModel.availability.collect { binding.checkAvailabilityState.text = it }
        }

        binding.checkAvailability.setOnClickListener { viewModel.checkAvailability(requireContext()) }
        binding.downloadHealthConnect.setOnClickListener { openPlayStore() }

        repeatOnResume {
            viewModel.permissions.collect { binding.checkPermissionsState.text = it }
        }

        binding.checkPermissions.setOnClickListener { viewModel.checkPermissions() }
        binding.requestPermissions.setOnClickListener { viewModel.requestPermissions(requireActivity()) }
        binding.openHealthConnect.setOnClickListener { viewModel.openHealthConnect() }

        repeatOnResume {
            viewModel.syncHealthData.collect { binding.syncHealthDataState.text = it }
        }

        binding.syncHealthData.setOnClickListener { viewModel.syncHealthData() }

        repeatOnResume {
            viewModel.pendingSummaries.collect { binding.syncPendingSummariesState.text = it }
        }

        binding.syncPendingSummaries.setOnClickListener { viewModel.syncPendingSummaries() }

        repeatOnResume {
            viewModel.pendingEvents.collect { binding.syncPendingEventsState.text = it }
        }

        binding.syncPendingEvents.setOnClickListener { viewModel.syncPendingEvents() }
    }

    private fun getUserID(): String? {
        val text = binding.userId.text.toString().trim()

        return if (text.isEmpty()) {
            binding.userIdContainer.error = "Cannot be empty"
            binding.userIdContainer.isErrorEnabled = true

            null
        } else {
            binding.userIdContainer.error = null
            binding.userIdContainer.isErrorEnabled = false

            text
        }
    }

    private fun openPlayStore() {
        requireContext().startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            )
        )
    }
}