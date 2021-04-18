package br.com.mouseweb.dao;

import br.com.mouseweb.entidades.Locacao;

import java.util.List;

public interface LocacaoDAO {

    public void salvar(Locacao locacao);
    public List<Locacao> obterLocacoesPendentes();
}
