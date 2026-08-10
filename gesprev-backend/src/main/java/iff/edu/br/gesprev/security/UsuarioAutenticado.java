package iff.edu.br.gesprev.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import iff.edu.br.gesprev.entity.Usuario;
import iff.edu.br.gesprev.repository.UsuarioRepository;

/**
 * Classe para obter o usuário autenticado no contexto de segurança do Spring Security.
 */
@Component
public class UsuarioAutenticado {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obter() {
        String cpf = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioRepository.findByCpf(cpf);
    }

    public Long obterUsuarioId() {
        return obter().getId();
    }
}