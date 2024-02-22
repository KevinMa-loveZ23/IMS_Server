package xyz.keinthema.serverims.service.impl

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import xyz.keinthema.serverims.repository.AccountRepository

@Service
class AccountDetailServiceImpl(private val accountRepository: AccountRepository,
                               private val reactiveMongoTemplate: ReactiveMongoTemplate
): ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        return accountRepository.findById(username.toLong())
            .map { account ->
                User.builder()
                    .username(account.id.toString())
                    .password(account.password)
                    .build()
            }
            .switchIfEmpty(Mono.error(UsernameNotFoundException("User ID not found: $username")))
    }
}