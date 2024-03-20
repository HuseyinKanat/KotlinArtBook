package com.example.artbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artbook.databinding.ActivityArtGalleryBinding
import com.example.artbook.databinding.RowRwBinding


class ArtBookAdapter(private val artlist: ArrayList<Art>):RecyclerView.Adapter<ArtBookAdapter.ArtBookHolder>() {

    class ArtBookHolder(val binding:RowRwBinding ):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtBookHolder {
        val binding = RowRwBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtBookHolder(binding)
    }

    override fun getItemCount(): Int {
        return  artlist.size
    }

    override fun onBindViewHolder(holder: ArtBookHolder, position: Int) {
        holder.binding.rvText.text= artlist[position].name
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,ArtGallery::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id", artlist[position].id)
            holder.itemView.context.startActivity(intent)

        }
    }
}