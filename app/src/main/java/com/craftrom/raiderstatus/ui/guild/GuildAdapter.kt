package com.craftrom.raiderstatus.ui.guild

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.craftrom.raiderstatus.R
import com.craftrom.raiderstatus.core.Guild
import com.craftrom.raiderstatus.databinding.ItemGuildBinding

class GuildAdapter(private val emptyHelp: TextView, private val guildRecyclerView: RecyclerView) : RecyclerView.Adapter<GuildAdapter.GuildViewHolder>() {

    private val guilds: MutableList<Guild> = mutableListOf()

    init {
        updateEmptyState()
    }

    fun setGuilds(guildList: List<Guild>) {
        guilds.clear()
        guilds.addAll(guildList)
        updateEmptyState()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuildViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGuildBinding.inflate(inflater, parent, false)
        return GuildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuildViewHolder, position: Int) {
        val guild = guilds[position]
        holder.bind(guild)
    }

    override fun getItemCount(): Int {
        return guilds.size
    }

    inner class GuildViewHolder(private val binding: ItemGuildBinding) : RecyclerView.ViewHolder(binding.root) {
        private val guildNameTextView: TextView = binding.nameTextView
        private val guildDescriptionTextView: TextView = binding.factionTextView
        private val realmTextView: TextView = binding.realmTextView
        private val layoutParams = binding.block3.layoutParams as LinearLayout.LayoutParams

        fun bind(guild: Guild) {
            guildNameTextView.text = guild.name
            realmTextView.text = guild.realm
            guildDescriptionTextView.text = guild.faction

            val foregroundCardColor = when {
                guild.faction.equals("alliance", ignoreCase = true) -> ContextCompat.getColor(binding.root.context, R.color.ALLIANCE_CARD)
                guild.faction.equals("horde", ignoreCase = true) -> ContextCompat.getColor(binding.root.context, R.color.HORDE_CARD)
                else -> ContextCompat.getColor(binding.root.context, android.R.color.transparent)
            }

            val colorStateList = ColorStateList.valueOf(foregroundCardColor)
            binding.cardGuildItem.setCardForegroundColor(colorStateList)

            when (adapterPosition) {
                0 -> {
                    binding.guildImage.setImageResource(R.drawable.honorablemention_rank1)
                    binding.block2.visibility = View.VISIBLE
                    layoutParams.weight = 2f
                }
                1 -> {
                    binding.guildImage.setImageResource(R.drawable.honorablemention_rank2)
                    binding.block2.visibility = View.VISIBLE
                    layoutParams.weight = 2f
                }
                else -> {
                    binding.guildImage.setImageDrawable(null)
                    binding.block2.visibility = View.GONE
                    layoutParams.weight = 5f
                }
            }
        }

    }

    private fun updateEmptyState() {
        if (guilds.isEmpty()) {
            emptyHelp.visibility = View.VISIBLE
            guildRecyclerView.visibility = View.GONE
        } else {
            emptyHelp.visibility = View.GONE
            guildRecyclerView.visibility = View.VISIBLE
        }
    }
}
