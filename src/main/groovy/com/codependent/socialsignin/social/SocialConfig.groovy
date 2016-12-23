package com.codependent.socialsignin.social

import java.security.SecureRandom
import java.sql.ResultSet
import java.sql.SQLException

import javax.sql.DataSource

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.social.UserIdSource
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer
import org.springframework.social.config.annotation.EnableSocial
import org.springframework.social.config.annotation.SocialConfigurerAdapter
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.ConnectionSignUp
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository
import org.springframework.social.facebook.api.Facebook
import org.springframework.social.facebook.api.User
import org.springframework.social.facebook.connect.FacebookConnectionFactory
import org.springframework.social.google.api.Google
import org.springframework.social.google.api.plus.Person
import org.springframework.social.google.connect.GoogleConnectionFactory
import org.springframework.social.security.AuthenticationNameUserIdSource

import com.codependent.socialsignin.repository.UserRepository;

@Configuration
@EnableSocial
class SocialConfig extends SocialConfigurerAdapter{

	private Logger logger = LoggerFactory.getLogger(getClass())
	
	private SecureRandom random = new SecureRandom()
	
	@Value('${spring.security.social.implicit-singup:true}')
	private boolean implicitSignup
	
	@Autowired
	private DataSource dataSource
	
	@Autowired
	private JdbcTemplate jdbcTemplate
	
	@Autowired
	private UserRepository userRepository
	
	@Override
	void addConnectionFactories(ConnectionFactoryConfigurer cfConfig, Environment env) {
		FacebookConnectionFactory fcf = new FacebookConnectionFactory(env.getProperty("facebook.clientId"), env.getProperty("facebook.clientSecret"))
		fcf.scope = "public_profile,email"
		cfConfig.addConnectionFactory fcf
		
		GoogleConnectionFactory gcf = new GoogleConnectionFactory(env.getProperty("google.clientId"), env.getProperty("google.clientSecret"))
		gcf.scope = "openid profile email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo#email https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/tasks https://www-opensocial.googleusercontent.com/api/people https://www.googleapis.com/auth/plus.login"
		cfConfig.addConnectionFactory gcf
	}
	
	@Bean
	@Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
	Facebook facebook(ConnectionRepository repository) {
		Connection<Facebook> connection = repository.findPrimaryConnection Facebook.class
		return connection != null ? connection.api : null
	}
	
	@Bean
	@Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
	Google google(ConnectionRepository repository) {
		Connection<Google> connection = repository.findPrimaryConnection Google.class
		return connection != null ? connection.api : null
	}
	
	@Override
	UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
		UsersConnectionRepository rep = new JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, Encryptors.noOpText())
		//UsersConnectionRepository rep = new InMemoryUsersConnectionRepository(connectionFactoryLocator)
		if(implicitSignup){
			rep.connectionSignUp = new ConnectionSignUp(){
				String execute(Connection<?> connection){
					def email
					if(connection.api instanceof Facebook){
						Facebook facebook = (Facebook)connection.api
						String [] fields = [ "id", "email",  "first_name", "last_name", "about" , "gender" ]
						User userProfile = facebook.fetchObject connection.key.providerUserId, User.class, fields
						email = userProfile.email
					}else if(connection.api instanceof Google){
						Google google = (Google)connection.api
						Person userProfile = google.plusOperations().googleProfile
						email = userProfile.emailAddresses.iterator().next()
					}else{
						throw new UnsupportedOperationException("connection no soportado: " + connection)
					}
					verifyExistingUser email
					email
				}
			}
		}
		rep
	}
	
	@Override
	UserIdSource getUserIdSource() {
		return new AuthenticationNameUserIdSource()
	}

	/**
	 * Checks wether the user is already registered in the user table. If not it creates it with a temporal random password and USER role
	 * @author JINGA4X
	 *
	 */
	private void verifyExistingUser(String id){
		List<UserDetails> user = jdbcTemplate.query(JdbcDaoImpl.DEF_USERS_BY_USERNAME_QUERY,
			new RowMapper<UserDetails>() {
				public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
					String username = rs.getString 1
					String password = rs.getString 2 
					boolean enabled = rs.getBoolean 3
					new org.springframework.security.core.userdetails.User(username, password, AuthorityUtils.NO_AUTHORITIES)
				}
		}, id)
		if(user.isEmpty()){
			//User not registered in the application. Manual sign up
			String password = new BigInteger(130, random).toString 32
			logger.info 'User not registered - manual sign up - user[{}], password[{}], roles[{}]', id, password, 'USER'
			userRepository.createUser id, password, ['USER']
		}else{
			logger.info 'User already registered in application'
		}
	}
	
}
