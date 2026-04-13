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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

	// User Dashboard

	@GetMapping("/user_dashboard")
	public String dashboard(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME " + userName);
		String email = principal.getName();
		Optional<User> optionalUser = userRepository.findByEmail(email);
		User user = optionalUser.get();

		model.addAttribute("user", user);
		return "user/user_dashboard";
	}


	// Add Contact

	@GetMapping("/add_contact")
	public String addcontact() {
		return "user/add_contact";
	}

	// View Contacts

	@GetMapping("/view_contact/{page}")
	public String contact(@PathVariable("page") Integer page, Model model, Principal principal) {

		String userName = principal.getName();
		User user = this.userRepository.findByEmail(userName).get();
		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalpages", contacts.getTotalPages());
		return "user/view_contact";
	}

	// showing particullar contact details


	@RequestMapping("/{cId}/view_contact")
	public String viewContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		String username = principal.getName();
		User user = userRepository.findByEmail(username).get();

		Optional<Contact> contactOptional = contactRepository.findById(cId);

		if (contactOptional.isPresent() && contactOptional.get().getUser().getId() == user.getId()) {

			model.addAttribute("contact", contactOptional.get());

		} else {
			model.addAttribute("contact", new Contact());
		}

		return "user/contact_details";
	}


	// setting

	@GetMapping("/setting")
	public String setting() {
		return "user/setting";
	}

	// profile

	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {

		String username = principal.getName();

		Optional<User> optionalUser = this.userRepository.findByEmail(username);

		model.addAttribute("user", optionalUser.get());

		return "user/profile";
	}

	@GetMapping("/logout")
	public String logout() {
		return "login";
	}

	@GetMapping("/home")
	public String home() {
		return "user/home";
	}

	// ================= UPDATE FORM OPEN =================

	@GetMapping("/update_details/{cId}")
	public String showUpdateForm(@PathVariable("cId") Integer cId, Model model) {
		Contact contact = contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);

		return "user/update_details";
	}

	@GetMapping("/update_contact/{cId}")
	public String showUpdateFormContact(@PathVariable("cId") Integer cId, Model model) {
		Contact contact = contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);
		return "user/update_contact";
	}

	// ================= UPDATE HANDLER =================

	@PostMapping("/update_details/{cId}")
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam(value = "profileImage", required = false) MultipartFile file,
			@PathVariable("cId") Integer cId, RedirectAttributes redirectAttributes) {

		try {

			// Old data fetch
			Contact oldContact = contactRepository.findById(cId).get();
			System.out.println(oldContact);

			// IMPORTANT (data loss fix)
			contact.setcId(cId);
			contact.setUser(oldContact.getUser());
			contact.setDate(oldContact.getDate());

			// Image handling
			if (!file.isEmpty()) {
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}


			// Save updated contact
			contactRepository.save(contact);

			redirectAttributes.addFlashAttribute("message",
					new Message("Contact Updated Successfully ✅", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();

			redirectAttributes.addFlashAttribute("message", new Message("Something went wrong ❌", "alert-danger"));
		}

		return "redirect:/user/update_details/" + cId;
	}

	// delete the contact

	@Transactional
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Principal principal,
			RedirectAttributes redirectAttributes) {

		Optional<Contact> contactOptional = contactRepository.findById(cId);

		if (contactOptional.isPresent()) {
			Contact contact = contactOptional.get();

			// Optional: check ownership
			if (contact.getUser().getEmail().equals(principal.getName())) {
				contactRepository.delete(contact);
				contactRepository.flush(); // DB me immediately reflect
				redirectAttributes.addFlashAttribute("message",
						new Message("Contact deleted successfully ✅", "alert-success"));
			} else {
				redirectAttributes.addFlashAttribute("message",
						new Message("You cannot delete this contact ❌", "alert-danger"));
			}

		} else {
			redirectAttributes.addFlashAttribute("message", new Message("Contact not found ❌", "alert-danger"));
		}

		return "redirect:/user/view_contact/0";
	}

	// Process the data

	@PostMapping("/process")
	public String addContact(Model model, @ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal,
			RedirectAttributes redirectAttributes) {

		try {
			String name = principal.getName();
			User user = userRepository.findByEmail(name).get();

			if (file.isEmpty()) {
				contact.setImage("regulatory.png");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); // ✅ fixed
			}

			contact.setUser(user);

			List<Contact> exist = this.contactRepository.findByEmail(contact.getEmail());

			if (!exist.isEmpty()) {
				redirectAttributes.addFlashAttribute("message", new Message("Email already exists ❌", "alert-danger"));
				return "redirect:/user/add_contact";
			}

			contact.setDate(LocalDateTime.now());
			contactRepository.save(contact);

			redirectAttributes.addFlashAttribute("message",
					new Message("Contact added successfully ✅", "alert-success"));

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", new Message("Something went wrong ❌", "alert-danger"));
			e.printStackTrace();
		}

		return "redirect:/user/add_contact";
	}

	// change password

	@GetMapping("/change_password")
	public String changePassword(Model model, @ModelAttribute User user, Principal principal) {
		String name = principal.getName();

		System.out.println(name);
		return "user/change_password";
	}

	@PostMapping("/change_password")
	public String changePasswordP(Model model, Principal principal, @RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, HttpSession session) {

		String username = principal.getName();

		User currentUser = userRepository.findByEmail(username).get();

		// Old password check (encrypted compare)
		if (passwordEncoder.matches(oldPassword, currentUser.getPassword())) {

			// New password encrypt karke save karo
			currentUser.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(currentUser);

			session.setAttribute("message", "✅ Password changed successfully");

		} else {
			session.setAttribute("message", "❌ Wrong old password");
		}

		return "user/change_password";
	}

}
