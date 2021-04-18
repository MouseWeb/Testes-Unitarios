package br.com.mouseweb.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Filme {

	private String nome;
	private Integer estoque;
	private Double precoLocacao;  

}