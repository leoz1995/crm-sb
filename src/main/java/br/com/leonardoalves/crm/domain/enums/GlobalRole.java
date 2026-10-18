package br.com.leonardoalves.crm.domain.enums;

public enum GlobalRole {
	PLATFORM_OWNER(4), BUSINESSPERSON(3), EMPLOYEE(2), MEMBER(1);
	
	private final int level;
	
	GlobalRole(int level) { this.level = level; }
	
	public boolean actAs(GlobalRole other) {
		return this.level >= other.level;
	}
}
