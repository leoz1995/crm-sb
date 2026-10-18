package br.com.leonardoalves.crm.dto.auth;

public record AuthResponseDTO(String token, String tokenType, long expiresIn) {}
