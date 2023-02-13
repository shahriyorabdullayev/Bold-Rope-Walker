package com.BoldRope.Walker.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.BoldRope.Walker.R
import com.BoldRope.Walker.databinding.FragmentGameBinding
import com.BoldRope.Walker.utils.Constants.KEY_BEST_SCORE
import com.BoldRope.Walker.utils.getString
import com.BoldRope.Walker.utils.saveString
import com.BoldRope.Walker.utils.viewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.random.Random.Default.nextInt


class GameFragment : Fragment(R.layout.fragment_game) {

    private var width: Int = 0
    private var height: Int = 0

    private val binding by viewBinding { FragmentGameBinding.bind(it) }

    private var score = 0

    private lateinit var mediaPlayer: MediaPlayer

    private var jobLegPlayer: Job? = null
    private var jobCalculateScore: Job? = null
    private var jobClouds: Job? = null
    private var jobPlayer: Job? = null

    private var jobGameOver: Job? = null


    private var jobPlayBird: Job? = null
    private var jobEndBird: Job? = null
    private var isLeftBird = false
    private var isRightBird = false

    private var jobRemoveList: Job? = null

    private var runPlayer = false

    private lateinit var resultList: ArrayList<Boolean>

    private lateinit var cloudImageView: ImageView
    private lateinit var cloudImageViewList: ArrayList<ImageView>
    private lateinit var cloudDrawables: ArrayList<Int>
    private lateinit var cloudAnimator: ValueAnimator

    private lateinit var birdImageView: ImageView
    private lateinit var birdDrawables: ArrayList<Int>

    private lateinit var leftBirdImageViewList: ArrayList<ImageView>
    private lateinit var rightBirdImageViewList: ArrayList<ImageView>

    private lateinit var leftBirdAnimationX: ValueAnimator
    private lateinit var leftBirdAnimationY: ValueAnimator

    private lateinit var rightBirdAnimationX: ValueAnimator
    private lateinit var rightBirdAnimationY: ValueAnimator

    private lateinit var birdTransitionY: ValueAnimator

    private lateinit var leftClickList: ArrayList<Boolean>
    private lateinit var rightClickList: ArrayList<Boolean>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        birdTransitionY = ValueAnimator()
        leftClickList = ArrayList()
        rightClickList = ArrayList()

