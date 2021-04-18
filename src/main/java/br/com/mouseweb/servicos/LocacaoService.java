package br.com.mouseweb.servicos;

import static br.com.mouseweb.utils.DataUtils.adicionarDias;

import java.util.Date;

import br.com.mouseweb.entidades.Filme;
import br.com.mouseweb.entidades.Locacao;
import br.com.mouseweb.entidades.Usuario;
import br.com.mouseweb.exception.FilmeSemEstoqueException;
import br.com.mouseweb.exception.LocadoraException;
import br.com.mouseweb.utils.DataUtils;

/* Fast = Executado Rápido
 * Independent =  O teste está independente
 * Repeatable = O teste está repetível
 * Self-Verifying = O teste está Auto validável
 * Timely = O teste criado no momento correto
 */
public class LocacaoService {
	
	public Locacao alugarFilme(Usuario usuario, Filme filme) throws LocadoraException, FilmeSemEstoqueException {

		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}

		if(filme == null) {
			throw new LocadoraException("Filme vazio");
		}

		if(filme.getEstoque() == 0) {
			throw new FilmeSemEstoqueException();
		}

		Locacao locacao = new Locacao();
		locacao.setFilme(filme);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		locacao.setValor(filme.getPrecoLocacao());

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		//TODO adicionar método para salvar
		
		return locacao;
	}
	
	public static void main(String[] args) throws Exception {
		//cenario
		LocacaoService service = new LocacaoService();
		Usuario usuario = new Usuario("Usuario 1");
		Filme filme = new Filme("Filme 1", 2, 5.0);
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filme);

		//verificacao
		System.out.println(locacao.getValor() == 5.0);
		System.out.println(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()));
		System.out.println(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)));
	}

}