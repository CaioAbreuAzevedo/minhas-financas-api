package br.com.caio.minhasfinancas.api.resource;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.caio.minhasfinancas.api.dto.UsuarioDTO;
import br.com.caio.minhasfinancas.exception.ErroAutenticacaoException;
import br.com.caio.minhasfinancas.exception.RegraNegocioException;
import br.com.caio.minhasfinancas.model.entity.Usuario;
import br.com.caio.minhasfinancas.service.LancamentoService;
import br.com.caio.minhasfinancas.service.UsuarioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="API Rest Usuarios")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioResource {
	
	private UsuarioService usuarioService;
	private LancamentoService lancamentoService;
	
	public UsuarioResource(UsuarioService usuarioService, LancamentoService lancamentoService) {
		this.usuarioService = usuarioService;
		this.lancamentoService = lancamentoService;
	}
	
	@SuppressWarnings("rawtypes")
	@ApiOperation(value="Verifica o e-mail e a senha do usuário e retorna OK caso os dados estejam de acordo.")
	@PostMapping("/autenticar")
	public ResponseEntity autenticar(@RequestBody String email, @RequestBody String senha) {
		try {
			Usuario usuarioAutenticado = usuarioService.autenticar(email, senha);
			return ResponseEntity.ok(usuarioAutenticado);
		} catch (ErroAutenticacaoException ea) {
			return ResponseEntity.badRequest().body(ea.getMessage());
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation(value="Salva um novo usuário caso o e-mail digitado ainda não exista na base de dados.")
	@PostMapping
	public ResponseEntity salvar(@RequestBody UsuarioDTO usuarioDTO) {
		Usuario usuario = Usuario.builder()
				.nome(usuarioDTO.getNome())
				.email(usuarioDTO.getEmail())
				.senha(usuarioDTO.getSenha())
				.build();
		try {
			Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);
			return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
		} catch (RegraNegocioException rne) {
			return ResponseEntity.badRequest().body(rne.getMessage());
		}
	}
	
	@SuppressWarnings("rawtypes")
	@GetMapping("{id}/saldo")
	public ResponseEntity obterSaldo(@PathVariable("id") Long id) {
		Optional<Usuario> usuario = usuarioService.obterPorId(id);
		
		if (!usuario.isPresent()) {
			return ResponseEntity.badRequest().body("Usuário não encontrado");
		}
		
		return ResponseEntity.ok(lancamentoService.obterSaldoPorUsuario(id));
	}

}
