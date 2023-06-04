package com.craftrom.raiderstatus.core

import com.google.gson.annotations.SerializedName

data class AffixResponse(
    val affix_details: List<Affix>
)

data class Affix(
    val name: String,
    val icon: String,
    val description: String,
    val wowhead_url: String
)

data class Period(
    val region: String,
    val previous: PeriodDetails,
    val current: PeriodDetails,
    val next: PeriodDetails
)

data class PeriodDetails(
    val period: Int,
    val start: String,
    val end: String
)

data class PeriodsResponse(
    val periods: List<Period>
)
data class GearData(
    val updated_at: String,
    val item_level_equipped: Int,
    val item_level_total: Int,
    val artifact_traits: Int,
    val corruption: CorruptionData,
    val items: GearItemsData
)

data class CorruptionData(
    val added: Int,
    val resisted: Int,
    val total: Int,
    val cloakRank: Int,
    val spells: List<Any>
)

data class GearItemsData(
    val head: GearItemData,
    val neck: GearItemData,
    val shoulder: GearItemData,
    val back: GearItemData,
    val chest: GearItemData,
    val waist: GearItemData,
    val shirt: GearItemData,
    val wrist: GearItemData,
    val hands: GearItemData,
    val legs: GearItemData,
    val feet: GearItemData,
    val finger1: GearItemData,
    val finger2: GearItemData,
    // Додайте решту полів згідно наданих даних
)

data class GearItemData(
    val item_id: Int,
    val item_level: Int,
    val icon: String,
    val name: String,
    val item_quality: Int,
    val is_legendary: Boolean,
    val is_azerite_armor: Boolean,
    val azerite_powers: List<AzeritePowerData>,
    val corruption: CorruptionData,
    val domination_shards: List<Any>,
    val gems: List<Any>,
    val bonuses: List<Int>,
    val tier: String? = null // Додайте nullable поле tier
)

data class AzeritePowerData(
    val id: Int,
    val spell: AzeriteSpellData,
    val tier: Int
)

data class AzeriteSpellData(
    val id: Int,
    val school: Int,
    val icon: String,
    val name: String,
    val rank: Any? // Використайте тип даних, який підходить для rank (може бути Int?)
)

data class CharacterData(
    val name: String,
    val race: String,
    @SerializedName("class")
    val charClass: String,
    val active_spec_name: String,
    val active_spec_role: String,
    val gender: String,
    val faction: String,
    val achievement_points: Int,
    val honorable_kills: Int,
    val thumbnail_url: String,
    val region: String,
    val realm: String,
    val last_crawled_at: String,
    val profile_url: String,
    val profile_banner: String,
    val mythic_plus_scores_by_season: List<MythicPlusScoresBySeasonData>,
    val gear: GearData,
    val guild: GuildData,
    val raid_progression: RaidProgressionData
)

data class RaidProgressionData(
    @SerializedName("aberrus-the-shadowed-crucible")
    val aberrus_the_shadowed_crucible: RaidData,
    @SerializedName("vault-of-the-incarnates")
    val vault_of_the_incarnates: RaidData
)

data class RaidData(
    val summary: String,
    val total_bosses: Int,
    val normal_bosses_killed: Int,
    val heroic_bosses_killed: Int,
    val mythic_bosses_killed: Int,
)

data class GuildData(
    val name: String,
    val realm: String
)

data class MythicPlusScoresBySeasonData(
    val season: String,
    val scores: MythicPlusScoresData,
    val segments: MythicPlusSegmentsData
)

data class MythicPlusScoresData(
    val all: Double,
    val dps: Double,
    val healer: Double,
    val tank: Double
)

data class MythicPlusSegmentsData(
    val all: MythicPlusSegmentData,
    val dps: MythicPlusSegmentData,
    val healer: MythicPlusSegmentData,
    val tank: MythicPlusSegmentData,
    val spec_0: MythicPlusSegmentData,
    val spec_1: MythicPlusSegmentData,
    val spec_2: MythicPlusSegmentData,
    val spec_3: MythicPlusSegmentData
)

data class MythicPlusSegmentData(
    val score: Double,
    val color: String
)




