package com.codependent.socialsignin.web.dto

import java.security.Principal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

import groovy.transform.ToString;

@ToString
class SignupInfo {

	String username
	String password
	String repeatedPassword
	
}
