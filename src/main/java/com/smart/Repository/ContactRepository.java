package com.smart.Repository;

import java.util.List;

import com.smart.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// 🔍 Find by email (global)
	List<Contact> findByEmail(String email);

	// 🔍 Find by email + user (IMPORTANT FIX)
	List<Contact> findByEmailAndUser(String email, User user);

	// 📄 Pagination (User contacts)
	@Query("from Contact c where c.user.id = :userId")
	Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);

	// 🔍 Search by name
	List<Contact> findByNameContainingAndUser(String name, User user);

	// 🔢 Count contacts
	long countByUser(User user);

}