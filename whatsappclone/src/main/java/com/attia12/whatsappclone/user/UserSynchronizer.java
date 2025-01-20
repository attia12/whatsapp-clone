package com.attia12.whatsappclone.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class UserSynchronizer {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    public void synchronizeWithIdp(Jwt token) {
        log.info("synchronize user with idp: {}", token);
        getUserEmailFromToken(token).ifPresent(email -> {
              log.info("synchronizing useremail: {}", email);
              Optional<User> user = userRepository.findByEmail(email);
              User user1=userMapper.fromTokenAttributes(token.getClaims());
              user.ifPresent(value -> user1.setId(user.get().getId()));
              userRepository.save(user1);
        });
    }
    private Optional<String> getUserEmailFromToken(Jwt token) {
        Map<String, Object> attributes = token.getClaims();
        if(attributes.containsKey("email")){return Optional.of(attributes.get("email").toString());}
        return Optional.empty();
    }
}
