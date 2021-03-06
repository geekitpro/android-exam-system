package com.msxt.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="examination")
public class Examination {	
	@Id
	@Column(name="ID")
	@GenericGenerator(name="uuidGG",strategy="uuid")
	@GeneratedValue(generator="uuidGG")
	private String id;
	
	@Version
	private Integer version;
	
	@ManyToOne
	@JoinColumn(name="position_id")
	private Position position;
	
	@OneToMany(mappedBy = "exam", cascade = {CascadeType.ALL, CascadeType.REMOVE})
	private List<ExaminationCatalog> catalogs;
	
	@Column(name="name")
	private String name;
	
	@Column(name="time")
	private int time = 45;
	
	@Transient
	private boolean onUsed = false;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public List<ExaminationCatalog> getCatalogs() {
		if( catalogs==null )
			catalogs = new ArrayList<ExaminationCatalog>();
		return catalogs;
	}
	public void setCatalogs(List<ExaminationCatalog> catalogs) {
		this.catalogs = catalogs;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Integer getVersion() {
		return version;
	}
	public boolean isOnUsed() {
		return onUsed;
	}
	public void setOnUsed(boolean onUsed) {
		this.onUsed = onUsed;
	}
}
