package br.com.mouseweb.service;

import br.com.mouseweb.dao.LocacaoDAO;
import br.com.mouseweb.entidades.Filme;
import br.com.mouseweb.entidades.Locacao;
import br.com.mouseweb.entidades.Usuario;
import br.com.mouseweb.exception.FilmeSemEstoqueException;
import br.com.mouseweb.exception.LocadoraException;
import br.com.mouseweb.matchers.MatchersProprios;
import br.com.mouseweb.servicos.EmailService;
import br.com.mouseweb.servicos.LocacaoService;

import br.com.mouseweb.servicos.SPCService;
import br.com.mouseweb.utils.DataUtils;
import buildermaster.BuilderMaster;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.com.mouseweb.builders.FilmeBuilder.umFilme;
import static br.com.mouseweb.builders.LocacaoBuilder.umLocacao;
import static br.com.mouseweb.builders.UsuarioBuilder.umUsuario;
import static br.com.mouseweb.matchers.MatchersProprios.ehHoje;
import static br.com.mouseweb.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static br.com.mouseweb.utils.DataUtils.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

/* Fast = Executado Rápido
 * Independent =  O teste está independente
 * Repeatable = O teste está repetível
 * Self-Verifying = O teste está Auto validável
 * Timely = O teste criado no momento correto
 */

public class LocacaoServiceTest {

    @InjectMocks
    private LocacaoService service;
    @Mock
    private LocacaoDAO dao;
    @Mock
    private SPCService spc;
    @Mock
    private EmailService email;

