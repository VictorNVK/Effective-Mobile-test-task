package com.example.bankcards.security;

import com.example.bankcards.entity.AdminEntity;
import com.example.bankcards.entity.ClientEntity;
import com.example.bankcards.repository.AdminEntityRepository;
import com.example.bankcards.repository.ClientEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminEntityRepository adminRepository;
    private final ClientEntityRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .map(this::mapAdmin)
                .orElseGet(() -> clientRepository.findByLogin(username)
                        .map(this::mapClient)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));
    }

    private UserDetails mapAdmin(AdminEntity admin) {
        return User.withUsername(admin.getUsername())
                .password(admin.getPassword())
                .roles("ADMIN")
                .build();
    }

    private UserDetails mapClient(ClientEntity client) {
        return User.withUsername(client.getLogin())
                .password(client.getPassword())
                .roles("USER")
                .build();
    }
}
