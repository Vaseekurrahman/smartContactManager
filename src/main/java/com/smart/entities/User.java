package com.smart.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")   // ✅ FIXED (important)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@NotBlank(message = "Name field is required")
	@Size(min = 2, max = 20, message = "min 2 and maximum 20 character are allowed !!")
	private String name;

	@Column(unique = true)
	private String email;

	private String password;
	private String role;
	private boolean enabled;
	private String imageUrl;
	private LocalDateTime date;

	@Column(length = 500)
	private String about;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
	private List<Contact> contacts = new ArrayList<>();

	// ===== CONSTRUCTOR =====
	public User() {}

	// ===== GETTERS SETTERS =====
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }

	public String getImageUrl() { return imageUrl; }
	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

	public String getAbout() { return about; }
	public void setAbout(String about) { this.about = about; }

	public List<Contact> getContacts() { return contacts; }
	public void setContacts(List<Contact> contacts) { this.contacts = contacts; }

	public LocalDateTime getDate() { return date; }
	public void setDate(LocalDateTime date) { this.date = date; }

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + "]";
	}
}