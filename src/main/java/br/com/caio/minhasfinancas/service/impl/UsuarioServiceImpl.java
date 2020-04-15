package br.com.caio.minhasfinancas.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.caio.minhasfinancas.exception.ErroAutenticacaoException;
import br.com.caio.minhasfinancas.exception.RegraNegocioException;
import br.com.caio.minhasfinancas.model.entity.Usuario;
import br.com.caio.minhasfinancas.model.repository.UsuarioRepository;
import br.com.caio.minhasfinancas.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {
	
	private UsuarioRepository repository;

	public UsuarioServiceImpl(UsuarioRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Usuario autenticar(String email, String senha) {
		Optional<Usuario> usuario = repository.findByEmail(email);
		
		if(!usuario.isPresent()) {
			throw new ErroAutenticacaoException("Email não encontrado");
		}
		
		if(!usuario.get().getSenha().equals(senha)) {
			throw new ErroAutenticacaoException("Senha incorreta");
		}
		
		return usuario.get();
	}

	@Override
	@Transactional
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());
		return repository.save(usuario);
	}

	@Override
	public void validarEmail(String email) {
		if(repository.existsByEmail(email)) {
			throw new RegraNegocioException("Email já cadastrado");
		}
	}

	@Override
	public Optional<Usuario> obterPorId(Long id) {
		return repository.findById(id);
	}
	

}
