package com.codependent.socialsignin.security


import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.social.security.SocialUser
import org.springframework.social.security.SocialUserDetails
import org.springframework.social.security.SocialUserDetailsService
import org.springframework.social.security.SpringSocialConfigurer

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private DataSource dataSource
	
	@Autowired
	void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.jdbcAuthentication()
				.dataSource(dataSource)
				.withDefaultSchema()
				.withUser("joseantonio.inigo@gmail.com").password("mypassword").roles("USER");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		SpringSocialConfigurer ssc = new SpringSocialConfigurer()
		ssc.alwaysUsePostLoginUrl true
		ssc.postLoginUrl "/secure-home"
		
		http
			.authorizeRequests()
				.antMatchers("/secure*").authenticated()
				.and()
			.formLogin()
				.loginPage("/login").permitAll()
				.defaultSuccessUrl("/secure-home")
				//.loginProcessingUrl("/secure-home")
				.failureUrl("/login?param.error=bad_credentials")
				.and()
			.rememberMe().key("rememberMeKey")
				.and()
			.logout()
				.logoutUrl("/logout")
				.deleteCookies("JSESSIONID")
				.and()
			.apply(ssc)
	}
	
	@Bean
	public SocialUserDetailsService socialUserDetailsService(){
		UserDetailsService userDetailsService = userDetailsService()
		return new SocialUserDetailsService(){
			@Override
			SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException{
				UserDetails userDetails = userDetailsService.loadUserByUsername userId
				new SocialUser(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities())
			}
		}
	}
	
}
