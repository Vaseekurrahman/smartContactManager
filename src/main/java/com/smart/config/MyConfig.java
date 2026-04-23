package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());

		return provider;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
												   UserDetailsService userDetailsService) throws Exception {

		http.authenticationProvider(authenticationProvider(userDetailsService));

		http.authorizeHttpRequests(auth -> auth
						.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
						.requestMatchers("/", "/login", "/signup", "/register").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/user/**").hasRole("USER")
						.anyRequest().authenticated()
				)

				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/doLogin")
						.defaultSuccessUrl("/user/user_dashboard", true)
						.failureUrl("/login?error=true")
						.permitAll()
				)

				.logout(logout -> logout.logoutSuccessUrl("/login?logout=true"));

		return http.csrf(csrf -> csrf.disable()).build();
	}
}