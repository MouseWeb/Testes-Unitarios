package br.com.mouseweb.servicos;

import br.com.mouseweb.dao.LocacaoDAO;
import br.com.mouseweb.entidades.Filme;
import br.com.mouseweb.entidades.Locacao;
import br.com.mouseweb.entidades.Usuario;
import br.com.mouseweb.exception.FilmeSemEstoqueException;
import br.com.mouseweb.exception.LocadoraException;
import br.com.mouseweb.utils.DataUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.com.mouseweb.utils.DataUtils.adicionarDias;

/* Fast = Executado Rápido
 * Independent =  O teste está independente
 * Repeatable = O teste está repetível
 * Self-Verifying = O teste está Auto validável
 * Timely = O teste criado no momento correto
 */
public class LocacaoService {

	private LocacaoDAO dao;
	private SPCService spcService;
	private EmailService emailService;
	
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

		if(spcService.possuiNegativacao(usuario)) {
			throw new LocadoraException("Usuário Negativado");
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
		dao.salvar(locacao);

		return locacao;
	}

	public Locacao alugarFilmeList(Usuario usuario, List<Filme> filmes) throws LocadoraException, FilmeSemEstoqueException {

		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}

		if(filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Filme vazio");
		}

		for (Filme filme: filmes){
			if(filme.getEstoque() == 0) {
				throw new FilmeSemEstoqueException();
			}
		}

		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		Double valorTotal = 0d;
		for(int i = 0; i < filmes.size(); i++) {
			Filme filme = filmes.get(i);
			Double valorFilme = filme.getPrecoLocacao();
			switch (i) {
				case 2: valorFilme = valorFilme * 0.75; break;
				case 3: valorFilme = valorFilme * 0.5; break;
				case 4: valorFilme = valorFilme * 0.25; break;
				case 5: valorFilme = 0d; break;
			}
			valorTotal += valorFilme;
		}
		locacao.setValor(valorTotal);

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if(DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);

		//Salvando a locacao...
		dao.salvar(locacao);

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

	public void notificarAtrasos(){
		List<Locacao> locacoes = dao.obterLocacoesPendentes();
		for(Locacao locacao: locacoes) {
			if(locacao.getDataRetorno().before(new Date())) {
				emailService.notificarAtraso(locacao.getUsuario());
			}
		}
	}

	public void setLocacaoDAO(LocacaoDAO dao) {
		this.dao = dao;
	}

	public void setSPCService(SPCService spc) {
		spcService = spc;
	}

	public void setEmailService(EmailService email) {
		emailService = email;
	}

}