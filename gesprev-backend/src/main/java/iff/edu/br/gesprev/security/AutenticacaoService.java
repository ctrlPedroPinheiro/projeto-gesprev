package iff.edu.br.gesprev.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import iff.edu.br.gesprev.entity.Usuario;
import iff.edu.br.gesprev.repository.UsuarioRepository;

/**
 * Serviço de autenticação que implementa a interface UserDetailsService do Spring Security.
 * Ele é responsável por carregar os detalhes do usuário com base no CPF fornecido durante o processo de autenticação.
 */
@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCpf(cpf);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return usuario;
    }
}