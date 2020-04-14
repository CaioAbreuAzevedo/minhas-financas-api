package br.com.caio.minhasfinancas.exception;

@SuppressWarnings("serial")
public class ErroAutenticacaoException extends RuntimeException {
	
	public ErroAutenticacaoException(String msg) {
		super(msg);
	}
}
