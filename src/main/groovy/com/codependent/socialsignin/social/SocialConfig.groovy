package com.codependent.socialsignin.social

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.core.env.Environment
import org.springframework.social.UserIdSource
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer
import org.springframework.social.config.annotation.EnableSocial
import org.springframework.social.config.annotation.SocialConfigurerAdapter
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.ConnectionSignUp
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository
import org.springframework.social.facebook.api.Facebook
import org.springframework.social.facebook.api.User
import org.springframework.social.facebook.connect.FacebookConnectionFactory
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleConnectionFactory
import org.springframework.social.security.AuthenticationNameUserIdSource

@Configuration
@EnableSocial
class SocialConfig extends SocialConfigurerAdapter{

	@Override
	void addConnectionFactories(ConnectionFactoryConfigurer cfConfig, Environment env) {
		FacebookConnectionFactory fcf = new FacebookConnectionFactory(env.getProperty("facebook.clientId"), env.getProperty("facebook.clientSecret"))
		fcf.setScope("public_profile,email")
		cfConfig.addConnectionFactory(fcf)
		
		GoogleConnectionFactory gcf = new GoogleConnectionFactory(env.getProperty("google.clientId"), env.getProperty("google.clientSecret"))
		gcf.setScope("openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo#email https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/tasks https://www-opensocial.googleusercontent.com/api/people https://www.googleapis.com/auth/plus.login");
		cfConfig.addConnectionFactory(gcf);
	}
	
	@Bean
	@Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
	Facebook facebook(ConnectionRepository repository) {
		Connection<Facebook> connection = repository.findPrimaryConnection(Facebook.class);
		return connection != null ? connection.getApi() : null;
	}
	
	@Bean
	@Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
	Google google(ConnectionRepository repository) {
		Connection<Google> connection = repository.findPrimaryConnection(Google.class);
		return connection != null ? connection.getApi() : null;
	}
	
	@Override
	UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
		//return new JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, Encryptors.noOpText());
		InMemoryUsersConnectionRepository rep = new InMemoryUsersConnectionRepository(connectionFactoryLocator)
		rep.setConnectionSignUp(new ConnectionSignUp(){
			public String execute(Connection<?> connection){
				Facebook facebook = (Facebook)connection.getApi();
				String [] fields = [ "id", "email",  "first_name", "last_name", "about" , "gender" ];
				User userProfile = facebook.fetchObject(connection.getKey().getProviderUserId(), User.class, fields);
				return userProfile.getEmail();
			}
		})
		return rep;
	}
	
	@Override
	UserIdSource getUserIdSource() {
		return new AuthenticationNameUserIdSource()
	}
	
	/*
	@Bean
	@Scope(value="singleton", proxyMode=ScopedProxyMode.INTERFACES)
	public ConnectionFactoryLocator connectionFactoryLocator() {
		ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry()
	
		registry.addConnectionFactory(new FacebookConnectionFactory(
			environment.getProperty("facebook.clientId"),
			environment.getProperty("facebook.clientSecret")))
	
		return registry
	}*/
	
	
	
}
