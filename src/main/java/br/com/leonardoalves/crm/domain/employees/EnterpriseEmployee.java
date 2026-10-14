package br.com.leonardoalves.crm.domain.employees;

import java.time.LocalDate;
import java.util.UUID;

import br.com.leonardoalves.crm.domain.enterprise.Enterprise;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="enterprise_employee")
public class EnterpriseEmployee {
	
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID id;
	
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enterprise_id", nullable = false)
	private Enterprise enterprise;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="employee_id", nullable=false)
	private Employee employee;
	
	@NotBlank //sem texto nullo
	@Column(nullable=false, length=100)
	private String jobTitle;
	
	@Column(nullable=false)
	private LocalDate admissionDate;
	
	@Column(nullable=false)
	private boolean active = true;
	
	protected EnterpriseEmployee() {}
	
	public EnterpriseEmployee(Enterprise enterprise, Employee employee, String jobTitle) {
		this.enterprise = enterprise;
		this.employee = employee;
		this.jobTitle = jobTitle;
	}
	
	public UUID getId() {
		return id;
	}
	
	public Enterprise getEnterprise() {
		return enterprise;
	}
	
	public Employee getEmployee() {
		return employee;
	}

	public String getJobTitle() {
		return jobTitle;
	}
	
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	
	public LocalDate getAdmissionDate() {
		return admissionDate;
	}
	

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
