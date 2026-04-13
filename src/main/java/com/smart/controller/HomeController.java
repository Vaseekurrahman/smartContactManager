package com.smart.controller;

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

	@GetMapping("/me")
	public String home1() {
		return "abbout";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			RedirectAttributes redirectAttributes) {

		try {

			if (!agreement) {
				throw new Exception("You have not agreed the terms and condition");
			}

			if (result1.hasErrors()) {
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setDate(LocalDateTime.now());
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			Optional<User> exist = userRepository.findByEmail(user.getEmail());

			if (exist.isPresent()) {
				redirectAttributes.addFlashAttribute("message", new Message("Email already exists ❌", "alert-danger"));
				return "redirect:/signup";
			}

			userRepository.save(user);
			redirectAttributes.addFlashAttribute("message",
					new Message("Successfully Registered Successfully!!", "alert-success"));
			return "redirect:/signup";
		} catch (Exception e) {
			model.addAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));

			return "signup";
		}
	}

	@GetMapping("/login")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}

}