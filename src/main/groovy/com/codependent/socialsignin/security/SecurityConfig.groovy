package com.codependent.socialsignin.security


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetails
import org.springframework.social.security.SocialUserDetailsService
import org.springframework.social.security.SpringSocialConfigurer

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter{

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
			.logout()
				.logoutUrl("/logout")
				.deleteCookies("JSESSIONID")
				.and()
			/*.rememberMe()
				.and()*/
			.apply(ssc)
	}
	
	@Bean
	public SocialUserDetailsService socialUserDetailsService(){
		return new SocialUserDetailsService(){
			@Override
			public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException{
				new SocialUser(userId, "HIDDEN", [])
			}
		}
	}
	
}
