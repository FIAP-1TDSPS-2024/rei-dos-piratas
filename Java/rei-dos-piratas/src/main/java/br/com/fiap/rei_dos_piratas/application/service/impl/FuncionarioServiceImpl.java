package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final PerfilRepository perfilRepository;

    public FuncionarioServiceImpl(FuncionarioRepository repository, PasswordEncoder passwordEncoder, PerfilRepository perfilRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
    }

    @Override
    public Page<Funcionario> listAll(int pageNumber, int pageSize) {
        return this.repository.findAllByUsuarioAtivoTrue(pageNumber, pageSize);
    }

    @Override
    public Funcionario findById(Long id) {
        try {
            return this.repository.findById(id);
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um vendedor com o id " + id);
        }
    }

    @Override
    @Transactional
    public Funcionario create(Funcionario funcionario) {
        //Encriptar senha para salvar em banco
        String encryptedPassword = this.passwordEncoder.encode(funcionario.getPassword());
        funcionario.setSenha(encryptedPassword);

        //Definir perfil do usuário
        Perfil perfil = this.perfilRepository.findByNome(funcionario.getPerfil().getNome());
        funcionario.setPerfil(perfil);

        return this.repository.create(funcionario);
    }

    @Override
    @Transactional
    public Funcionario update(Funcionario funcionario) {

        //Encriptar senha para salvar em banco
        String encryptedPassword = this.passwordEncoder.encode(funcionario.getPassword());
        funcionario.setSenha(encryptedPassword);

        Funcionario funcionarioAtualizado = this.repository.update(funcionario);

        if (funcionarioAtualizado == null){
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o id " + funcionario.getId() + ". Crie um novo cliente.");
        }

        return funcionarioAtualizado;
    }

    @Override
    public Funcionario ativarDesativar(Long id) {
        Funcionario funcionario = this.findById(id);

        if (funcionario.isUsuarioAtivo()){
            funcionario.setUsuarioAtivo(false);
        }
        else {
            funcionario.setUsuarioAtivo(true);
        }

        return this.repository.update(funcionario);
    }
}
