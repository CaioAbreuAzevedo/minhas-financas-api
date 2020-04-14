package br.com.caio.minhasfinancas.service;

import java.util.List;

import br.com.caio.minhasfinancas.model.entity.Lancamento;
import br.com.caio.minhasfinancas.model.enums.StatusLancamentoEnum;

public interface LancamentoService {
	
	Lancamento salvar(Lancamento lancamento);
	
	Lancamento atualizar(Lancamento lancamento);
	
	List<Lancamento> buscar(Lancamento lancamentoFiltro);

	void deletar(Lancamento lancamento);
	
	void atualizarStatus (Lancamento lancamento, StatusLancamentoEnum status);
	
	void validar(Lancamento lancamento);

}
