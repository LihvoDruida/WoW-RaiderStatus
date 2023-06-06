package com.craftrom.raiderstatus.ui.character

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.craftrom.raiderstatus.MainActivity
import com.craftrom.raiderstatus.R
import com.craftrom.raiderstatus.core.CharacterData
import com.craftrom.raiderstatus.core.database.CharacterDatabaseHelper
import com.craftrom.raiderstatus.databinding.DialogCharacterBinding
import com.craftrom.raiderstatus.databinding.FragmentCharacterBinding
import com.craftrom.raiderstatus.databinding.ItemCharacterBinding
import com.craftrom.raiderstatus.utils.CharacterDataFetcher
import com.craftrom.raiderstatus.utils.Constants.getSeasonName
import com.craftrom.raiderstatus.utils.Constants.isInternetAvailable
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(DelicateCoroutinesApi::class)
class CharacterFragment : Fragment() {

    private lateinit var characterList: MutableList<Character>
    private var _binding: FragmentCharacterBinding? = null
    private val binding get() = _binding!!
    private lateinit var characterAdapter: CharacterAdapter
    lateinit var characterDatabaseHelper: CharacterDatabaseHelper


    override fun onResume() {
        super.onResume()
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterBinding.inflate(inflater, container, false)
        val view = binding.root
        val internetAvailable = isInternetAvailable(requireContext())
        characterDatabaseHelper = CharacterDatabaseHelper(requireContext())

        characterList = mutableListOf()

        binding.characterRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            characterAdapter = CharacterAdapter(characterList)
            adapter = characterAdapter
        }

        // Restore character list from SQLite
        characterList.addAll(characterDatabaseHelper.loadCharacters())
        characterAdapter.notifyDataSetChanged()

        binding.fab.setOnClickListener {
            val context = binding.root.context
            val dialog = createBottomSheetDialog(context)
            dialog.show()
        }

        if (internetAvailable) {
            fetchChar()
        }

