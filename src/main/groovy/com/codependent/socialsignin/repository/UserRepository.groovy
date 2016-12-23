package com.codependent.socialsignin.repository

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserRepository {

	private Logger logger = LoggerFactory.getLogger(getClass())
	
	@Autowired
	private JdbcTemplate jdbcTemplate
	
	void createUser(String username, String password, List<String> authorities){
		logger.info 'User not registered - manual sign up - user[{}], password[{}], roles[{}]', username, password, authorities
		jdbcTemplate.update('insert into users(username,password,enabled) values(?,?,?)', username, password, true)
		for(String authority : authorities){
			jdbcTemplate.update('insert into authorities(username,authority) values(?,?)', username, authority)
		}
		
	}
	
}
