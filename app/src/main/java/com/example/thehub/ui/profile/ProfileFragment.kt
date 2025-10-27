package com.example.thehub.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.thehub.R
import com.example.thehub.databinding.FragmentProfileBinding
import com.example.thehub.di.ServiceLocator
import com.example.thehub.ui.login.LoginActivity
import com.example.thehub.utils.TokenStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(ServiceLocator.userRepository)
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.errorRetry.setOnClickListener { viewModel.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ProfileUiState.Loading -> renderLoading()
                        is ProfileUiState.Success -> renderSuccess(state)
                        is ProfileUiState.Error -> renderError(state)
                    }
                }
            }
        }
    }

    private fun handleUnauthorized() {
        val context = requireContext()
        TokenStore.clear(context)
        startActivity(Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    private fun renderLoading() {
        val hasContent = binding.profileContent.isVisible
        binding.progressBar.isVisible = !hasContent
        binding.swipeRefresh.isVisible = hasContent
        binding.swipeRefresh.isRefreshing = hasContent
        binding.errorGroup.isVisible = false
    }

    private fun renderSuccess(state: ProfileUiState.Success) {
        binding.progressBar.isVisible = false
        binding.errorGroup.isVisible = false
        binding.swipeRefresh.isVisible = true
        binding.swipeRefresh.isRefreshing = false
        binding.profileContent.isVisible = true

        binding.profileTitle.text = getString(R.string.profile_title)
        binding.profileName.text = state.user.name
        binding.profileEmail.text = state.user.email

        val avatarUrl = state.user.avatarUrl?.takeIf { it.isNotBlank() }
        if (avatarUrl != null) {
            binding.profileAvatar.load(avatarUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
            }
        } else {
            binding.profileAvatar.setImageResource(R.drawable.ic_person)
        }
    }

    private fun renderError(state: ProfileUiState.Error) {
        binding.progressBar.isVisible = false
        binding.swipeRefresh.isVisible = false
        binding.swipeRefresh.isRefreshing = false
        binding.profileContent.isVisible = false

        if (state.isUnauthorized) {
            handleUnauthorized()
            return
        }

        binding.errorGroup.isVisible = true
        binding.errorMessage.text = state.message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*
Manual quick tests:
1. Éxito: usa un proxy como Charles/mitmproxy para que /auth/me responda 200 con {id,name,email,avatar_url} y verifica que el Card muestre nombre, correo y foto.
2. Pull-to-refresh: con datos ya cargados, fuerza otra respuesta diferente y desliza hacia abajo; el SwipeRefresh debe mostrar el loader y actualizar los datos.
3. Error genérico: responde 500 o JSON inválido y comprueba que aparece el mensaje, se oculta el contenido y el botón "Reintentar" vuelve a llamar al endpoint.
4. 401: intercepta la petición y regresa 401; la app debe limpiar el token y volver a Login automáticamente.
5. Avatar faltante: responde avatar_url=null o "" y se debe mostrar el ícono ic_person.
Para simular error de red sin backend, activa "No internet" en el emulador y confirma el mensaje correspondiente.
*/
