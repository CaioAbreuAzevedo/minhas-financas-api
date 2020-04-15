package br.com.caio.minhasfinancas.api.resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.caio.minhasfinancas.api.dto.LancamentoDTO;
import br.com.caio.minhasfinancas.exception.RegraNegocioException;
import br.com.caio.minhasfinancas.model.entity.Lancamento;
import br.com.caio.minhasfinancas.model.entity.Usuario;
import br.com.caio.minhasfinancas.model.enums.StatusLancamentoEnum;
import br.com.caio.minhasfinancas.model.enums.TipoLancamentoEnum;
import br.com.caio.minhasfinancas.service.LancamentoService;
import br.com.caio.minhasfinancas.service.UsuarioService;
import io.swagger.annotations.Api;

@Api(value = "API Rest Lancamentos")
@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoResource {

	private LancamentoService lancamentoService;
	private UsuarioService usuarioService;

	public LancamentoResource(LancamentoService lancamentoService, UsuarioService usuarioService) {
		this.lancamentoService = lancamentoService;
		this.usuarioService = usuarioService;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO lancamentoDTO) {
		try {
			validarLancamento(lancamentoDTO);
			Lancamento lancamento = converterLancamentoDTOParaLancamento(lancamentoDTO);
			
			if (lancamento.getTipo() == TipoLancamentoEnum.DESPESA) {
				lancamento.setValor(lancamento.getValor().negate());
			}
			
			lancamento = lancamentoService.salvar(lancamento);
			return new ResponseEntity(lancamento, HttpStatus.CREATED);
		} catch (RegraNegocioException rne) {
			return ResponseEntity.badRequest().body(rne.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO lancamentoDTO) {
		try {
			return lancamentoService.obterPorId(id).map(retorno -> {
				validarLancamento(lancamentoDTO);
				Lancamento lancamento = converterLancamentoDTOParaLancamento(lancamentoDTO);
				lancamento.setId(retorno.getId());
				
				if (lancamento.getTipo() == TipoLancamentoEnum.DESPESA) {
					lancamento.setValor(lancamento.getValor().negate());
				}
				
				lancamentoService.atualizar(lancamento);
				return ResponseEntity.ok(lancamento);
			}).orElseGet(() -> new ResponseEntity("Erro ao atualizar o lancamento. Tente novamente", HttpStatus.BAD_REQUEST));
		} catch (RegraNegocioException rne) {
			return ResponseEntity.badRequest().body(rne.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id) {
		return lancamentoService.obterPorId(id).map(retorno -> {
			lancamentoService.deletar(retorno);
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}).orElseGet(() -> new ResponseEntity("Lancamento não encontrado", HttpStatus.BAD_REQUEST));
	}

	@SuppressWarnings("rawtypes")
	@GetMapping
	public ResponseEntity buscar(@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "tipo", required = false) String tipo,
			@RequestParam(value = "ano", required = false) Integer ano, @RequestParam("usuario") Long idUsuario) {

		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);

		if (!usuario.isPresent()) {
			return ResponseEntity.badRequest().body("Usuário não encontrado");
		}

		Lancamento filtro = Lancamento.builder().descricao(descricao).mes(mes).tipo(TipoLancamentoEnum.valueOf(tipo.toUpperCase())).ano(ano).usuario(usuario.get()).build();

		List<Lancamento> lancamentos = lancamentoService.buscar(filtro);

		return ResponseEntity.ok(lancamentos);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PutMapping("{id}/atualizar-status")
	public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody String novoStatus) {
		try {
			return lancamentoService.obterPorId(id). map(retorno -> {
				retorno.setStatus(StatusLancamentoEnum.valueOf(novoStatus.toUpperCase()));
				lancamentoService.atualizar(retorno);
				return ResponseEntity.ok(retorno);
			}).orElseGet(() -> new ResponseEntity("Lancamento não encontrado", HttpStatus.BAD_REQUEST));
		} catch (RegraNegocioException rne) {
			return ResponseEntity.badRequest().body(rne.getMessage());
		}
	}

	private Lancamento converterLancamentoDTOParaLancamento(LancamentoDTO lancamentoDTO) {
		return Lancamento.builder().id(lancamentoDTO.getId()).descricao(lancamentoDTO.getDescricao())
				.ano(lancamentoDTO.getAno()).mes(lancamentoDTO.getMes())
				.valor(lancamentoDTO.getValor())
				.usuario(usuarioService.obterPorId(lancamentoDTO.getUsuario())
						.orElseThrow(() -> new RegraNegocioException("Usuário não encontrado para o ID informado")))
				.tipo(TipoLancamentoEnum.valueOf(lancamentoDTO.getTipo().toUpperCase()))
				.status(StatusLancamentoEnum.valueOf(lancamentoDTO.getStatus()))
				.build();
	}
	
	private void validarLancamento(LancamentoDTO lancamentoDTO) {
		if (lancamentoDTO.getTipo() == null) {
			throw new RegraNegocioException("Obrigatório preencher o tipo de lançamento (Receita ou Despesa)");
		}
		
		if (!lancamentoDTO.getTipo().equalsIgnoreCase("despesa") && !lancamentoDTO.getTipo().equalsIgnoreCase("receita")) {
			throw new RegraNegocioException("Tipo de lançamento imcopatível. Tipos permitidos: Despesa ou Receita");
		}
		
		if (lancamentoDTO.getStatus() == null) {
			lancamentoDTO.setStatus("PENDENTE");
		}
		
		if (lancamentoDTO.getValor().compareTo(BigDecimal.ZERO) <= 0) {
			throw new RegraNegocioException("O valor deve ser um número maior que zero");
		}
	}

}
