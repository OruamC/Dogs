package com.oruam.dogs.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.oruam.dogs.databinding.ItemDogBinding
import com.oruam.dogs.model.DogBreed
import com.oruam.dogs.util.getProgressDrawable
import com.oruam.dogs.util.loadImage

class DogsListAdapter(private val dogList: ArrayList<DogBreed>) :
    RecyclerView.Adapter<DogsListAdapter.ViewHolder>() {

    fun updateDogList(newDogList: List<DogBreed>) {
        dogList.clear()
        dogList.addAll(newDogList)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemDogBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dog = dogList[position]
        holder.binding.name.text = dog.dogBreed
        holder.binding.lifespan.text = dog.lifeSpan

        holder.binding.llDogItem.setOnClickListener {
            val action = ListFragmentDirections.actionDetailFragment()
            action.dogUuid = dogList[position].uuid
            Navigation.findNavController(it).navigate(action)
        }
        holder.binding.imageView.loadImage(
            dogList[position].imageUrl,
            getProgressDrawable(holder.binding.imageView.context)
        )
    }

    override fun getItemCount(): Int {
        return dogList.size
    }
}