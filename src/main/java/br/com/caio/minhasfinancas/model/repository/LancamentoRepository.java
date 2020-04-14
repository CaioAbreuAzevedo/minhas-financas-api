package br.com.caio.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.caio.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
