package com.smart.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.Repository.UserRepository;
import com.smart.config.Message; //  Correct import
import com.smart.entities.User;

import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String home(Model model) {
		return "home";
	}


	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for registering user

	@GetMapping("/contactus")
	public String HomeContact() {
		return "contactus";// Default constructor

	}


	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("user") User user,
						   BindingResult result1,
						   @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
						   RedirectAttributes redirectAttributes) {

		try {

			if (!agreement) {
				redirectAttributes.addFlashAttribute("message",
						new Message("Please accept Terms & Conditions ❌", "alert-danger"));
				return "redirect:/signup";
			}

			if (result1.hasErrors()) {
				System.out.println("Validation Error");
				return "signup";
			}

			Optional<User> exist = userRepository.findByEmail(user.getEmail());

			if (exist.isPresent()) {
				redirectAttributes.addFlashAttribute("message",
						new Message("Email already exists ❌", "alert-danger"));
				return "redirect:/signup";
			}

			user.setRole("ROLE_USER");
			user.setDate(LocalDateTime.now());
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			userRepository.save(user);

			redirectAttributes.addFlashAttribute("message",
					new Message("Registered Successfully ✅", "alert-success"));

			return "redirect:/signup";

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("message",
					new Message("Something went wrong ❌", "alert-danger"));
			return "redirect:/signup";
		}
	}

	@GetMapping("/login")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}



}