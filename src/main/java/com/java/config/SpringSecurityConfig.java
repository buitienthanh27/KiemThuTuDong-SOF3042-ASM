package com.java.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.java.service.VeganaService;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

	@Autowired
	@Lazy
	private VeganaService veganaService;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService((UserDetailsService) veganaService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				// Các trang không yêu cầu login
				.requestMatchers("/", "/login", "/logout").permitAll()
				// Trang /checkout yêu cầu phải login với vai trò ROLE_USER hoặc ROLE_ADMIN
				.requestMatchers("/checkout").hasAnyRole("USER", "ADMIN")
				// Trang chỉ dành cho ADMIN
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().permitAll()
			)
			.exceptionHandling(exception -> exception
				.accessDeniedPage("/403")
			)
			.formLogin(form -> form
				.loginProcessingUrl("/doLogin")
				.loginPage("/login")
				.defaultSuccessUrl("/?login_success")
				.successHandler(new SuccessHandler())
				.failureUrl("/login?error=true")
				.usernameParameter("customerId")
				.passwordParameter("password")
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/")
			)
			.rememberMe(remember -> remember
				.rememberMeParameter("remember")
			)
			.authenticationProvider(authenticationProvider());

		//oauth2- đăng nhập từ mxh
		// http.oauth2Login(oauth2 -> oauth2
		// 	.loginPage("/login")
		// 	.defaultSuccessUrl("/", true)
		// 	.failureUrl("/login")
		// );

		return http.build();
	}

}
