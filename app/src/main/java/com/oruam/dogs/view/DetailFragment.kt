package com.oruam.dogs.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.oruam.dogs.databinding.FragmentDetailBinding
import com.oruam.dogs.viewmodel.DetailViewModel

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {

    private var binding: FragmentDetailBinding? = null

    private lateinit var viewModel: DetailViewModel
    private var dogUuid = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }

        viewModel = ViewModelProvider(this@DetailFragment).get(DetailViewModel::class.java)
        viewModel.fetch()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.dog.observe(viewLifecycleOwner, Observer { dog ->
            dog?.let {
                binding?.dogName?.text = dog.dogBreed
                binding?.dogPurpose?.text = dog.breedFor
                binding?.dogTemperament?.text = dog.temperament
                binding?.dogLifespan?.text = dog.lifeSpan
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}