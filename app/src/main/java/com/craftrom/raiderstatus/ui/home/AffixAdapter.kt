package com.craftrom.raiderstatus.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.craftrom.raiderstatus.R
import com.craftrom.raiderstatus.core.Affix

import com.craftrom.raiderstatus.databinding.DialogContentBinding
import com.craftrom.raiderstatus.databinding.ItemAffixesBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso

class AffixAdapter(private val affixes: List<Affix>) :
    RecyclerView.Adapter<AffixAdapter.AffixViewHolder>() {

    class AffixViewHolder(private val binding: ItemAffixesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(affix: Affix) {
            Picasso.get()
                .load("https://wow.zamimg.com/images/wow/icons/large/${affix.icon}.jpg")
                .into(binding.affixImage)

            binding.affixName.text = affix.name
            val affixLevel = getAffixLevel(adapterPosition)
            binding.affixLevel.text = affixLevel

            binding.root.setOnClickListener {
                val context = binding.root.context
                val dialog = createBottomSheetDialog(context, affix)
                dialog.show()
            }

        }

        private fun getAffixLevel(position: Int): String {
            return when (position) {
                0 -> "+2"
                1 -> "+7"
                2 -> "+14"
                else -> ""
            }
        }

        private fun createBottomSheetDialog(context: Context, affix: Affix): BottomSheetDialog {
            val dialog = BottomSheetDialog(context, R.style.ThemeBottomSheet)
            val dialogBinding = DialogContentBinding.inflate(LayoutInflater.from(context))
            dialog.setContentView(dialogBinding.root)

            Picasso.get()
                .load("https://wow.zamimg.com/images/wow/icons/large/${affix.icon}.jpg")
                .into(dialogBinding.dialogIcon)

            dialogBinding.dialogTitle.text = "${affix.name} (${getAffixLevel(adapterPosition)})"
            dialogBinding.dialogContentList.text = affix.description

            dialog.setCancelable(true)
            dialog.dismissWithAnimation = true
            dialog.window?.setWindowAnimations(R.style.dialog_animation_fade)
            return dialog
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AffixViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAffixesBinding.inflate(inflater, parent, false)
        return AffixViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AffixViewHolder, position: Int) {
        val affix = affixes[position]
        holder.bind(affix)
    }

    override fun getItemCount(): Int {
        return affixes.size
    }
}

