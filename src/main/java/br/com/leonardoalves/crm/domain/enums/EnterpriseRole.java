package br.com.leonardoalves.crm.domain.enums;

public enum EnterpriseRole {
	OWNER(3), ADMIN(2), MEMBER(1);
	
	private final int level;
	
	EnterpriseRole(int level){ this.level = level; }
	public boolean actAs(EnterpriseRole other) {
		return this.level >= other.level;
	}
}
