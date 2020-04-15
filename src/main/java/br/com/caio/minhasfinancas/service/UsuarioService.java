package br.com.caio.minhasfinancas.service;

import java.util.Optional;

import br.com.caio.minhasfinancas.model.entity.Usuario;

public interface UsuarioService {
	
	Usuario autenticar(String email, String senha);
	
	Usuario salvarUsuario(Usuario usuario);
	
	Optional<Usuario> obterPorId(Long id);

	void validarEmail(String email);

}
