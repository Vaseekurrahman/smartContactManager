package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.Repository.ContactRepository;
import com.smart.Repository.UserRepository;
import com.smart.config.Message;
import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private UserRepository userRepository;

	// ================= COMMON USER =================
	@ModelAttribute
	public void addUserToModel(Model model, Principal principal) {
		if (principal != null) {
			userRepository.findByEmail(principal.getName())
					.ifPresent(user -> model.addAttribute("user", user));
		}
	}

	// ================= DASHBOARD =================
	@GetMapping("/user_dashboard")
	public String dashboard(Model model, Principal principal) {

		User user = getLoggedUser(principal);

		model.addAttribute("totalContacts", contactRepository.countByUser(user));

		Pageable pageable = PageRequest.of(0, 5, Sort.by("date").descending());
		Page<Contact> recentContacts = contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", recentContacts.getContent());

		return "user/user_dashboard";
	}

	// ================= ADD CONTACT PAGE =================
	@GetMapping("/add_contact")
	public String addcontact() {
		return "user/add_contact";
	}


	//profile page
	@GetMapping("/profile")
	public String profile() {
		return "user/profile";
	}
//settings page
	@GetMapping("/setting")
	public String settings() {
		return "user/setting";

	}
	// ================= VIEW CONTACT =================
	@GetMapping("/view_contact/{page}")
	public String contact(@PathVariable Integer page, Model model, Principal principal) {

		User user = getLoggedUser(principal);

		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalpages", contacts.getTotalPages());

		return "user/view_contact";
	}

	// ================= VIEW SINGLE CONTACT =================
	@GetMapping("/{cId}/view_contact")
	public String viewContactDetails(@PathVariable Integer cId, Model model, Principal principal) {

		User user = getLoggedUser(principal);
		Contact contact = contactRepository.findById(cId)
				.orElseThrow(() -> new RuntimeException("Contact not found"));

		// ✅ FIXED (no .equals on int)
		if (contact.getUser().getId() != user.getId()) {
			model.addAttribute("contact", new Contact());
		} else {
			model.addAttribute("contact", contact);
		}

		return "user/contact_details";
	}

	// ================= DELETE =================
	@Transactional
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable Integer cId, Principal principal,
								RedirectAttributes redirectAttributes) {

		User user = getLoggedUser(principal);

		Contact contact = contactRepository.findById(cId).orElse(null);

		// ✅ FIXED
		if (contact != null && contact.getUser().getId() == user.getId()) {

			contactRepository.delete(contact);

			redirectAttributes.addFlashAttribute("message",
					new Message("Contact deleted successfully ✅", "alert-success"));

		} else {
			redirectAttributes.addFlashAttribute("message",
					new Message("Unauthorized or not found ❌", "alert-danger"));
		}

		return "redirect:/user/view_contact/0";
	}

	// ================= ADD CONTACT =================
	@PostMapping("/process")
	public String addContact(@ModelAttribute Contact contact,
							 @RequestParam("profileImage") MultipartFile file,
							 Principal principal,
							 RedirectAttributes redirectAttributes) {

		try {

			User user = getLoggedUser(principal);

			// Duplicate email check
			List<Contact> exist = contactRepository.findByEmailAndUser(contact.getEmail(), user);

			if (!exist.isEmpty()) {
				redirectAttributes.addFlashAttribute("message",
						new Message("Email already exists ❌", "alert-danger"));
				return "redirect:/user/add_contact";
			}

			// Image upload
			String fileName = handleFileUpload(file);

			contact.setImage(fileName);
			contact.setUser(user);
			contact.setDate(LocalDateTime.now());

			contactRepository.save(contact);

			redirectAttributes.addFlashAttribute("message",
					new Message("Contact added successfully ✅", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("message",
					new Message("Something went wrong ❌", "alert-danger"));
		}

		return "redirect:/user/add_contact";
	}

	// ================= EDIT CONTACT PAGE =================
	@GetMapping("/update_details/{cId}")
	public String editContact(@PathVariable Integer cId,
							  Model model,
							  Principal principal) {

		User user = getLoggedUser(principal);

		Contact contact = contactRepository.findById(cId)
				.orElseThrow(() -> new RuntimeException("Contact not found"));

		if (contact.getUser().getId() != user.getId()) {
			throw new RuntimeException("Unauthorized");
		}

		model.addAttribute("contact", contact);

		return "user/update_details";
	}

	// ================= UPDATE =================
	@PostMapping("/update_details/{cId}")
	public String updateHandler(@ModelAttribute Contact contact,
								@RequestParam(value = "profileImage", required = false) MultipartFile file,
								@PathVariable Integer cId,
								Principal principal,
								RedirectAttributes redirectAttributes) {

		try {

			User user = getLoggedUser(principal);

			Contact oldContact = contactRepository.findById(cId)
					.orElseThrow(() -> new RuntimeException("Contact not found"));

			// ✅ FIXED AUTH CHECK
			if (oldContact.getUser().getId() != user.getId()) {
				throw new RuntimeException("Unauthorized");
			}

			contact.setcId(cId);
			contact.setUser(oldContact.getUser());
			contact.setDate(oldContact.getDate());

			String fileName = file.isEmpty() ? oldContact.getImage() : handleFileUpload(file);
			contact.setImage(fileName);

			contactRepository.save(contact);

			redirectAttributes.addFlashAttribute("message",
					new Message("Updated Successfully ✅", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("message",
					new Message("Update failed ❌", "alert-danger"));
		}

		return "redirect:/user/view_contact/0";
	}

// update contact page
@GetMapping("/update_contact/{cId}")
public String updateContact(@PathVariable Integer cId,
							Model model,
							Principal principal) {

	Contact contact = contactRepository.findById(cId).get();
	model.addAttribute("contact", contact);

	return "user/update_contact";
}

	//changepassword page
	@GetMapping("/change_password")
	public String changePassword() {
		return "user/change_password";
	}


	// ================= CHANGE PASSWORD =================
	@PostMapping("/change_password")
	public String changePassword(Principal principal,
								 @RequestParam String oldPassword,
								 @RequestParam String newPassword,
								 HttpSession session) {

		User user = getLoggedUser(principal);

		if (passwordEncoder.matches(oldPassword, user.getPassword())) {

			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);

			session.setAttribute("message", "Password changed ✅");

		} else {
			session.setAttribute("message", "Wrong old password ❌");
		}

		return "user/change_password";
	}

	// ================= COMMON METHODS =================
	private User getLoggedUser(Principal principal) {
		return userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private String handleFileUpload(MultipartFile file) throws Exception {

		if (file.isEmpty()) return "default.png";

		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

		File saveDir = new ClassPathResource("static/images").getFile();
		Path path = Paths.get(saveDir.getAbsolutePath() + File.separator + fileName);

		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

		return fileName;
	}
}