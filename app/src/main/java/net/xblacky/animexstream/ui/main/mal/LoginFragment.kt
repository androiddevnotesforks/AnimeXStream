package net.vapormusic.animexstream.ui.main.mal


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.findNavController
import com.axiel7.moelist.utils.PkceGenerator
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.android.synthetic.main.web_view.view.*
import net.vapormusic.animexstream.R

class LoginFragment : Fragment(), View.OnClickListener {


    private lateinit var rootView: View
    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        setupClickListeners()
        codeVerifier = PkceGenerator.generateVerifier(128)
        codeChallenge = codeVerifier

        return rootView
    }




    override fun onResume() {
        super.onResume()
    }

    private fun setupClickListeners() {
        rootView.exitwebview.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.exitwebview -> findNavController().popBackStack()

        }
    }
}
