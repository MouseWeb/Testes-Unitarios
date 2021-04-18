package br.com.mouseweb.servicos;

import br.com.mouseweb.entidades.Usuario;

public interface EmailService {

    public void notificarAtraso(Usuario usuario);

}
