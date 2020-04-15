package br.com.caio.minhasfinancas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.caio.minhasfinancas.exception.RegraNegocioException;
import br.com.caio.minhasfinancas.model.entity.Lancamento;
import br.com.caio.minhasfinancas.model.enums.StatusLancamentoEnum;
import br.com.caio.minhasfinancas.model.enums.TipoLancamentoEnum;
import br.com.caio.minhasfinancas.model.repository.LancamentoRepository;
import br.com.caio.minhasfinancas.service.LancamentoService;

@Service
public class LancamentoServiceImpl implements LancamentoService {

	private LancamentoRepository lancamentoRepository;
	
	public LancamentoServiceImpl(LancamentoRepository lancamentoRepository) {
		this.lancamentoRepository = lancamentoRepository;
	}
	
	@Override
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		validar(lancamento);
		lancamento.setStatus(StatusLancamentoEnum.PENDENTE);
		return lancamentoRepository.save(lancamento);
	}

	@Override
	@Transactional
	public Lancamento atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		validar(lancamento);
		return lancamentoRepository.save(lancamento);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
		@SuppressWarnings("rawtypes")
		Example example = Example.of(lancamentoFiltro,
				ExampleMatcher.matching()
				.withIgnoreCase()
				.withStringMatcher(StringMatcher.CONTAINING));
		return lancamentoRepository.findAll(example);
	}

	@Override	
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		lancamentoRepository.delete(lancamento);
	}

	@Override
	@Transactional
	public void atualizarStatus(Lancamento lancamento, StatusLancamentoEnum status) {
		lancamento.setStatus(status);
		atualizar(lancamento);
	}

	@Override
	public void validar(Lancamento lancamento) {
		if (lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")) {
			throw new RegraNegocioException("Informe uma descrição");
		}
		
		if (lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12) {
			throw new RegraNegocioException("Informe um mês válido (de 1 a 12)");
		}
		
		if (lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
			throw new RegraNegocioException("Informe um ano válido com 4 dígitos");
		}
		
		if (lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
			throw new RegraNegocioException("Informe um usuário");
		}
		
		if (lancamento.getValor() == null) {
			throw new RegraNegocioException("Informe o valor válido");
		}
		
		if (lancamento.getTipo() == null) {
			throw new RegraNegocioException("Informe um tipo de lançamento válido");
		}
	}

	@Override
	public Optional<Lancamento> obterPorId(Long id) {
		return lancamentoRepository.findById(id);
	}

	@Override
	@Transactional
	public BigDecimal obterSaldoPorUsuario(Long id) {
		BigDecimal receitas = lancamentoRepository.obterSaldoPorTipoLancamentoEUsuario(id, TipoLancamentoEnum.RECEITA);
		BigDecimal despesas = lancamentoRepository.obterSaldoPorTipoLancamentoEUsuario(id, TipoLancamentoEnum.DESPESA);
		
		if (receitas == null) {
			receitas = BigDecimal.ZERO;
		}
		
		if (despesas == null) {
			despesas = BigDecimal.ZERO;
		}
		
		return receitas.add(despesas);
	}

}