        calculateDisplayMetrics()
        scrollingBackground()
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.bird)
        resultList = ArrayList()
        resultList.add(true)
        leftBirdImageViewList = ArrayList()
        rightBirdImageViewList = ArrayList()
        birdDrawables = ArrayList()
        birdDrawables.add(R.drawable.bird_1)
        birdDrawables.add(R.drawable.bird_2)
        birdDrawables.add(R.drawable.bird_3)
        birdDrawables.add(R.drawable.bird_4)


        runPlayerAndCalculateScore()

        startPlayer()


        binding.clickLeft.setOnClickListener {
            playerClickAnimation(nextInt(-15, 0).toFloat())
            leftClickList.add(true)
            resultList.add(true)
        }

        binding.clickRight.setOnClickListener {
            playerClickAnimation(nextInt(0, 15).toFloat())
            rightClickList.add(true)
            resultList.add(true)
        }


        jobGameOver = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(1000)
                if (abs(leftClickList.size - rightClickList.size) >= 4) {
                    delay(1500)
                    gameOver()
                }
            }
        }

        jobRemoveList = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(5000)
                if (leftClickList.isEmpty() && rightClickList.isEmpty()) {
                    gameOver()
                } else {
                    leftClickList.clear()
                    rightClickList.clear()
                }

            }
        }

    }


    private fun birdTransitionY(pos: Float, birdImageView: ImageView) {
        birdTransitionY = ValueAnimator.ofFloat(birdImageView.y, pos)
        birdTransitionY.addUpdateListener {
            val va = it.animatedValue as Float
            birdImageView.translationY = va
        }
        birdTransitionY.apply {
            interpolator = LinearInterpolator()
            duration = 3000
            start()
        }
    }

    private fun gameOver() {
        jobPlayer?.cancel()
        binding.body.animate().rotation(80f).setDuration(300).start()

        val anim1 = ValueAnimator.ofFloat(binding.body.y, height.toFloat() + 800f)
        anim1.addUpdateListener {
            val va = it.animatedValue as Float
            binding.body.y = va
        }
        anim1.apply {
            duration = 2200
            interpolator = LinearInterpolator()
            start()
        }

        binding.leg.animate().rotation(50f).setDuration(300).start()
        val anim2 = ValueAnimator.ofFloat(binding.leg.y, height.toFloat() + 500f)
        anim2.addUpdateListener {
            val va = it.animatedValue as Float
            binding.leg.y = va
        }
        anim2.apply {
            duration = 2000
            interpolator = LinearInterpolator()
            start()
        }

        anim2.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                showGameOverDialog()


            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })


    }

    private fun showGameOverDialog() {
        jobCanceled()
        val dialog = Dialog(requireContext())
        dialog.apply {
            setContentView(R.layout.res_dialog)
            setCancelable(false)
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (getString(KEY_BEST_SCORE).toInt() < score) {
                saveString(KEY_BEST_SCORE, (score - 1).toString())
            }

            findViewById<TextView>(R.id.tv_score_dialog).text = (score - 1).toString()

            findViewById<TextView>(R.id.tv_bestscore_dialog).text = getString(KEY_BEST_SCORE)

            findViewById<TextView>(R.id.btn_menu_dialog).setOnClickListener {
                dismiss()
                requireActivity().onBackPressed()
            }

            show()
        }
    }

    private fun jobCanceled() {
        jobLegPlayer?.cancel()
        jobCalculateScore?.cancel()
        jobPlayer?.cancel()
        jobGameOver?.cancel()
        jobEndBird?.cancel()
        jobRemoveList?.cancel()

        jobPlayBird?.cancel()
        jobEndBird?.cancel()
    }

    private fun playerClickAnimation(toFloat: Float) {
        binding.body.animate().rotation(toFloat).setDuration(300).start()
        binding.langar.animate().rotation(toFloat).setDuration(300).start()
    }

    private fun startPlayer() {
        jobLegPlayer = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(100)
                for (i in 0 until leftBirdImageViewList.size) {
                    leftBirdImageViewList[i].setImageResource(birdDrawables[nextInt(0, 3)])
                }
                for (i in 0 until rightBirdImageViewList.size) {
                    rightBirdImageViewList[i].setImageResource(birdDrawables[nextInt(0, 3)])
                }
            }
        }

        jobPlayer = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(1000)
                val pos = nextInt(-30, 30).toFloat()
                playerBodyAnimation(pos)
