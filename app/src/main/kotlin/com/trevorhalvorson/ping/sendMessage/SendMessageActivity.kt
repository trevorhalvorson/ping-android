package com.trevorhalvorson.ping.sendMessage

import android.app.AlertDialog
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.*
import com.google.android.libraries.remixer.annotation.*
import com.google.android.libraries.remixer.ui.view.RemixerFragment
import com.trevorhalvorson.ping.BuildConfig
import com.trevorhalvorson.ping.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_send_message.*
import kotlinx.android.synthetic.main.layout_number_pad.*
import javax.inject.Inject

class SendMessageActivity : AppCompatActivity(), SendMessageContract.View, View.OnClickListener {

    @Inject lateinit var sendMessagePresenter: SendMessageContract.Presenter

    override fun setPresenter(presenter: SendMessageContract.Presenter) {
        sendMessagePresenter = presenter
    }

    override fun showProgress() {
        progressDialog = ProgressDialog.show(this, getString(R.string.progress_dialog_title_text),
                getString(R.string.progress_dialog_message_text), true, false)
    }

    override fun hideProgress() {
        progressDialog?.dismiss()
    }

    override fun showError(error: String?) {
        if (error != null) {
            errorDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.send_message_error_dialog_title_text))
                    .setMessage(error)
                    .create()
            errorDialog?.show()
        }
    }

    override fun hideError() {
        errorDialog?.dismiss()
    }

    override fun clearInput() {
        number_edit_text.text.clear()
    }

    private var progressDialog: ProgressDialog? = null
    private var errorDialog: AlertDialog? = null

    private var message = BuildConfig.MESSAGE
    private var imageUrl = BuildConfig.IMAGE_URL
    private var pin = BuildConfig.PIN
    private var blankClickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)
        AndroidInjection.inject(this)

        hideSystemUI()

        send_button.setOnClickListener {
            sendMessagePresenter.sendMessage(message)
        }

        number_edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                send_button.isEnabled = Patterns.PHONE.matcher(p0).matches() &&
                        !PhoneNumberUtils.isEmergencyNumber(p0.toString())
            }
        })

        num_pad_1.setOnClickListener(this)
        num_pad_2.setOnClickListener(this)
        num_pad_3.setOnClickListener(this)
        num_pad_4.setOnClickListener(this)
        num_pad_5.setOnClickListener(this)
        num_pad_6.setOnClickListener(this)
        num_pad_7.setOnClickListener(this)
        num_pad_8.setOnClickListener(this)
        num_pad_9.setOnClickListener(this)
        num_pad_0.setOnClickListener(this)

        num_pad_del.setOnClickListener {
            if (number_edit_text.text.isNotEmpty()) {
                number_edit_text.text.delete(number_edit_text.text.length - 1,
                        number_edit_text.text.length)
            }
        }
        num_pad_del.setOnLongClickListener {
            clearInput()
            true
        }

        num_pad_blank.setOnClickListener {
            // TODO: launch dialog and require PIN before show remix fragment
            if (blankClickCount++ == 7) {
                RemixerFragment.newInstance().attachToButton(this, num_pad_blank)
                blankClickCount = 0
            }
        }

        RemixerBinder.bind(this)
    }

    override fun onClick(p0: View?) {
        number_edit_text.text.append((p0 as TextView).text)
    }

    override fun onBackPressed() {
        // prevent user from exiting app via the back button
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                .or(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                .or(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                .or(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                .or(View.SYSTEM_UI_FLAG_FULLSCREEN)
                .or(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    // Remixer UI controls

    @StringVariableMethod(title = "Title Text", initialValue = BuildConfig.TITLE_TEXT)
    fun setTitleText(text: String?) {
        if (text != null) {
            title_text.text = text
        }
    }

    @RangeVariableMethod(title = "Title Text Size", initialValue = BuildConfig.TITLE_TEXT_SIZE,
            minValue = 12F, maxValue = 120F)
    fun setTitleTextSize(textSize: Float?) {
        if (textSize != null) {
            title_text.textSize = textSize
        }
    }

    // TODO: find a cleaner way to add colors on these methods
    @ColorListVariableMethod(title = "Title Text Color",
            initialValue = BuildConfig.TITLE_TEXT_COLOR, limitedToValues = intArrayOf(
            0xFF000000.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFFFFFFFF.toInt()
    ))
    fun setTitleTextColor(color: Int?) {
        if (color != null) {
            title_text.setTextColor(color)
        }
    }

    @StringVariableMethod(title = "Image URL", initialValue = BuildConfig.IMAGE_URL)
    fun setLogoImageUrl(url: String?) {
        if (!TextUtils.isEmpty(url)) {
            imageUrl = url!!
            Glide.with(this).load(imageUrl).apply(centerCropTransform()).into(logo_image)
        }
    }

    @RangeVariableMethod(title = "Image Width", initialValue = BuildConfig.IMAGE_WIDTH,
            minValue = 0F, maxValue = 1200F)
    fun setLogoImageWidth(width: Float?) {
        if (width != null) {
            logo_image.layoutParams.width = width.toInt()
            logo_image.requestLayout()
            Glide.with(this).clear(logo_image)
            Glide.with(this).load(imageUrl).into(logo_image)
        }
    }

    @RangeVariableMethod(title = "Image Height", initialValue = BuildConfig.IMAGE_HEIGHT,
            minValue = 0F, maxValue = 1200F)
    fun setLogoImageHeight(height: Float?) {
        if (height != null) {
            logo_image.layoutParams.height = height.toInt()
            logo_image.requestLayout()
            Glide.with(this).clear(logo_image)
            Glide.with(this).load(imageUrl).into(logo_image)
        }
    }

    @StringListVariableMethod(title = "Image Scale Type",
            initialValue = BuildConfig.IMAGE_SCALE_TYPE, limitedToValues = arrayOf(
            "CENTER",
            "CENTER_CROP",
            "CENTER_INSIDE",
            "FIT_CENTER",
            "FIT_XY"
    ))
    fun setLogoImageScaleType(type: String?) {
        val scaleType: ImageView.ScaleType? = when (type) {
            "CENTER" -> ImageView.ScaleType.CENTER
            "CENTER_CROP" -> ImageView.ScaleType.CENTER_CROP
            "CENTER_INSIDE" -> ImageView.ScaleType.CENTER_INSIDE
            "FIT_CENTER" -> ImageView.ScaleType.FIT_CENTER
            "FIT_XY" -> ImageView.ScaleType.FIT_XY
            else -> {
                ImageView.ScaleType.CENTER
            }
        }
        logo_image.scaleType = scaleType
        logo_image.requestLayout()
        Glide.with(this).clear(logo_image)
        Glide.with(this).load(imageUrl).into(logo_image)
    }

    @StringVariableMethod(title = "Copy Text", initialValue = BuildConfig.COPY_TEXT)
    fun setCopyText(text: String?) {
        if (text != null) {
            copy_text.text = text
        }
    }

    @RangeVariableMethod(title = "Copy Text Size", initialValue = BuildConfig.COPY_TEXT_SIZE,
            minValue = 12F, maxValue = 120F)
    fun setCopyTextSize(textSize: Float?) {
        if (textSize != null) {
            copy_text.textSize = textSize
        }
    }

    @ColorListVariableMethod(title = "Copy Text Color", initialValue = BuildConfig.COPY_TEXT_COLOR,
            limitedToValues = intArrayOf(
                    0xFF000000.toInt(),
                    0xFFE91E63.toInt(),
                    0xFF9C27B0.toInt(),
                    0xFF673AB7.toInt(),
                    0xFF3F51B5.toInt(),
                    0xFF2196F3.toInt(),
                    0xFF03A9F4.toInt(),
                    0xFF00BCD4.toInt(),
                    0xFF009688.toInt(),
                    0xFF4CAF50.toInt(),
                    0xFF8BC34A.toInt(),
                    0xFFFFEB3B.toInt(),
                    0xFFFFC107.toInt(),
                    0xFF8BC34A.toInt(),
                    0xFFFF9800.toInt(),
                    0xFFFF5722.toInt(),
                    0xFF9E9E9E.toInt(),
                    0xFF607D8B.toInt(),
                    0xFF424242.toInt(),
                    0xFF37474F.toInt(),
                    0xFF212121.toInt(),
                    0xFF263238.toInt(),
                    0xFFFFFFFF.toInt()
            ))
    fun setCopyTextColor(color: Int?) {
        if (color != null) {
            copy_text.setTextColor(color)
        }
    }

    @StringVariableMethod(title = "Send Button Text", initialValue = BuildConfig.SEND_BUTTON_TEXT)
    fun setSendButtonText(text: String?) {
        if (text != null) {
            send_button.text = text
        }
    }

    @ColorListVariableMethod(title = "Send Button Text Color",
            initialValue = BuildConfig.SEND_BUTTON_TEXT_COLOR, limitedToValues = intArrayOf(
            0xFF000000.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFFFFFFFF.toInt()
    ))
    fun setSendButtonTextColor(color: Int?) {
        if (color != null) {
            send_button.setTextColor(color)
        }
    }

    @ColorListVariableMethod(title = "Send Message Button Color",
            initialValue = BuildConfig.SEND_BUTTON_BACKGROUND_COLOR, limitedToValues = intArrayOf(
            0xFFCCCCCC.toInt(),
            0xFF000000.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFFFFFFFF.toInt()
    ))
    fun setSendButtonBackgroundColor(color: Int?) {
        if (color != null) {
            send_button.setBackgroundColor(color)
        }
    }

    @ColorListVariableMethod(title = "Phone Number Background Color",
            initialValue = BuildConfig.PHONE_INPUT_BACKGROUND_COLOR, limitedToValues = intArrayOf(
            0xFF000000.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFFFFFFFF.toInt()
    ))
    fun setPhoneNumberBackgroundTextColor(color: Int?) {
        if (color != null) {
            number_text_input_layout.setBackgroundColor(color)
        }
    }

    @ColorListVariableMethod(title = "Phone Number Input Text Color",
            initialValue = BuildConfig.PHONE_INPUT_TEXT_COLOR, limitedToValues = intArrayOf(
            0xFF000000.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFFFFFFFF.toInt()
    ))
    fun setPhoneNumberInputTextColor(color: Int?) {
        if (color != null) {
            number_edit_text.setTextColor(color)
        }
    }

    @ColorListVariableMethod(title = "Number Pad Text Color",
            initialValue = BuildConfig.NUM_PAD_TEXT_COLOR, limitedToValues = intArrayOf(
            0xFF000000.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFFFFFFFF.toInt()
    ))
    fun setPhoneNumberPadTextColor(color: Int?) {
        if (color != null) {
            num_pad_1.setTextColor(color)
            num_pad_2.setTextColor(color)
            num_pad_3.setTextColor(color)
            num_pad_4.setTextColor(color)
            num_pad_5.setTextColor(color)
            num_pad_6.setTextColor(color)
            num_pad_7.setTextColor(color)
            num_pad_8.setTextColor(color)
            num_pad_9.setTextColor(color)
            num_pad_0.setTextColor(color)
            num_pad_del.setTextColor(color)
        }
    }

    @ColorListVariableMethod(title = "Number Pad Background Color",
            initialValue = BuildConfig.NUM_PAD_BACKGROUND_COLOR, limitedToValues = intArrayOf(
            0xFFFFFFFF.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFF000000.toInt()
    ))
    fun setPhoneNumberPadBackgroundColor(color: Int?) {
        if (color != null && layout_number_pad != null) {
            layout_number_pad.setBackgroundColor(color)
        }
    }

    @ColorListVariableMethod(title = "Main Background Color",
            initialValue = BuildConfig.BACKGROUND_COLOR, limitedToValues = intArrayOf(
            0xFFFFFFFF.toInt(),
            0xFFE91E63.toInt(),
            0xFF9C27B0.toInt(),
            0xFF673AB7.toInt(),
            0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(),
            0xFF03A9F4.toInt(),
            0xFF00BCD4.toInt(),
            0xFF009688.toInt(),
            0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFFEB3B.toInt(),
            0xFFFFC107.toInt(),
            0xFF8BC34A.toInt(),
            0xFFFF9800.toInt(),
            0xFFFF5722.toInt(),
            0xFF9E9E9E.toInt(),
            0xFF607D8B.toInt(),
            0xFF424242.toInt(),
            0xFF37474F.toInt(),
            0xFF212121.toInt(),
            0xFF263238.toInt(),
            0xFF000000.toInt()
    ))
    fun setBackgroundColor(color: Int?) {
        if (color != null) {
            container_linear_layout.setBackgroundColor(color)
        }
    }

    @StringVariableMethod(title = "Message", initialValue = BuildConfig.MESSAGE)
    fun setMessageText(s: String?) {
        if (s != null) {
            message = s
        }
    }

    @StringVariableMethod(title = "PIN", initialValue = BuildConfig.PIN)
    fun setPin(s: String?) {
        if (s != null) {
            pin = s
        }
    }
}
