package com.oruam.dogs.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.oruam.dogs.R
import com.oruam.dogs.databinding.FragmentListBinding
import com.oruam.dogs.viewmodel.ListViewModel

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {

    private var binding: FragmentListBinding? = null

    private lateinit var viewModel: ListViewModel
    private val dogsListAdapter = DogsListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this@ListFragment).get(ListViewModel::class.java)
        viewModel.refresh()

        binding?.dogsList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dogsListAdapter
        }
        binding?.refreshLayout?.setOnRefreshListener {
            binding?.dogsList?.visibility = View.GONE
            binding?.listError?.visibility = View.GONE
            binding?.loadingView?.visibility = View.VISIBLE
            viewModel.refreshBypassCache()
            binding?.refreshLayout?.isRefreshing = false
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.dogs.observe(viewLifecycleOwner, Observer { dogs ->
            dogs?.let {
                binding?.dogsList?.visibility = View.VISIBLE
                dogsListAdapter.updateDogList(dogs)
            }
        })

        viewModel.dogsLoadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding?.listError?.visibility = if (isError) View.VISIBLE else View.GONE
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding?.loadingView?.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding?.listError?.visibility = View.GONE
                    binding?.dogsList?.visibility = View.GONE
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionSettings -> {
                view?.let {
                    Navigation.findNavController(it)
                        .navigate(ListFragmentDirections.actionSettings())
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}