//                if (leftBirdImageViewList.isNotEmpty()) {
//                    for (i in 0 until leftBirdImageViewList.size) {
//                        birdTransitionY(pos, leftBirdImageViewList[i])
//                    }
//                }

            }
        }

        jobPlayBird = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(6000)
                if (!isLeftBird) {
                    leftBird()
                } else {
                    delay(5000)
                    leftBirdEnd()
                }
            }
        }

        jobEndBird = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(8000)
                if (!isRightBird) {
                    rightBird()
                } else {
                    delay(5000)
                    rightBirdEnd()
                }
            }
        }
    }


    private fun playerBodyAnimation(gravity: Float) {
        binding.body.animate().rotation(gravity).setDuration(2000).start()
        binding.langar.animate().rotation(gravity).setDuration(2000).start()
    }

    private fun leftBird() {
        mediaPlayer.start()
        birdImageView = ImageView(requireContext())
        binding.birdParentLayout.addView(birdImageView)
        cloudImageView.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        cloudImageView.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        birdImageView.x = -100f
        birdImageView.y = 100f
        leftBirdImageViewList.add(birdImageView)
        birdImageView.setImageResource(birdDrawables[nextInt(0, 3)])
        startLeftBirdAnimation(birdImageView)

    }

    private fun leftBirdEnd() {
        leftBirdAnimationX = ValueAnimator.ofFloat(leftBirdImageViewList[0].x, 0f)
        leftBirdAnimationY = ValueAnimator.ofFloat(leftBirdImageViewList[0].y, 0f)

        leftBirdAnimationX.addUpdateListener {
            val va = it.animatedValue as Float
            leftBirdImageViewList[0].x = va
        }

        leftBirdAnimationY.addUpdateListener {
            val va = it.animatedValue as Float
            leftBirdImageViewList[0].y = va
        }

        leftBirdAnimationX.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        leftBirdAnimationY.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        leftBirdAnimationY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                for (i in 0 until leftBirdImageViewList.size) {
                    leftBirdImageViewList[i].visibility = View.GONE
                }
                leftBirdImageViewList.clear()
                isLeftBird = false
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
    }

    private fun rightBird() {
        mediaPlayer.start()
        birdImageView = ImageView(requireContext())
        binding.birdParentLayout.addView(birdImageView)
        cloudImageView.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        cloudImageView.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        birdImageView.x = width.toFloat()
        birdImageView.y = 100f
        rightBirdImageViewList.add(birdImageView)
        birdImageView.setImageResource(birdDrawables[nextInt(0, 3)])
        startRightBirdAnimation(birdImageView)


    }

    private fun rightBirdEnd() {

        rightBirdAnimationX = ValueAnimator.ofFloat(rightBirdImageViewList[0].x, width.toFloat())
        rightBirdAnimationY = ValueAnimator.ofFloat(rightBirdImageViewList[0].y, 0f)

        rightBirdAnimationX.addUpdateListener {
            val va = it.animatedValue as Float
            rightBirdImageViewList[0].x = va
        }
        rightBirdAnimationY.addUpdateListener {
            val va = it.animatedValue as Float
            rightBirdImageViewList[0].y = va
        }

        rightBirdAnimationX.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        rightBirdAnimationY.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        rightBirdAnimationY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                for (i in 0 until rightBirdImageViewList.size) {
                    rightBirdImageViewList[i].visibility = View.GONE
                }
                rightBirdImageViewList.clear()
                isRightBird = false

            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
    }

    private fun startLeftBirdAnimation(birdImageView: ImageView) {
        leftBirdAnimationX =
            ValueAnimator.ofFloat(birdImageView.x, (width / 2 - nextInt(550, 650)).toFloat())
        leftBirdAnimationY =
            ValueAnimator.ofFloat(birdImageView.y, (height / 2 - nextInt(150, 200)).toFloat())

        leftBirdAnimationX.addUpdateListener {
            val va = it.animatedValue as Float
            birdImageView.x = va
        }
        leftBirdAnimationY.addUpdateListener {
            val va = it.animatedValue as Float
            birdImageView.y = va
        }
        leftBirdAnimationY.addUpdateListener {
            val va = it.animatedValue as Float
            birdImageView.y = va
        }

        leftBirdAnimationX.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        leftBirdAnimationY.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        leftBirdAnimationY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                mediaPlayer.stop()
                for (i in 0 until leftBirdImageViewList.size) {
                    leftBirdImageViewList[i].setImageResource(R.drawable.bird_calm)
                }
                playerClickAnimation(nextInt(-15, 0).toFloat())
                leftClickList.add(true)
                isLeftBird = true


            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }
        })
    }

    private fun startRightBirdAnimation(birdImageView: ImageView) {
        rightBirdAnimationX =
            ValueAnimator.ofFloat(birdImageView.x, (width / 2 + nextInt(200, 300)).toFloat())
        rightBirdAnimationY =
            ValueAnimator.ofFloat(birdImageView.y, (height / 2 - nextInt(150, 200)).toFloat())

        rightBirdAnimationX.addUpdateListener {
            val va = it.animatedValue as Float
            birdImageView.x = va
        }
        rightBirdAnimationY.addUpdateListener {
            val va = it.animatedValue as Float
            birdImageView.y = va
        }
        rightBirdAnimationY.addUpdateListener {
            val va = it.animatedValue as Float
            birdImageView.y = va
        }

        rightBirdAnimationX.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        rightBirdAnimationY.apply {
            interpolator = LinearInterpolator()
            duration = 5000
            start()
        }

        rightBirdAnimationY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                mediaPlayer.stop()
                for (i in 0 until rightBirdImageViewList.size) {
                    rightBirdImageViewList[i].setImageResource(R.drawable.bird_calm)
                }
                playerClickAnimation(nextInt(0, 15).toFloat())
                rightClickList.add(true)
                isRightBird = true

            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
    }

    private fun calculateDisplayMetrics() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels
    }

    private fun scrollingBackground() {
        cloudImageViewList = ArrayList()
        cloudDrawables = ArrayList()
        cloudDrawables.add(R.drawable.cloud_1)
        cloudDrawables.add(R.drawable.cloud_2)
        cloudDrawables.add(R.drawable.cloud_3)
        cloudDrawables.add(R.drawable.cloud_4)
        cloudAnimator = ValueAnimator()
        for (i in 0..15) {
            defaultClouds()
        }

        jobClouds = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(300)
                clouds()
            }
        }
    }

    private fun defaultClouds() {
        cloudImageView = ImageView(requireContext())
        binding.parentCloud.addView(cloudImageView)
        cloudImageView.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        cloudImageView.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        cloudImageView.x = nextInt(0, width + 200).toFloat()
        cloudImageView.y = nextInt(0, height + 100).toFloat()
        cloudImageViewList.add(cloudImageView)
        cloudImageView.setImageResource(cloudDrawables[nextInt(0, 3)])
        defaultCloudAnimation(cloudImageView)
    }

    private fun clouds() {
        cloudImageView = ImageView(requireContext())
        binding.parentCloud.addView(cloudImageView)
        cloudImageView.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        cloudImageView.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        cloudImageView.x = -250f
        cloudImageView.y = nextInt(0, height + 100).toFloat()
        cloudImageViewList.add(cloudImageView)
        cloudImageView.setImageResource(cloudDrawables[nextInt(0, 3)])
        startCloudAnimation(cloudImageView)
    }

    private fun startCloudAnimation(imageView: ImageView) {
        cloudAnimator = ValueAnimator.ofFloat(imageView.x, width.toFloat())
        cloudAnimator.addUpdateListener {
            val va = it.animatedValue as Float
            imageView.x = va
        }
        cloudAnimator.apply {
            duration = 15000
            interpolator = LinearInterpolator()
            start()
        }

        cloudAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                cloudImageViewList.remove(imageView)
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })

    }

    private fun defaultCloudAnimation(imageView: ImageView) {
        cloudAnimator = ValueAnimator.ofFloat(imageView.x, width.toFloat())
        cloudAnimator.addUpdateListener {
            val va = it.animatedValue as Float
            imageView.x = va
        }
        cloudAnimator.apply {
            startDelay = 500
            duration = 12000
            interpolator = LinearInterpolator()
            start()
        }

        cloudAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                cloudImageViewList.remove(imageView)
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })

    }


    private fun runPlayerAndCalculateScore() {
        jobLegPlayer = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(600)
                with(binding) {
                    if (runPlayer) {
                        leg.setImageResource(R.drawable.leg_player_1)
                        runPlayer = false
                    } else {
                        leg.setImageResource(R.drawable.leg_player_2)
                        runPlayer = true
                    }
                }
            }
        }

        jobCalculateScore = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (isActive) {
                delay(200)
                with(binding) {
                    tvScore.text = "${score++}"
                }
            }
        }
    }

    private fun View.absX(): Float {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        return location[0].toFloat()
    }

    private fun View.absY(): Float {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        return location[1].toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        jobCanceled()
        jobClouds?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        jobCanceled()
        jobClouds?.cancel()
    }
}