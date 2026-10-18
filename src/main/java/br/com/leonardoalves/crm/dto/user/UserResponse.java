package br.com.leonardoalves.crm.dto.user;

import java.util.UUID;

import br.com.leonardoalves.crm.domain.User;
import br.com.leonardoalves.crm.domain.enums.EnterpriseRole;

/**
 * DTO de saida. A senha nao esta aqui — e nao e por @JsonIgnore: o campo
 * simplesmente nao existe, logo nao ha o que esconder.
 */
public record UserResponse(
		UUID id,
		String name,
		String email,
		EnterpriseRole globalRole,
		boolean ativo
) {
	public static UserResponse de(User u) {
		return new UserResponse(u.getId(), u.getName(), u.getEmail(),
				u.getGlobalRole(), u.isActiveUser());
	}
}
