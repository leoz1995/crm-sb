package br.com.leonardoalves.crm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Alteracao parcial de perfil. Campo nulo = "nao mexer".
 *
 * Sem o campo password de proposito: a troca de senha tem rota propria, porque
 * exige a senha atual como prova de identidade.
 */
public record UserPatchRequest(

		@Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
		String name,

		@Email(message = "E-mail inválido")
		@Size(max = 254)
		String email
) {
	public UserPatchRequest {
		name = name == null ? null : name.trim();
		email = email == null ? null : email.trim().toLowerCase();
	}
}
