package io.fabric8.quickstarts.camel;
import com.github.choonchernlim.security.adfs.saml2.DefaultSAMLBootstrap;
import com.github.choonchernlim.security.adfs.saml2.SAMLConfigBean;
import com.github.choonchernlim.security.adfs.saml2.SAMLConfigBeanBuilder;
import com.github.choonchernlim.security.adfs.saml2.SAMLWebSecurityConfigurerAdapter;

import org.opensaml.xml.signature.SignatureConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml.SAMLBootstrap;

@EnableWebSecurity
class AppSecurityConfig extends SAMLWebSecurityConfigurerAdapter {

    // See `SAMLConfigBean Properties` section below for more info. 
    @Override
    protected SAMLConfigBean samlConfigBean() {
        return new SAMLConfigBeanBuilder()
                .withIdpServerName("gpadfs.grameenphone.com")
                .withSpServerName("localhost")
                .withSpContextPath("")
                .withKeystoreResource(new DefaultResourceLoader().getResource("classpath:server.jks"))
                .withKeystorePassword("mykeypass")
                .withKeystoreAlias("alias")
                .withKeystorePrivateKeyPassword("mykeypass")
                .withSuccessLoginDefaultUrl("/")
                .withSuccessLogoutUrl("/goodbye")
                .withStoreCsrfTokenInCookie(true)
                .build();
    }

    // This configuration is not needed if your signature algorithm is SHA256withRSA and 
    // digest algorithm is SHA-256. However, if you are using different algorithm(s), then
    // add this bean with the correct algorithms.
    @Bean
    public static SAMLBootstrap samlBootstrap() {
        return new DefaultSAMLBootstrap("RSA",
                                        SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512,
                                        SignatureConstants.ALGO_ID_DIGEST_SHA512);
    }

    // call `samlizedConfig(http)` first to decorate `http` with SAML configuration
    // before configuring app specific HTTP security
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        samlizedConfig(http)
                .authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();
    }

    // call `samlizedConfig(web)` first to decorate `web` with SAML configuration 
    // before configuring app specific web security
    @Override
    public void configure(final WebSecurity web) throws Exception {
        samlizedConfig(web).ignoring().antMatchers("/resources/**");
    }
}