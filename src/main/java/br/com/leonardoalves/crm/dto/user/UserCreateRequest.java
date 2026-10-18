package br.com.leonardoalves.crm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada do cadastro.
 *
 * Nao expoe globalRole nem activeUser de proposito: aceitar a entidade ou um DTO
 * com esses campos permitiria mass assignment (o cliente se promoveria a admin).
 */
public record UserCreateRequest(

		@NotBlank(message = "O nome é obrigatório")
		@Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
		String name,

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "E-mail inválido")
		@Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
		String email,

		/** O BCrypt trunca em 72 bytes: acima disso o excedente seria ignorado em silencio. */
		@NotBlank(message = "A senha é obrigatória")
		@Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
		String password
) {
	/** Normaliza na BORDA: nao existe caminho que produza e-mail desnormalizado. */
	public UserCreateRequest {
		name = name == null ? null : name.trim();
		email = email == null ? null : email.trim().toLowerCase();
	}
}
