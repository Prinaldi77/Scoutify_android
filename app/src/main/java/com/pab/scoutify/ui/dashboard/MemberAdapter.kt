package com.pab.scoutify.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pab.scoutify.databinding.ItemMemberBinding
import com.pab.scoutify.model.Anggota

class MemberAdapter(private val members: List<Anggota>) :
    RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.binding.apply {
            tvMemberName.text = member.nama ?: "Anggota Pramuka"
            tvMemberRank.text = member.angkatan?.uppercase() ?: "ANGKATAN 2026"
            tvMemberRegu.text = "Kelas: ${member.kelas ?: "-"} • ${member.jabatan ?: "Anggota"}"
        }
    }

    override fun getItemCount() = members.size
}