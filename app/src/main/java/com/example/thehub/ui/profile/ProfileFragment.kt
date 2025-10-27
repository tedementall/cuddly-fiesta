package com.example.thehub.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thehub.di.ServiceLocator
import com.example.thehub.ui.login.LoginActivity
import com.example.thehub.utils.TokenStore

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(ServiceLocator.userRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state = viewModel.state.collectAsStateWithLifecycle().value

                MaterialTheme {
                    Surface {
                        ProfileRoute(
                            state = state,
                            onRefresh = { viewModel.refresh() },
                            onRetry = { viewModel.refresh() },
                            onUnauthorized = { handleUnauthorized() },
                        )
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
}

/*
Manual quick tests:
1. Éxito: usa un proxy como Charles/mitmproxy para que /auth/me responda 200 con {id,name,email,avatar_url} y verifica nombre/correo/foto.
2. Error genérico: responde 500 o JSON inválido y comprueba que aparece el mensaje y botón Reintentar.
3. 401: intercepta la petición y regresa 401; la app debe limpiar el token y volver a Login automáticamente.
4. Avatar faltante: responde avatar_url=null o "" y se debe mostrar el ícono por defecto.
Para simular error de red sin backend, activa "No internet" en el emulador y confirma el mensaje correspondiente.
*/
