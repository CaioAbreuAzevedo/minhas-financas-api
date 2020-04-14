package br.com.caio.minhasfinancas.service;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.caio.minhasfinancas.exception.ErroAutenticacaoException;
import br.com.caio.minhasfinancas.exception.RegraNegocioException;
import br.com.caio.minhasfinancas.model.entity.Usuario;
import br.com.caio.minhasfinancas.model.repository.UsuarioRepository;
import br.com.caio.minhasfinancas.service.impl.UsuarioServiceImpl;

@RunWith(SpringRunner.class)
public class UsuarioServiceTest {
	
	@SpyBean
	UsuarioServiceImpl service;
	
	@MockBean
	UsuarioRepository repository;
	
	@Test(expected = Test.None.class)
	public void validacaoEmailNaoExistenteNaBaseDeDados() {
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		service.validarEmail("teste@teste");
	}
	
	@Test(expected = RegraNegocioException.class)
	public void validacaoEmailExistenteNaBaseDeDados() {
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		service.validarEmail("teste@teste");
	}
	
	@Test(expected = Test.None.class)
	public void testarAutenticarUsuarioComSucesso() {
		Usuario usuario = criarUsuario();
		Mockito.when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
		Usuario resultado = service.autenticar(usuario.getEmail(), usuario.getSenha());
		Assertions.assertThat(resultado).isNotNull();
	}
	
	@Test
	public void testarAutenticarUsuarioSenhaErrada() {
		Usuario usuario = criarUsuario();
		Mockito.when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
		Throwable exception = Assertions.catchThrowable(() -> service.autenticar(usuario.getEmail(), "senhaErraada"));
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacaoException.class).hasMessage("Senha incorreta");
	}
	
	@Test
	public void testarAutenticarUsuarioEmailInexistente() {
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		Throwable exception = Assertions.catchThrowable(() -> service.autenticar("emailErrado", "qualquerSenha"));
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacaoException.class).hasMessage("Email não encontrado");
	}
	
	@Test(expected = Test.None.class)
	public void testarSalvarUsuarioComSucesso() {
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(criarUsuario());
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
		Assertions.assertThat(usuarioSalvo).isNotNull();
		Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo(criarUsuario().getNome());
		Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo(criarUsuario().getEmail());
		Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo(criarUsuario().getSenha());
	}
	
	@Test(expected = RegraNegocioException.class)
	public void testarSalvarUsuarioEmailJaCadastrado() {
		Usuario usuario = criarUsuario();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(Mockito.anyString());
		service.salvarUsuario(usuario);
		Mockito.verify(repository, Mockito.never()).save(usuario);
	}
	
	private Usuario criarUsuario() {
		return Usuario.builder()
				.nome("Usuario Teste")
				.email("teste@teste")
				.senha("teste123")
				.build();
	}
	
}
