package iff.edu.br.gesprev.service;

import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import iff.edu.br.gesprev.dto.LoginDTO;
import iff.edu.br.gesprev.dto.UsuarioDTO;
import iff.edu.br.gesprev.entity.Usuario;
import iff.edu.br.gesprev.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioDTO autenticar(LoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByCpf(loginDTO.cpf());
        
        if (usuario == null) {
            return null;
        }
        
        if (!usuario.isAtivo()) {
            return null;
        }

        boolean senhaBate = passwordEncoder.matches(loginDTO.senha(), usuario.getSenha());

        if (!senhaBate) {
            return null;
        }

        usuario.setUltimoLogin(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        return converterEntidade(usuario);
    }

    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::converterEntidade)
                .collect(Collectors.toList());
    }

    public Usuario obterUsuarioEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public UsuarioDTO obterUsuarioPorCPF(String cpf) {
        Usuario usuario = usuarioRepository.findByCpf(cpf);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        return converterEntidade(usuario);
    }

    public UsuarioDTO criarUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.findByCpf(usuarioDTO.cpf()) != null) {
            throw new RuntimeException("CPF já cadastrado");
        }
        Usuario usuario = converterDTO(usuarioDTO);
        usuario.setSenha(passwordEncoder.encode(usuarioDTO.senha()));
        usuario.setAtivo(true);
        usuario.setDataCriacao(java.time.LocalDateTime.now());
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return converterEntidade(usuarioSalvo);
    }

    public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuarioExistente.getCpf().equals(usuarioDTO.cpf())) {
            if (usuarioRepository.findByCpf(usuarioDTO.cpf()) != null) {
                throw new RuntimeException("CPF já cadastrado");
            }
        }

        usuarioExistente.setNome(usuarioDTO.nome());
        usuarioExistente.setCpf(usuarioDTO.cpf());
        usuarioExistente.setSenha(passwordEncoder.encode(usuarioDTO.senha()));
        usuarioExistente.setPerfil(usuarioDTO.perfil());
        usuarioExistente.setAtivo(usuarioDTO.ativo());

        Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);
        return converterEntidade(usuarioAtualizado);
    }

    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    public UsuarioDTO converterEntidade(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                null,
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.getDataCriacao(),
                usuario.getUltimoLogin()
        );
    }

    public Usuario converterDTO(UsuarioDTO usuarioDTO) {
        return new Usuario(
                usuarioDTO.id(),
                usuarioDTO.nome(),
                usuarioDTO.cpf(),
                usuarioDTO.senha(),
                usuarioDTO.perfil(),
                usuarioDTO.ativo(),
                usuarioDTO.dataCriacao(),
                usuarioDTO.ultimoLogin()
        );
    }
}
