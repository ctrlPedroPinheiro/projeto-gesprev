package iff.edu.br.gesprev.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import iff.edu.br.gesprev.dto.ServidorDTO;
import iff.edu.br.gesprev.entity.Servidor;
import iff.edu.br.gesprev.repository.ServidorRepository;
import java.util.List;

@Service
public class ServidorService {

    private final ServidorRepository servidorRepository;

    public ServidorService(ServidorRepository servidorRepository) {
        this.servidorRepository = servidorRepository;
    }

    public List<ServidorDTO> listarTodos() {
        return servidorRepository.findAll()
                .stream()
                .map(this::converterDTO)
                .collect(Collectors.toList());
    }

    public ServidorDTO obterServidorPorId(Long id) {
        Servidor servidor = servidorRepository.findById(id).orElseThrow(() -> new RuntimeException("Servidor não encontrado"));
        return converterDTO(servidor);
    }

    public Servidor obterServidorEntidadePorId(Long id) {
        return servidorRepository.findById(id).orElseThrow(() -> new RuntimeException("Servidor não encontrado"));
    }

    public ServidorDTO criarServidor(ServidorDTO servidorDTO) {
        if (servidorRepository.findByCpf(servidorDTO.cpf()) != null) {
            throw new RuntimeException("CPF já cadastrado");
        }
        Servidor servidor = converterEntidade(servidorDTO);
        Servidor servidorSalvo = servidorRepository.save(servidor);
        return converterDTO(servidorSalvo);
    }

    public ServidorDTO atualizarServidor(Long id, ServidorDTO servidorDTO) {
        Servidor servidorExistente = servidorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servidor não encontrado"));

        if (!servidorExistente.getCpf().equals(servidorDTO.cpf())) {
            if (servidorRepository.findByCpf(servidorDTO.cpf()) != null) {
                throw new RuntimeException("CPF já cadastrado");
            }
        }

        servidorExistente.setNome(servidorDTO.nome());
        servidorExistente.setDtNascimento(servidorDTO.dtNascimento());
        servidorExistente.setCpf(servidorDTO.cpf());
        servidorExistente.setPis(servidorDTO.pis());
        servidorExistente.setSexo(servidorDTO.sexo());
        servidorExistente.setEmail(servidorDTO.email());
        servidorExistente.setMatricula(servidorDTO.matricula());
        servidorExistente.setCargo(servidorDTO.cargo());
        servidorExistente.setOrgao(servidorDTO.orgao());
        servidorExistente.setDtAdmissao(servidorDTO.dtAdmissao());

        Servidor servidorAtualizado = servidorRepository.save(servidorExistente);
        return converterDTO(servidorAtualizado);
    }

    public void deletarServidor(Long id) {
        if (!servidorRepository.existsById(id)) {
            throw new RuntimeException("Servidor não encontrado");
        }
        servidorRepository.deleteById(id);
    }

    public ServidorDTO converterDTO(Servidor servidor) {
        return new ServidorDTO(
                servidor.getId(),
                servidor.getNome(),
                servidor.getDtNascimento(),
                servidor.getCpf(),
                servidor.getPis(),
                servidor.getSexo(),
                servidor.getEmail(),
                servidor.getMatricula(),
                servidor.getCargo(),
                servidor.getOrgao(),
                servidor.getDtAdmissao()
        );
    }

    public Servidor converterEntidade(ServidorDTO servidorDTO) {
        return new Servidor(
                servidorDTO.id(),
                servidorDTO.nome(),
                servidorDTO.dtNascimento(),
                servidorDTO.cpf(),
                servidorDTO.pis(),
                servidorDTO.sexo(),
                servidorDTO.email(),
                servidorDTO.matricula(),
                servidorDTO.cargo(),
                servidorDTO.orgao(),
                servidorDTO.dtAdmissao()
        );
    }
}
