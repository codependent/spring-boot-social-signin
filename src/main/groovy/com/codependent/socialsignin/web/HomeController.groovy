package com.codependent.socialsignin.web

import java.security.Principal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

	Logger logger = LoggerFactory.getLogger(this.getClass())
	
	@GetMapping( value = ['/' , '/home'] )
	def home(){
		logger.info '/home'
		'home'
	}
	
	@GetMapping('/secure-home')
	void secureHome(Principal principal){
		logger.info '/secure-home -> securityContextHolder authentication[{}]', SecurityContextHolder.context.authentication.name
		logger.info '/secure-home -> principal[{}]', principal.name
	}
	
}
