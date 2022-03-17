package dev.simpletimer.audio_player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.audio.AudioInformationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.AudioChannel
import net.dv8tion.jda.api.entities.Guild
import java.nio.ByteBuffer


/**
 * ギルドのオーディオを動かしたりする
 *
 * @property guild 対象の[Guild]
 */
class GuildAudioPlayer(val guild: Guild) {
    //AudioManager
    private val audioPlayerManager = SimpleTimer.instance.audioManager

    //プレイヤー
    private val player = audioPlayerManager.playerManager.createPlayer()

    //JDAのAudioManager
    private val audioManager = guild.audioManager.apply {

        //SendingHandlerを設定
        sendingHandler = object : AudioSendHandler {
            //前回のフレーム
            private var lastFrame: AudioFrame? = null

            override fun canProvide(): Boolean {
                //フレームを取得
                lastFrame = player.provide()
                //nullかを返す
                return lastFrame != null
            }

            override fun provide20MsAudio(): ByteBuffer {
                //フレームのデータをByteに変換して返す
                return ByteBuffer.wrap(lastFrame!!.data)
            }

            override fun isOpus(): Boolean {
                //常にtrueを返す
                return true
            }
        }

    }

    fun isConnected(): Boolean {
        return audioManager.isConnected
    }

    /**
     * チャンネルに接続をする
     *
     * @param channel 接続する[AudioChannel]
     */
    fun connect(channel: AudioChannel) {
        //接続
        audioManager.openAudioConnection(channel)
    }

    /**
     * チャンネルから抜ける
     *
     */
    fun disconnect() {
        //接続しているかを確認
        if (!isConnected()) return

        //切断
        audioManager.closeAudioConnection()
    }

    /**
     * オーディオを再生
     *
     * @param audioData [AudioInformationData]
     */
    fun play(audioData: AudioInformationData) {
        //接続しているかを確認
        if (!isConnected()) return

        //コールーチンに持ってく
        CoroutineScope(Dispatchers.Default).launch {
            //音源を読み込み
            audioPlayerManager.playerManager.loadItemOrdered(
                player,
                audioData.file,
                object : AudioLoadResultHandler {
                    //トラックの読み込み
                    override fun trackLoaded(track: AudioTrack) {
                        //再生
                        player.playTrack(track)
                    }

                    override fun playlistLoaded(playlist: AudioPlaylist) {
                        //プレイリストはないので何もしない
                    }
                    override fun noMatches() {
                        //なにもしない
                    }
                    override fun loadFailed(exception: FriendlyException) {
                        //エラー無視
                    }
                })
        }
    }
}