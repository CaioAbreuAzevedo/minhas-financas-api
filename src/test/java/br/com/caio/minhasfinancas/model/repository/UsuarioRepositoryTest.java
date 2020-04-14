package br.com.caio.minhasfinancas.model.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.caio.minhasfinancas.model.entity.Usuario;

@RunWith( SpringRunner.class )
@DataJpaTest
@AutoConfigureTestDatabase( replace = Replace.NONE )
public class UsuarioRepositoryTest {
	
	@Autowired
	UsuarioRepository repository;
	
	@Autowired
	TestEntityManager testEntityManager;
	
	@Test
	public void testarVerificarEmailExistente() {
		testEntityManager.persist(criarUsuario());
		Assertions.assertThat(repository.existsByEmail("teste@teste")).isTrue();
	}
	
	@Test
	public void testarVerificarEnailNaoExistente() {
		Assertions.assertThat(repository.existsByEmail("email@inexistente.teste")).isFalse();
	}
	
	@Test
	public void testarPersistirUsuarioNaBaseDeDados() {
		Usuario usuario = criarUsuario();
		Usuario usuarioSalvo = repository.save(usuario);
		Assertions.assertThat(usuarioSalvo.getId()).isNotNull();
	}
	
	@Test
	public void testarRetornarUsuarioPorEmailExistente() {
		testEntityManager.persist(repository.save(criarUsuario()));
		Optional<Usuario> resultado = repository.findByEmail("teste@teste");
		Assertions.assertThat(resultado.isPresent()).isTrue();
	}
	
	@Test
	public void testarRetornarUsuarioPorEmailNaoExistente() {
		Optional<Usuario> resultado = repository.findByEmail("teste@teste");
		Assertions.assertThat(resultado.isPresent()).isFalse();
	}

	private Usuario criarUsuario() {
		return Usuario.builder()
				.nome("Usuario Teste")
				.email("teste@teste")
				.senha("teste123")
				.build();
	}

}
