package xyz.keinthema.serverims.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import xyz.keinthema.serverims.handler.JwtAuthFilter
import xyz.keinthema.serverims.handler.ShaSaltedPasswordEncoder


@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val userDetailsService: ReactiveUserDetailsService,
//    private val jwtAuthFilter: JwtAuthFilter
) {
//    @Bean
//    fun filterChain(http: HttpSecurity): SecurityFilterChain {
//        http.authorizeHttpRequests { authz ->
//            authz
//                .requestMatchers(HttpMethod.POST, "/account").permitAll()
//                .anyRequest().authenticated()
//        }
//            .csrf { csrf ->
////            csrf.ignoringRequestMatchers(AntPathRequestMatcher("/account", "POST"))
//            csrf.disable()
//        }
////            .httpBasic {  }
////            .formLogin {  }
//        return http.build()
//    }
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    fun beforeAccountCreatedHttpSecurity(http: ServerHttpSecurity): SecurityWebFilterChain {
//        http
//            .securityMatcher(PathPatternParserServerWebExchangeMatcher("/account", HttpMethod.POST))
//            .authorizeExchange { exchanges ->
//                exchanges
//                    .anyExchange().permitAll()
//            }
//            .csrf { csrf ->
//                csrf.disable()
//            }
//            .httpBasic { }

//        http
//            .securityMatcher(PathPatternParserServerWebExchangeMatcher("/account", HttpMethod.POST))
//            .authorizeExchange { exchanges: AuthorizeExchangeSpec ->
//                exchanges
//                    .anyExchange().permitAll()
//            }
////            .securityMatcher(PathPatternParserServerWebExchangeMatcher("/login", HttpMethod.POST))
////            .authorizeExchange { exchanges ->
////                exchanges
////                    .anyExchange().permitAll()
////            }
//            .csrf { csrf ->
//                csrf.disable()
//            }

        http
            .httpBasic {  }
            .authorizeExchange { exchanges -> exchanges
//            .pathMatchers(HttpMethod.POST, "/account").permitAll()
//            .pathMatchers(HttpMethod.POST, "/login").permitAll()
            .pathMatchers(HttpMethod.POST, "/account", "/login").permitAll()
            .anyExchange()
                .permitAll()
//            .authenticated()
            }
//            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//            .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .csrf { csrf ->
                csrf.disable()
            }

////        http
////            .securityMatcher(PathPatternParserServerWebExchangeMatcher("/login", HttpMethod.POST))
////            .authorizeExchange { exchanges ->
////                exchanges.anyExchange().permitAll()
////            }
////            .csrf { csrf ->
////                csrf.disable()
////            }

//    println("/account set")
        return http.build()
    }

//    @Bean
//    fun webHttpSecurity(http: ServerHttpSecurity): SecurityWebFilterChain {
//        http.authorizeExchange { auth ->
//            auth
//                .anyExchange()
//                .permitAll()
////                .authenticated()
//        }

//            .authenticationManager(authenticationManager())
//        http
//            .authorizeExchange { exchanges: AuthorizeExchangeSpec ->
//                exchanges
//                    .matchers(PathPatternParserServerWebExchangeMatcher("/account", HttpMethod.POST)).permitAll()
//                    .anyExchange().authenticated()
//            }

//            .formLogin { logIn ->
//                logIn.loginPage("/login")
//            }

//            .httpBasic {  }
//            .csrf { csrf ->
////                csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
//                csrf.disable()
//            }

//        return http
//            .authorizeExchange()
//            .pathMatchers(HttpMethod.POST, "/account").permitAll()
//            .anyExchange().authenticated()
//            .and()
//            .csrf().disable()
//            .build();
//        println("all set")

//        return http.build()
//    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return ShaSaltedPasswordEncoder()
    }

//    @Bean
//    fun userDetailsService(): ReactiveUserDetailsService {
//        return userDetailsService
//    }

    @Bean
    fun authenticationManager(): ReactiveAuthenticationManager {
        val authenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
        authenticationManager.setPasswordEncoder(passwordEncoder())
        return authenticationManager
    }
}