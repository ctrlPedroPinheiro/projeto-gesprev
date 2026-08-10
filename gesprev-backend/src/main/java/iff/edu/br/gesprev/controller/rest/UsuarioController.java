package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import iff.edu.br.gesprev.dto.LoginDTO;
import iff.edu.br.gesprev.dto.UsuarioDTO;
import iff.edu.br.gesprev.service.UsuarioService;
import iff.edu.br.gesprev.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios e autenticacao", description = "Login JWT e administracao de usuarios pelo diretor.")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public UsuarioController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Retorna token JWT e dados do usuario autenticado.", security = {})
    public ResponseEntity<?> autenticar(@Valid @RequestBody LoginDTO loginDTO) {
        UsuarioDTO usuario = usuarioService.autenticar(loginDTO);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CPF ou senha inválidos");
        }
        String token = jwtService.gerarToken(usuario.cpf(), usuario.perfil().name());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "usuario", usuario
        ));
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar usuario por CPF", description = "Endpoint administrativo do perfil DIRETOR.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UsuarioDTO> obterPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(usuarioService.obterUsuarioPorCPF(cpf));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Endpoint administrativo do perfil DIRETOR.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PostMapping
    @Operation(summary = "Criar usuario", description = "Cria usuario ANALISTA ou DIRETOR. Endpoint exclusivo do DIRETOR.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UsuarioDTO> criar(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO criado = usuarioService.criarUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuario", description = "Atualiza dados, senha, perfil e situacao do usuario. Endpoint exclusivo do DIRETOR.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, usuarioDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuario", description = "Remove usuario do sistema. Endpoint exclusivo do DIRETOR.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
