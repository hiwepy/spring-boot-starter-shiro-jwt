package org.apache.shiro.spring.boot;

import org.apache.shiro.spring.boot.biz.ShiroBizFilterFactoryBean;
import org.apache.shiro.spring.boot.jwt.authc.JwtAuthenticatingFilter;
import org.apache.shiro.spring.boot.jwt.authz.JwtAuthorizationFilter;
import org.apache.shiro.spring.boot.jwt.authz.JwtIssueFilter;
import org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.AbstractShiroWebFilterConfiguration;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnWebApplication
@AutoConfigureBefore(ShiroWebAutoConfiguration.class)
@ConditionalOnClass()
@ConditionalOnProperty(prefix = ShiroJwtProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({ ShiroBizProperties.class, ShiroJwtProperties.class })
public class ShiroJwtWebFilterConfiguration extends AbstractShiroWebFilterConfiguration implements ApplicationContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(ShiroJwtWebFilterConfiguration.class);
	private ApplicationContext applicationContext;
	
	@Autowired
	private ShiroBizProperties properties;
	@Autowired
	private ShiroJwtProperties casProperties;
	@Autowired
	private ServerProperties serverProperties;

	/**
	 * JSON Web Token (JWT) Authentication Filter </br>
	 * 该过滤器负责用户的认证工作
	 */
	@Bean("authc")
	@ConditionalOnMissingBean(name = "authc")
	public FilterRegistrationBean<JwtAuthorizationFilter> authenticationFilter(ShiroJwtProperties properties){
		FilterRegistrationBean<JwtAuthorizationFilter> registration = new FilterRegistrationBean<JwtAuthorizationFilter>(); 
		JwtAuthorizationFilter authenticationFilter = new JwtAuthorizationFilter();
		//authenticationFilter.setFailureUrl(properties.getFailureUrl());
		registration.setFilter(authenticationFilter);
	    registration.setEnabled(false); 
	    return registration;
	}
	
	/**
	 * JSON Web Token (JWT) Issue Filter </br>
	 * 该过滤器负责对Token的校验工作
	 */
	@Bean("issue")
	@ConditionalOnMissingBean(name = "issue")
	public FilterRegistrationBean<JwtIssueFilter> jwtIssueFilter(ShiroJwtProperties properties){
		FilterRegistrationBean<JwtIssueFilter> registration = new FilterRegistrationBean<JwtIssueFilter>(); 
		JwtIssueFilter jwtIssueFilter = new JwtIssueFilter();
		//jwtIssueFilter.setFailureUrl(properties.getFailureUrl());
		registration.setFilter(jwtIssueFilter);
	    registration.setEnabled(false); 
	    return registration;
	}
	
	/**
	 * JSON Web Token (JWT) Validation Filter </br>
	 * 该过滤器负责对Token的校验工作
	 */
	@Bean("token")
	@ConditionalOnMissingBean(name = "token")
	public FilterRegistrationBean<JwtAuthenticatingFilter> authenticatingFilter(ShiroJwtProperties properties){
		FilterRegistrationBean<JwtAuthenticatingFilter> registration = new FilterRegistrationBean<JwtAuthenticatingFilter>(); 
		JwtAuthenticatingFilter authenticatingFilter = new JwtAuthenticatingFilter();
		//authenticatingFilter.setFailureUrl(properties.getFailureUrl());
		registration.setFilter(authenticatingFilter);
	    registration.setEnabled(false); 
	    return registration;
	}
	
	@Bean
    @ConditionalOnMissingBean
    @Override
    protected ShiroFilterFactoryBean shiroFilterFactoryBean() {
		
		ShiroFilterFactoryBean filterFactoryBean = new ShiroBizFilterFactoryBean();
		//系统主页：登录成功后跳转路径
        filterFactoryBean.setSuccessUrl(properties.getSuccessUrl());
        //异常页面：无权限时的跳转路径
        filterFactoryBean.setUnauthorizedUrl(properties.getUnauthorizedUrl());
        
        //必须设置 SecurityManager
   		filterFactoryBean.setSecurityManager(securityManager);
   		//拦截规则
        filterFactoryBean.setFilterChainDefinitionMap(shiroFilterChainDefinition.getFilterChainMap());
        
        return filterFactoryBean;
        
    }

    @Bean(name = "filterShiroFilterRegistrationBean")
    @ConditionalOnMissingBean
    protected FilterRegistrationBean<AbstractShiroFilter> filterShiroFilterRegistrationBean() throws Exception {

        FilterRegistrationBean<AbstractShiroFilter> filterRegistrationBean = new FilterRegistrationBean<AbstractShiroFilter>();
        filterRegistrationBean.setFilter((AbstractShiroFilter) shiroFilterFactoryBean().getObject());
        filterRegistrationBean.setOrder(Integer.MAX_VALUE);

        return filterRegistrationBean;
    }
    
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
}
