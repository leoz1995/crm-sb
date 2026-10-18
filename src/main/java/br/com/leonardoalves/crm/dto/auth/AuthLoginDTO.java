package br.com.leonardoalves.crm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Normaliza o e-mail no construtor compacto: UserService.create() grava
 * normalizado, logo a busca do login tem de normalizar igual.
 *
 * Sem @Size na senha de proposito: validar tamanho minimo NO LOGIN vazaria a
 * politica de senha e daria ao atacante um sinal diferente (400 vs 401) para
 * senhas curtas.
 */
public record AuthLoginDTO(
		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "E-mail inválido")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		String password
) {
	public AuthLoginDTO {
		email = email == null ? null : email.trim().toLowerCase();
	}
}
