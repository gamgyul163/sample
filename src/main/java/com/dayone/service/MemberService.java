package com.dayone.service;

import com.dayone.exception.impl.AlreadyExistUserException;
import com.dayone.model.Auth;
import com.dayone.persist.MemberRepository;
import com.dayone.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.memberRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 ID입니다."));
  }

  public MemberEntity register(Auth.SignUp member) {
    if (memberRepository.existsByUsername(member.getUsername())) {
      throw new AlreadyExistUserException();
    }

    member.setPassword(passwordEncoder.encode(member.getPassword()));

    return memberRepository.save(member.toEntity());
  }

  public MemberEntity authenticate(Auth.SignIn member) {
    String userName = member.getUsername();
    MemberEntity user = memberRepository.findByUsername(userName)
        .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 ID입니다."));

    if (!passwordEncoder.matches(member.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("아이디 혹은 비밀번호가 일치하지 않습니다.");
    }
    return user;
  }
}
