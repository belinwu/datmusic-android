/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.File
import org.threeten.bp.LocalDateTime
import tm.alashow.domain.models.BaseEntity

typealias PlaylistId = Long
typealias Playlists = List<Playlist>
typealias PlaylistsWithAudios = List<PlaylistWithAudios>

const val PLAYLIST_NAME_MAX_LENGTH = 100

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: PlaylistId = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "artwork_path")
    val artworkPath: String? = null,

    @ColumnInfo(name = "artwork_source")
    val artworkSource: String? = null,

    @ColumnInfo(name = "updated_at", defaultValue = "")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "params")
    override var params: String = "",
) : BaseEntity, LibraryItem {

    fun artworkFile() = artworkPath?.let { File(it) }

    fun updatedCopy() = copy(updatedAt = LocalDateTime.now())

    override fun getIdentifier() = id.toString()
    override fun getLabel() = name
}

typealias PlaylistAudioId = Long

@Entity(
    tableName = "playlist_audios",
    indices = [Index("playlist_id"), Index("audio_id")]
)
data class PlaylistAudio(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", defaultValue = "0")
    val id: PlaylistAudioId = 0,

    @ColumnInfo(name = "playlist_id")
    val playlistId: PlaylistId = 0,

    @ColumnInfo(name = "audio_id")
    val audioId: AudioId = "",

    @ColumnInfo(name = "position")
    val position: Int = 0,
)

data class PlaylistWithAudios(
    @Embedded
    val playlist: Playlist = Playlist(),

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistAudio::class,
            parentColumn = "playlist_id",
            entityColumn = "audio_id"
        )
    )
    val audios: List<Audio> = emptyList(),
)

data class PlaylistItem(
    @Embedded
    val playlistAudio: PlaylistAudio = PlaylistAudio(),

    @Relation(
        parentColumn = "audio_id",
        entityColumn = "id"
    )
    val audio: Audio = Audio()
)

typealias PlaylistAudios = List<AudioOfPlaylist>
typealias PlaylistItems = List<PlaylistItem>

data class AudioOfPlaylist(
    @Embedded
    val audio: Audio = Audio(),

    val position: Int,
)
