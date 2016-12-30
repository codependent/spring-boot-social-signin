package com.codependent.socialsignin.web

import javax.servlet.http.HttpSession

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.NoSuchConnectionException
import org.springframework.social.connect.UserProfile
import org.springframework.social.connect.UserProfileBuilder
import org.springframework.social.connect.web.ProviderSignInAttempt
import org.springframework.social.facebook.api.Facebook
import org.springframework.social.facebook.api.User
import org.springframework.social.security.SocialAuthenticationToken
import org.springframework.social.security.SocialUserDetails
import org.springframework.social.security.SocialUserDetailsService
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

import com.codependent.socialsignin.repository.UserRepository
import com.codependent.socialsignin.web.dto.SignupInfo

@Controller
class SignupController {

	Logger logger = LoggerFactory.getLogger(this.getClass())
	
	@Autowired
	ConnectionFactoryLocator connectionFactoryLocator
	
	@Autowired
	ConnectionRepository connectionRepository
	
	@Autowired
	SocialUserDetailsService socialUserDetailsService;
	
	@Autowired
	UserRepository userRepository
	
	@GetMapping("/signup")
	def signupForm(ModelMap model, HttpSession session){
		ProviderSignInAttempt attemp = (ProviderSignInAttempt)session.getAttribute('org.springframework.social.connect.web.ProviderSignInAttempt')
		if(attemp != null){
			//Social signup
			Connection<?> conn = attemp.getConnection(connectionFactoryLocator)
			if (conn != null) {
				UserProfile userProfile = null;
				if(conn.getApi() instanceof Facebook){
					//XXX Workaround for Spring Social bug with Facebook API 2.8
					String [] fields = [ "id", "email",  "first_name", "last_name", "about" , "gender" ]
					User facebookUser = conn.api.fetchObject conn.key.providerUserId, User.class, fields
					userProfile= new UserProfileBuilder().setName(facebookUser.getName()).setFirstName(facebookUser.getFirstName())
														 .setLastName(facebookUser.getLastName())
														 .setEmail(facebookUser.getEmail()).build()
				}else{
					userProfile = conn.fetchUserProfile()
				}
				try{
					//Authentication and redirect to private home page
					SocialUserDetails userDetails = socialUserDetailsService.loadUserByUserId userProfile.email
					SocialAuthenticationToken authentication = new SocialAuthenticationToken(conn, userDetails, [:], userDetails.getAuthorities())
					SecurityContextHolder.getContext().setAuthentication(authentication);
					try{
						//Account association
						connectionRepository.getConnection(conn.key)
						println 'here'
					}catch(NoSuchConnectionException ex){
						println 'here2'
						connectionRepository.addConnection(conn)
					}
					'redirect:/secure-home'
				}catch(UsernameNotFoundException ex){
					//Unregistered user - normal signup with prepopulated fields
					model.put 'signupInfo', new SignupInfo( username: userProfile.email, password : '', repeatedPassword: '')
					'signup'
				}
			} else {
				//Normal signup - no social sign in
				model.put 'signupInfo', new SignupInfo()
				'signup'
			}
		}else{
			//Normal signup - no social sign in
			model.put 'signupInfo', new SignupInfo()
			'signup'
		}
	}
	
	@PostMapping('/signup')
	def signupFormPost(@ModelAttribute SignupInfo signupInfo){
		logger.info 'signupFormPost [{}]', signupInfo
		userRepository.createUser signupInfo.username, signupInfo.password, ['USER']
		'login'
	}
	
}
