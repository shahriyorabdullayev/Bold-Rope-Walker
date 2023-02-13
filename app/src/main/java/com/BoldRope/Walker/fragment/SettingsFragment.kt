package com.BoldRope.Walker.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.BoldRope.Walker.R
import com.BoldRope.Walker.databinding.FragmentSettingsBinding
import com.BoldRope.Walker.utils.*
import com.BoldRope.Walker.utils.Constants.EN
import com.BoldRope.Walker.utils.Constants.KEY_LANGUAGE
import com.BoldRope.Walker.utils.Constants.KEY_SOUND
import com.BoldRope.Walker.utils.Constants.KEY_VIBRATION
import com.BoldRope.Walker.utils.Constants.RU
import com.BoldRope.Walker.utils.SoundService
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.reword.Reword
import java.util.*


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding by viewBinding { FragmentSettingsBinding.bind(it) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        soundOnOff(getBoolean(KEY_SOUND))
        vibrationOnOff(getBoolean(KEY_VIBRATION))
        manageLanguage(getString(KEY_LANGUAGE))

        binding.apply {

            btnBack.setOnClickListener {
                vibrator()
                requireActivity().onBackPressed()
            }

            btnMusicOn.setOnClickListener {
                if (getBoolean(KEY_VIBRATION)) {
                    if (!getBoolean(KEY_SOUND)) {
                        vibrator()
                        soundOnOff(true)
                        saveBoolean(KEY_SOUND, true)
                        requireActivity().startService(Intent(requireContext(),
                            SoundService::class.java))
                    }
                } else {
                    if (!getBoolean(KEY_SOUND)) {
                        saveBoolean(KEY_SOUND, true)
                        soundOnOff(true)
                        requireActivity().startService(Intent(requireContext(),
                            SoundService::class.java))
                    }
                }
            }

            btnMusicOff.setOnClickListener {
                if (getBoolean(KEY_VIBRATION)) {
                    vibrator()
                    soundOnOff(false)
                    saveBoolean(KEY_SOUND, false)
                    requireActivity().stopService(Intent(requireContext(),
                        SoundService::class.java))
                } else {
                    saveBoolean(KEY_SOUND, false)
                    soundOnOff(false)
                    requireActivity().stopService(Intent(requireContext(),
                        SoundService::class.java))
                }
            }

            btnVibrOn.setOnClickListener {
                saveBoolean(KEY_VIBRATION, true)
                vibrationOnOff(true)
            }

            btnVibrOff.setOnClickListener {
                saveBoolean(KEY_VIBRATION, false)
                vibrationOnOff(false)
            }

            btnEn.setOnClickListener {
                if (getBoolean(KEY_VIBRATION)) {
                    vibrator()
                    saveString(KEY_LANGUAGE, EN)
                    manageLanguage(EN)
                    AppLocale.desiredLocale = Locale.ENGLISH
                    Reword.reword(binding.root)
                } else {
                    saveString(KEY_LANGUAGE, EN)
                    manageLanguage(EN)
                    AppLocale.desiredLocale = Locale.ENGLISH
                    Reword.reword(binding.root)

                }

            }

            btnRu.setOnClickListener {
                if (getBoolean(KEY_VIBRATION)) {
                    vibrator()
                    saveString(KEY_LANGUAGE, RU)
                    manageLanguage(RU)
                    AppLocale.desiredLocale = Locale(RU)
                    Reword.reword(binding.root)

                } else {
                    saveString(KEY_LANGUAGE, RU)
                    manageLanguage(RU)
                    AppLocale.desiredLocale = Locale(RU)
                    Reword.reword(binding.root)

                }
            }


        }



    }

    private fun soundOnOff(isOn: Boolean) {
        if (isOn) {
            binding.apply {
                btnMusicOn.setImageResource(R.drawable.ic_music_on_selected)
                btnMusicOn.setBackgroundResource(R.drawable.bg_icons_selected)

                btnMusicOff.setImageResource(R.drawable.ic_music_off)
                btnMusicOff.setBackgroundResource(R.drawable.bg_icons)
            }

        } else {
            binding.apply {
                btnMusicOn.setImageResource(R.drawable.ic_music_on)
                btnMusicOn.setBackgroundResource(R.drawable.bg_icons)

                btnMusicOff.setImageResource(R.drawable.ic_music_off_selected)
                btnMusicOff.setBackgroundResource(R.drawable.bg_icons_selected)
            }

        }


    }

    private fun vibrationOnOff(isOn: Boolean) {
        if (isOn) {
            binding.apply {
                btnVibrOn.setImageResource(R.drawable.ic_vibr_on_selected)
                btnVibrOn.setBackgroundResource(R.drawable.bg_icons_selected)

                btnVibrOff.setImageResource(R.drawable.ic_vibr_off)
                btnVibrOff.setBackgroundResource(R.drawable.bg_icons)
            }

        } else {
            binding.apply {
                btnVibrOn.setImageResource(R.drawable.icon_vibr_on)
                btnVibrOn.setBackgroundResource(R.drawable.bg_icons)

                btnVibrOff.setImageResource(R.drawable.ic_vibr_selected)
                btnVibrOff.setBackgroundResource(R.drawable.bg_icons_selected)
            }

        }


    }

    private fun manageLanguage(language: String) {
        if (language == EN) {
            binding.apply {
//                btnEn.setBackgroundResource(R.drawable.bg_icons)
//                btnEn.setImageResource(R.drawable.ic_en)
//
//                btnRu.setBackgroundResource(R.drawable.bg_icons_selected)
//                btnRu.setImageResource(R.drawable.ic_ru_selected)
                btnEn.setBackgroundResource(R.drawable.bg_icons_selected)
                btnEn.setImageResource(R.drawable.ic_en_selected)

                btnRu.setBackgroundResource(R.drawable.bg_icons)
                btnRu.setImageResource(R.drawable.ic_ru)
            }
        } else if (language == RU) {
            binding.apply {
//                btnEn.setBackgroundResource(R.drawable.bg_icons_selected)
//                btnEn.setImageResource(R.drawable.ic_en_selected)
//
//                btnRu.setBackgroundResource(R.drawable.bg_icons)
//                btnRu.setImageResource(R.drawable.ic_ru)
                btnEn.setBackgroundResource(R.drawable.bg_icons)
                btnEn.setImageResource(R.drawable.ic_en)

                btnRu.setBackgroundResource(R.drawable.bg_icons_selected)
                btnRu.setImageResource(R.drawable.ic_ru_selected)
            }

        }
    }

}