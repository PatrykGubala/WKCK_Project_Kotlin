package com.example.firstapp.ui.conversations.single

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.firstapp.R
import com.example.firstapp.ui.data.Message
import com.example.firstapp.ui.data.User
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SingleConversationAdapter : ListAdapter<Message, SingleConversationAdapter.MessageViewHolder>(DiffCallback) {
    private val firestore = FirebaseFirestore.getInstance()
    private var currentPlayingMessage: Message? = null
    private var isFirstTimeLoading = true
    private var scrollToBottomListener: ScrollToBottomListener? = null

    fun setScrollToBottomListener(listener: ScrollToBottomListener) {
        scrollToBottomListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int,
    ) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message)
        private val senderTextView: TextView = itemView.findViewById(R.id.username)
        private val senderCodeTextView: TextView = itemView.findViewById(R.id.usernameCode)
        private val timeAgoTextView: TextView = itemView.findViewById(R.id.timeAgo)
        private val userImageView: ImageView = itemView.findViewById(R.id.userImage)
        private val messageImage: ImageView = itemView.findViewById(R.id.imageMessage)
        private val audioPlayerLayout: View = itemView.findViewById(R.id.audioPlayerLayout)
        private val playPauseButton: ImageView = itemView.findViewById(R.id.playPauseButton)
        private val audioSeekBar: SeekBar = itemView.findViewById(R.id.audioSeekBar)
        private val audioDurationTextView: TextView = itemView.findViewById(R.id.audioDurationTextView)

        private var mediaPlayer: MediaPlayer? = null
        private var isPlaying: Boolean = false

        init {
            playPauseButton.setOnClickListener {
                val message = getItem(adapterPosition)
                togglePlayPause(message)
            }

            audioSeekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        if (fromUser) {
                            mediaPlayer?.seekTo(progress)
                            updateSeekBar()
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }
                },
            )
        }

        fun bind(message: Message) {
            if (message.message.isNullOrEmpty()) {
                messageTextView.visibility = View.GONE
            } else {
                messageTextView.visibility = View.VISIBLE
                messageTextView.text = message.message
            }
            messageTextView.text = message.message
            timeAgoTextView.text = getRelativeTimeAgo(message.timestamp?.toDate()?.time ?: 0)

            message.senderId?.let { userId ->
                getUserDetails(userId) { user ->
                    user?.let { userDetails ->
                        Glide.with(itemView.context)
                            .load(userDetails.profileImageUrl)
                            .into(userImageView)
                        senderTextView.text = userDetails.username
                        senderCodeTextView.text = "#${userDetails.usernameCode}"
                    }
                }
            }
            if (message.messageImageUrl != null && message.messageImageUrl.isNotBlank()) {
                messageImage.visibility = View.VISIBLE

                Glide.with(itemView.context)
                    .load(message.messageImageUrl)
                    .override(800, 800)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .listener(
                        object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                if (isFirstTimeLoading) {
                                    scrollToBottomListener?.scrollToBottom()
                                    isFirstTimeLoading = false
                                }
                                return false
                            }
                        },
                    )
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(messageImage)

                Log.d(TAG, "Message height: ${itemView.height}")
            } else {
                messageImage.visibility = View.GONE
            }

            if (message.messageRecordingUrl != null && message.messageRecordingUrl.isNotBlank()) {
                audioPlayerLayout.visibility = View.VISIBLE
                setupAudioPlayer(message)
            } else {
                audioPlayerLayout.visibility = View.GONE
            }
        }

        private fun getRelativeTimeAgo(timestamp: Long): String {
            val messageCalendar =
                Calendar.getInstance().apply {
                    timeInMillis = timestamp
                }
            val currentCalendar = Calendar.getInstance()

            return if (isSameDay(messageCalendar, currentCalendar)) {
                "Dzisiaj o ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageCalendar.time)}"
            } else if (isYesterday(messageCalendar, currentCalendar)) {
                "Wczoraj o ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageCalendar.time)}"
            } else {
                SimpleDateFormat("dd/MM/yyyy 'o' HH:mm", Locale.getDefault()).format(messageCalendar.time)
            }
        }

        private fun isSameDay(
            cal1: Calendar,
            cal2: Calendar,
        ): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(
            cal1: Calendar,
            cal2: Calendar,
        ): Boolean {
            cal2.add(Calendar.DAY_OF_YEAR, -1)
            return isSameDay(cal1, cal2)
        }

        private fun setupAudioPlayer(message: Message) {
            val audioUrl = message.messageRecordingUrl

            Glide.with(itemView.context)
                .downloadOnly()
                .load(audioUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(
                    object : RequestListener<File> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<File>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: File?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<File>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            resource?.let { file ->
                                mediaPlayer =
                                    MediaPlayer().apply {
                                        setDataSource(file.path)
                                        prepare()
                                        setOnCompletionListener {
                                            stopMediaPlayer()
                                            setupAudioPlayer(message)
                                        }
                                    }
                                audioSeekBar.max = mediaPlayer?.duration ?: 0
                                audioDurationTextView.text = formatTime(mediaPlayer?.duration ?: 0)
                            }
                            return false
                        }
                    },
                )
                .submit()
        }

        private fun togglePlayPause(message: Message) {
            if (currentPlayingMessage == message) {
                if (isPlaying) {
                    pauseMediaPlayer()
                } else {
                    startMediaPlayer()
                }
            } else {
                stopCurrentMediaPlayer()
                startNewMediaPlayer(message)
            }
        }

        private fun startMediaPlayer() {
            mediaPlayer?.start()
            isPlaying = true
            updateSeekBar()
            updatePlayPauseButton()
        }

        private fun pauseMediaPlayer() {
            mediaPlayer?.pause()
            isPlaying = false
            updatePlayPauseButton()
        }

        private fun stopCurrentMediaPlayer() {
            currentPlayingMessage?.let {
                if (it != getItem(adapterPosition)) {
                    stopMediaPlayer()
                }
            }
        }

        private fun startNewMediaPlayer(message: Message) {
            stopMediaPlayer()
            currentPlayingMessage = message
            mediaPlayer =
                MediaPlayer().apply {
                    setDataSource(message.messageRecordingUrl)
                    prepare()
                    setOnCompletionListener {
                        stopMediaPlayer()
                        setupAudioPlayer(message)
                    }
                }
            audioSeekBar.max = mediaPlayer?.duration ?: 0
            audioDurationTextView.text = formatTime(mediaPlayer?.duration ?: 0)
            startMediaPlayer()
        }

        private fun stopMediaPlayer() {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            audioSeekBar.progress = 0
            updatePlayPauseButton()
        }

        private fun updatePlayPauseButton() {
            val isPlaying = mediaPlayer?.isPlaying == true
            playPauseButton.isSelected = isPlaying
        }

        private fun updateSeekBar() {
            val mediaPlayer = mediaPlayer ?: return
            if (mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                audioSeekBar.progress = currentPosition
                audioDurationTextView.text = formatTime(currentPosition)
                audioSeekBar.postDelayed(::updateSeekBar, 10)
            }
        }

        private fun formatTime(ms: Int): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    private fun getUserDetails(
        userId: String,
        callback: (User?) -> Unit,
    ) {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    companion object {
        private const val TAG = "SingleConversationAdapter"

        private val DiffCallback =
            object : DiffUtil.ItemCallback<Message>() {
                override fun areItemsTheSame(
                    oldItem: Message,
                    newItem: Message,
                ): Boolean {
                    return oldItem.messageId == newItem.messageId
                }

                override fun areContentsTheSame(
                    oldItem: Message,
                    newItem: Message,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