        return view
    }
    private fun createBottomSheetDialog(context: Context): BottomSheetDialog {
        val dialog = BottomSheetDialog(context, R.style.ThemeBottomSheet)
        val dialogBinding = DialogCharacterBinding.inflate(LayoutInflater.from(context))
        val internetAvailable = isInternetAvailable(requireContext())
        dialog.setContentView(dialogBinding.root)

        updateButtonState(dialogBinding) // Оновлюємо стан кнопки після встановлення слухача

        dialogBinding.nameEditText.addTextChangedListener {
            updateButtonState(dialogBinding) // Оновлюємо стан кнопки після зміни тексту в полі name
        }

        dialogBinding.realmEditText.addTextChangedListener {
            updateButtonState(dialogBinding) // Оновлюємо стан кнопки після зміни тексту в полі realm
        }

        dialogBinding.addButton.setOnClickListener {
            val name = dialogBinding.nameEditText.text.toString()
            val realm = dialogBinding.realmEditText.text.toString()
            val region = dialogBinding.regionSpinner.selectedItem.toString()
            dialogBinding.nameEditText.requestFocus()
            val character = Character(name, realm, region)

            if (name.isNotBlank() && realm.isNotBlank()) {
                characterDatabaseHelper.saveCharacter(character)

                // Додати персонажа до списку та оновити адаптер
                characterList.add(character)
                characterAdapter.notifyItemInserted(characterList.size - 1)

                dialog.dismiss()
                updateCharacterList()
                if (internetAvailable) {
                    fetchChar()
                }
            }
        }

        dialog.setCancelable(true)
        dialog.dismissWithAnimation = true

        return dialog
    }

    private fun updateCharacterList() {
        val characters = characterDatabaseHelper.loadCharacters()
        characterList.clear()
        characterList.addAll(characters)
        characterAdapter.notifyDataSetChanged()
    }


    private fun updateButtonState(dialogBinding: DialogCharacterBinding) {
        val name = dialogBinding.nameEditText.text.toString()
        val realm = dialogBinding.realmEditText.text.toString()

        val isButtonEnabled = name.isNotBlank() && realm.isNotBlank()
        dialogBinding.addButton.isEnabled = isButtonEnabled
    }



    data class Character(
        val name: String,
        val realm: String,
        val region: String,
        val characterData: CharacterData? = null
    )

    private class CharacterAdapter(
        private val characterList: MutableList<Character>
    ) : RecyclerView.Adapter<CharacterAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemCharacterBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val deleteButton: Button = binding.delete
            val screenshotButton: Button = binding.screenshot
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val character = characterList[position]
            val binding = holder.binding
            val context = binding.root.context
            val season = character.characterData?.mythic_plus_scores_by_season?.firstOrNull()?.season
            val itemLevelEquipped = character.characterData?.gear?.item_level_equipped ?: 0
            val characterDatabaseHelper = CharacterDatabaseHelper(context)
            val alpha = 64 // Значення прозорості (від 0 до 255, де 0 - повна прозорість, 255 - повна непрозорість)

            val dividerBgColor = when (character.characterData?.faction) {
                "alliance" -> {
                    val color = ContextCompat.getColor(context, R.color.ALLIANCE)
                    Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
                }
                "horde" -> {
                    val color = ContextCompat.getColor(context, R.color.HORDE)
                    Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
                }
                else -> {
                    Color.TRANSPARENT
                }
            }

            val foregroundCardColor = when (character.characterData?.faction) {
                "alliance" -> ContextCompat.getColor(context, R.color.ALLIANCE_CARD)
                "horde" -> ContextCompat.getColor(context, R.color.HORDE_CARD)
                else -> ContextCompat.getColor(context, android.R.color.transparent)
            }
            val classColor = when (character.characterData?.charClass) {
                "Death Knight" -> ContextCompat.getColor(context, R.color.DEATHKNIGHT)
                "Demon Hunter" -> ContextCompat.getColor(context, R.color.DEMONHUNTER)
                "Druid" -> ContextCompat.getColor(context, R.color.DRUID)
                "Evoker" -> ContextCompat.getColor(context, R.color.EVOKER)
                "Hunter" -> ContextCompat.getColor(context, R.color.HUNTER)
                "Mage" -> ContextCompat.getColor(context, R.color.MAGE)
                "Monk" -> ContextCompat.getColor(context, R.color.MONK)
                "Paladin" -> ContextCompat.getColor(context, R.color.PALADIN)
                "Priest" -> ContextCompat.getColor(context, R.color.PRIEST)
                "Rogue" -> ContextCompat.getColor(context, R.color.ROGUE)
                "Shaman" -> ContextCompat.getColor(context, R.color.SHAMAN)
                "Warlock" -> ContextCompat.getColor(context, R.color.WARLOCK)
                "Warrior" -> ContextCompat.getColor(context, R.color.WARRIOR)
                else -> ContextCompat.getColor(context, R.color.ALLIANCE)
            }
            val factionName = when (character.characterData?.faction) {
                "alliance" -> context.getString(R.string.alliance)
                "horde" -> context.getString(R.string.horde)
                else -> context.getString(R.string.unknown_faction)
            }
            val regionName = when (character.characterData?.region) {
                "us" -> "US"
                "eu" -> "EU"
                "tw" -> "TW"
                "kr" -> "KR"
                "cn" -> "CN"
                else -> ""
            }

            val colorStateList = ColorStateList.valueOf(foregroundCardColor)
            binding.cardCharItem.setCardForegroundColor(colorStateList)

            Picasso.get()
                .load(character.characterData?.thumbnail_url)
                .error(R.drawable.ic_logo)
                .into(binding.charAvatar)
            binding.seasonTitle.text = getSeasonName(season.toString())
            binding.charName.text = character.characterData?.name ?: "-"
            binding.charName.setTextColor(classColor)
            binding.charRegion.text = regionName
            binding.charFaction.text = factionName
            binding.raceText.text = character.characterData?.race ?: "-"
            binding.classText.text =
                "${character.characterData?.charClass ?: "-"} (${character.characterData?.active_spec_name ?: "-"})"
            binding.guildText.text = "<${character.characterData?.guild?.name}>"
            binding.raids1Name.setText(R.string.raids_one)
            binding.raids1Score.text =
                character.characterData?.raid_progression?.aberrus_the_shadowed_crucible?.summary
                    ?: "-"
            binding.raids2Name.setText(R.string.raids_two)
            binding.raids2Score.text =
                character.characterData?.raid_progression?.vault_of_the_incarnates?.summary ?: "-"
            binding.charAchievement.text = character.characterData?.achievement_points.toString()
            binding.charGear.text = itemLevelEquipped.toString()
            val colorResId = when {
                itemLevelEquipped < 300 -> R.color.UNCOMMON
                itemLevelEquipped < 400 -> R.color.RARE
                else -> R.color.EPIC
            }

            val textColor = ContextCompat.getColor(context, colorResId)
            binding.charGear.setTextColor(textColor)
            binding.charMscore.text =
                (character.characterData?.mythic_plus_scores_by_season?.find { it.season == season }?.segments?.all?.score
                    ?: "-").toString()
            val mColor = character.characterData?.mythic_plus_scores_by_season?.find { it.season == season }?.segments?.all?.color
            if (mColor != null && mColor.isNotEmpty()) {
                val colorInt = Color.parseColor(mColor)
                binding.charMscore.setTextColor(colorInt)
            }
            binding.charDivider.setBackgroundColor(dividerBgColor)

            holder.deleteButton.setOnClickListener {
                val index = holder.adapterPosition
                if (index != RecyclerView.NO_POSITION) {
                    characterList.removeAt(index)
                    notifyItemRemoved(index)

                    // Видалити персонажа з бази даних SQLite
                    characterDatabaseHelper.deleteCharacter(character.name)

                    Toast.makeText(context, "Персонаж видалено", Toast.LENGTH_SHORT).show()
                }
            }

            holder.screenshotButton.setOnClickListener {
                // Set the hardware layer type to enable the animation
                binding.cardCharItem.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                // Define the alpha animation
                val alphaAnimator = ObjectAnimator.ofFloat(binding.cardCharItem, "alpha", .5f, 1f).apply {
                    duration = 200 // Set the duration of each half of the blink effect (in milliseconds)
                    repeatCount = 1 // Set the number of times the animation should repeat (adjust as desired)
                    repeatMode = ValueAnimator.REVERSE // Reverse the animation to create the blink effect
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            // Reset the layer type to none after the animation ends
                            binding.cardCharItem.setLayerType(View.LAYER_TYPE_NONE, null)
                            // Make the cardCharItem visible again
                            binding.cardCharItem.alpha = 1f
                        }
                    })
                }
                // Start the alpha animation
                alphaAnimator.start()


                val screenshot = takeScreenshotOfView(binding.cardCharItem)
                saveScreenshotToGallery(context, screenshot)
            }
        }

        override fun getItemCount(): Int {
            return characterList.size
        }

        private fun takeScreenshotOfView(view: View): Bitmap {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }

        private fun saveScreenshotToGallery(context: Context, bitmap: Bitmap) {
            val savedImageURL = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "screenshot_${System.currentTimeMillis()}",
                "Screenshot"
            )
            if (savedImageURL != null) {
                Toast.makeText(context, "Screenshot saved to gallery", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun fetchChar(){
        // Використовуємо корутину для виконання запитів паралельно
        GlobalScope.launch(Dispatchers.Main) {
            characterList.forEachIndexed { index, character ->
                val characterData = fetchCharacterData(character)
                characterList[index] = character.copy(characterData = characterData)
                characterAdapter.notifyItemChanged(index)
            }
        }
    }
    private suspend fun fetchCharacterData(character: Character): CharacterData? {
        return suspendCoroutine { continuation ->
            CharacterDataFetcher(character).fetchCharacterData { characterData ->
                continuation.resume(characterData)
            }
        }
    }

    private fun getTitle() = getString(R.string.title_character)
    private fun getSubtitle() = getString(R.string.subtitle_character)

    override fun onDestroyView() {
        super.onDestroyView()
        characterDatabaseHelper.close()
        _binding = null
    }
}