    @Rule
    public ErrorCollector erro = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        // Com anotação é desnecessário instância manualmente
        /*service = new LocacaoService();
        dao = Mockito.mock(LocacaoDAO.class);
        service.setLocacaoDAO(dao);

        spc = Mockito.mock(SPCService.class);
        service.setSPCService(spc);

        email = Mockito.mock(EmailService.class);
        service.setEmailService(email);*/
    }

    public Locacao alugarFilme(Usuario usuario, Filme filme) {
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

    @Test
    public void deveAlugarFilme() throws Exception {
        Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 2, 5.0);
        //Builder
        Usuario usuarioBuilder = umUsuario().agora();
        //------------------------------------------------------------------------------//
        //acao
        Locacao locacao = service.alugarFilme(usuario, filme);
        //Builder
        Locacao locacao1 = service.alugarFilme(usuarioBuilder, filme);
        //------------------------------------------------------------------------------//
        //verificacao
        Assert.assertEquals(5.0, locacao.getValor(), 0.01);
        Assert.assertTrue(isMesmaData(locacao.getDataLocacao(), new Date()));
        Assert.assertTrue(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)));

        Assert.assertEquals(5.0, locacao1.getValor(), 0.01);
        Assert.assertTrue(isMesmaData(locacao1.getDataLocacao(), new Date()));
        Assert.assertTrue(isMesmaData(locacao1.getDataRetorno(), obterDataComDiferencaDias(1)));

        //verificacao 2
        assertThat(locacao.getValor(), is(equalTo(5.0)));
        assertThat(locacao.getValor(), is(not(6.0)));
        assertThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        assertThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));

        assertThat(locacao1.getValor(), is(equalTo(5.0)));
        assertThat(locacao1.getValor(), is(not(6.0)));
        assertThat(isMesmaData(locacao1.getDataLocacao(), new Date()), is(true));
        assertThat(isMesmaData(locacao1.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));

        //verificacao 3
        erro.checkThat(locacao.getValor(), is(equalTo(5.0)));
        erro.checkThat(locacao.getValor(), is(not(6.0)));
        erro.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        erro.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));

        erro.checkThat(locacao1.getValor(), is(equalTo(5.0)));
        erro.checkThat(locacao1.getValor(), is(not(6.0)));
        erro.checkThat(isMesmaData(locacao1.getDataLocacao(), new Date()), is(true));
        erro.checkThat(isMesmaData(locacao1.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));

    }

    @Test
    public void deveAlugarFilmeList() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));

        //Builder
        Usuario usuarioBuilder = umUsuario().agora();
        List<Filme> filmeBuilder = Arrays.asList(umFilme().comValor(5.0).agora());
        //------------------------------------------------------------------------------//
        //acao
        Locacao locacao = service.alugarFilmeList(usuario, filmes);
        Locacao locacao1 = service.alugarFilmeList(usuarioBuilder, filmeBuilder);
        //------------------------------------------------------------------------------//
        //verificacao
        Assert.assertEquals(5.0, locacao.getValor(), 0.01);
        Assert.assertTrue(isMesmaData(locacao.getDataLocacao(), new Date()));
        Assert.assertTrue(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)));

        Assert.assertEquals(5.0, locacao1.getValor(), 0.01);
        Assert.assertTrue(isMesmaData(locacao1.getDataLocacao(), new Date()));
        Assert.assertTrue(isMesmaData(locacao1.getDataRetorno(), obterDataComDiferencaDias(1)));

        //verificacao 2
        assertThat(locacao.getValor(), is(equalTo(5.0)));
        assertThat(locacao.getValor(), is(not(6.0)));
        assertThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        assertThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));

        //verificacao 3
        erro.checkThat(locacao.getValor(), is(equalTo(5.0)));
        erro.checkThat(locacao.getValor(), is(not(6.0)));
        erro.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        erro.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));

        //verificacao 4
        erro.checkThat(locacao.getDataLocacao(), ehHoje());
        erro.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));

    }

    //Forma elegante
    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 2", 0, 4.0);

        //acao
        service.alugarFilme(usuario, filme);

    }

    //Forma elegante
    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoqueList() throws Exception{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(umFilme().semEstoque().agora());

        //acao
        service.alugarFilmeList(usuario, filmes);

    }

    // Forma robusta -> recomendação
    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException{
        //cenario
        Filme filme = new Filme("Filme 2", 1, 4.0);

        //acao
        try {
            service.alugarFilme(null, filme);
            Assert.fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    // Forma robusta -> recomendação
    @Test
    public void naoDeveAlugarFilmeSemUsuarioList() throws FilmeSemEstoqueException{
        //cenario
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));

        //acao
        try {
            service.alugarFilmeList(null, filmes);
            Assert.fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    //Forma nova
    @Test
    public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme vazio");

        //acao
        service.alugarFilme(usuario, null);
    }

    //Forma nova
    @Test
    public void naoDeveAlugarFilmeSemFilmeList() throws FilmeSemEstoqueException, LocadoraException{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme vazio");

        //acao
        service.alugarFilmeList(usuario, null);
    }

    @Test
    public void devePagar75PctNoFilme3() throws FilmeSemEstoqueException, LocadoraException{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), new Filme("Filme 3", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilmeList(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilmeList(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(13.0));

    }

    @Test
    public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0),
                new Filme("Filme 5", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilmeList(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(14.0));

    }

    @Test
    public void devePagar0PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException{
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(
                new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0),
                new Filme("Filme 5", 2, 4.0), new Filme("Filme 6", 2, 4.0));

        //acao
        Locacao resultado = service.alugarFilmeList(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(14.0));

    }

    @Test
    //@Ignore
    public void deveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException{
        Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));

        //acao
        Locacao retorno = service.alugarFilmeList(usuario, filmes);

        //verificacao
        //assertThat(retorno.getDataRetorno(), MatchersProprios.caiEm(Calendar.SUNDAY));
        assertThat(retorno.getDataRetorno(), MatchersProprios.caiNumaSegunda());

    }

    public static void main(String[] args) {
        new BuilderMaster().gerarCodigoClasse(Locacao.class);
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
        //cenario
        Usuario usuario = umUsuario().agora();
        Usuario usuario2 = umUsuario().comNome("Usuario 2").agora();
        Filme filme = umFilme().agora();

        // Informa qual usuário deverá ser retornado a qual é a instância e atribui true
        when(spc.possuiNegativacao(usuario)).thenReturn(true);
        when(spc.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

        //acao
        try {
            service.alugarFilme(usuario, filme);
            //verificacao
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuário Negativado"));
        }
        //verificacao
        verify(spc).possuiNegativacao(usuario);

    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas(){
        //cenario
        Usuario usuario = umUsuario().agora();
        Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
        Usuario usuario3 = umUsuario().comNome("Outro atrasado").agora();

        List<Locacao> locacoes = Arrays.asList(
                umLocacao().atrasada().comUsuario(usuario).agora(),
                umLocacao().comUsuario(usuario2).agora(),
                umLocacao().atrasada().comUsuario(usuario3).agora(),
                umLocacao().atrasada().comUsuario(usuario3).agora());

        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        service.notificarAtrasos();

        //verificacao
        verify(email, times(3)).notificarAtraso(Mockito.any(Usuario.class));
        verify(email).notificarAtraso(usuario);
        verify(email, Mockito.atLeastOnce()).notificarAtraso(usuario3);
        verify(email, never()).notificarAtraso(usuario2);
        verifyNoMoreInteractions(email);
    }

    @Test
    public void deveTratarErronoSPC() throws Exception {
        //cenario
        Usuario usuario = umUsuario().agora();
        Filme filme = umFilme().agora();

        when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catratrófica"));

        //verificacao
        exception.expect(LocadoraException.class);
        exception.expectMessage("Problemas com SPC, tente novamente");

        //acao
        service.alugarFilme(usuario, filme);

    }

}
