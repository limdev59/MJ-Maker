package kr.ac.tukorea.ge.spgp2026.mjmaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.ac.tukorea.ge.spgp2026.mjmaker.databinding.ActivityMainBinding
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Scene

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Scene.change(TitleScene())
    }
}
