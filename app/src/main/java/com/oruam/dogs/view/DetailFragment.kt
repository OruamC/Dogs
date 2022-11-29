package com.oruam.dogs.view

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.oruam.dogs.R
import com.oruam.dogs.databinding.FragmentDetailBinding
import com.oruam.dogs.databinding.SendSmsDialogBinding
import com.oruam.dogs.model.DogBreed
import com.oruam.dogs.model.SmsInfo
import com.oruam.dogs.util.getProgressDrawable
import com.oruam.dogs.util.loadImage
import com.oruam.dogs.viewmodel.DetailViewModel

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {

    private var binding: FragmentDetailBinding? = null

    private lateinit var viewModel: DetailViewModel
    private var dogUuid = 0

    private var sendSmsStarted = false
    private var currentDog: DogBreed? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }

        viewModel = ViewModelProvider(this@DetailFragment).get(DetailViewModel::class.java)
        viewModel.fetch(dogUuid)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.dog.observe(viewLifecycleOwner, Observer { dog ->
            dog?.let {
                currentDog = dog
                it.imageUrl?.let { url ->
                    setupBackgroundColor(url)
                }

                binding?.dogName?.text = dog.dogBreed
                binding?.dogPurpose?.text = dog.bredFor
                binding?.dogTemperament?.text = dog.temperament
                binding?.dogLifespan?.text = dog.lifeSpan
                context?.let { binding?.dogImage?.loadImage(dog.imageUrl, getProgressDrawable(it)) }
            }
        })
    }

    private fun setupBackgroundColor(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource)
                        .generate { palette ->
                            val intColor = palette?.lightMutedSwatch?.rgb ?: 0
                            binding?.rlDetail?.setBackgroundColor(intColor)
                        }
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send_sms -> {
                sendSmsStarted = true
                (activity as MainActivity).checkSmsPermission()
            }
            R.id.action_share -> {
                // Generic flag to share informations to other applications
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed")
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}"
                )
                intent.putExtra(Intent.EXTRA_STREAM, currentDog?.imageUrl)
                startActivity(Intent.createChooser(intent, "Share with"))
            }
        }
        return super.onContextItemSelected(item)
    }

    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            context?.let {
                val smsInfo = SmsInfo(
                    "",
                    "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}",
                    currentDog?.imageUrl
                )
                val dialogBinding = SendSmsDialogBinding.inflate(LayoutInflater.from(it))
                AlertDialog.Builder(it)
                    .setView(dialogBinding.root)
                    .setPositiveButton("Send SMS") { _, _ ->
                        if (!dialogBinding.smsDestination.text.isNullOrEmpty()) {
                            smsInfo.to = dialogBinding.smsDestination.text.toString()
                            sendSms(smsInfo)
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }
                    .show()

                dialogBinding.smsText.setText(smsInfo.text)
                Glide.with(this).load(smsInfo.imageUrl).fitCenter().into(dialogBinding.smsImage)
            }
        }
    }

    private fun sendSms(smsInfo: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val sms = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            context?.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        sms?.sendTextMessage(smsInfo.to, null, smsInfo.text, pi, null)
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}