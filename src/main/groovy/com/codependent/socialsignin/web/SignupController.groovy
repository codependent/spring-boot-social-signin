package com.codependent.socialsignin.web

import javax.servlet.http.HttpSession

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.UserProfile
import org.springframework.social.connect.web.ProviderSignInAttempt
import org.springframework.social.security.SocialAuthenticationToken
import org.springframework.social.security.SocialUserDetails
import org.springframework.social.security.SocialUserDetailsService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SignupController {

	Logger logger = LoggerFactory.getLogger(this.getClass())
	
	@Autowired
	ConnectionFactoryLocator connectionFactoryLocator
	
	@Autowired
	ConnectionRepository connectionRepository
	
	@Autowired
	SocialUserDetailsService socialUserDetailsService;
	
	@GetMapping("/signup")
	def signupForm(HttpSession session){
		ProviderSignInAttempt attemp = (ProviderSignInAttempt)session.getAttribute('org.springframework.social.connect.web.ProviderSignInAttempt')
		if(attemp != null){
			//Social signup
			Connection<?> conn = attemp.getConnection(connectionFactoryLocator)
			if (conn != null) {
				//Automatic Account association
				UserProfile userProfile = conn.fetchUserProfile()
				connectionRepository.addConnection(conn);
				
				//Authentication and redirect to private home page
				//SocialUserDetails userDetails = socialUserDetailsService.loadUserByUserId(conn.getKey().key.providerUserId)
				println conn.key.providerUserId
				SocialUserDetails userDetails = socialUserDetailsService.loadUserByUserId(userProfile.email)
				SocialAuthenticationToken authentication = new SocialAuthenticationToken(conn, userDetails, [:], userDetails.getAuthorities())
				SecurityContextHolder.getContext().setAuthentication(authentication);
				'redirect:/secure-home'
			} else {
				//TODO  Normal signup - no social sign in
				'signup'
			}
		}else{
			//TODO Normal signup - no social sign in
			'signup'
		}
	}
	
}
