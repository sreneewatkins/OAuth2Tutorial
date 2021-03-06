package com.tts.OAuth2Tutorial;

//https://spring.io/guides/tutorials/spring-boot-oauth2/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@RestController
@SpringBootApplication
public class OAuth2TutorialApplication extends WebSecurityConfigurerAdapter {

	@GetMapping("/user")
	public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {

		return Collections.singletonMap("name", principal.getAttribute("name"));
	}

	@GetMapping("/error")
	public String error() {
		HttpServletRequest request = null;
		String message = (String) request.getSession().getAttribute("error.message");
		request.getSession().removeAttribute("error.message");
		return message;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
				.authorizeRequests(a -> a
						.antMatchers("/", "/error", "/webjars/**").permitAll()
						.anyRequest().authenticated()
				)
				.exceptionHandling(e -> e
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				)
				.logout(l -> l
						.logoutSuccessUrl("/").permitAll()
				)
				.csrf(c -> c
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				)
//				.oauth2Login();
				.oauth2Login(o -> o
						.failureHandler((request, response, exception) -> {
							request.getSession().setAttribute("error.message", exception.getMessage());
							AuthenticationFailureHandler handler = null;
							handler.onAuthenticationFailure(request, response, exception);
						})
				);
		// @formatter:on
	}

	public static void main(String[] args) {

		SpringApplication.run(OAuth2TutorialApplication.class, args);
	}

}//end OAuth2TutorialApplication class
