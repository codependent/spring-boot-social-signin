package com.codependent.socialsignin.web

import javax.servlet.http.HttpSession

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.UserProfile
import org.springframework.social.connect.web.ProviderSignInAttempt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SignupController {

	Logger logger = LoggerFactory.getLogger(this.getClass())
	
	@Autowired
	ConnectionFactoryLocator connectionFactoryLocator
	
	@GetMapping("/signup")
	void signupForm(HttpSession session){
		ProviderSignInAttempt attemp = (ProviderSignInAttempt)session.getAttribute('org.springframework.social.connect.web.ProviderSignInAttempt')
		if(attemp != null){
			//Social signup
			Connection<?> connection = providerSignInUtils.getConnection(request);
			if (connection != null) {
				UserProfile userProfile = connection.fetchUserProfile()
				println userProfile
			} else {
				//TODO  Normal signup - no social sign in
			}
		}else{
			//TODO Normal signup - no social sign in
		}
	}
	
}
