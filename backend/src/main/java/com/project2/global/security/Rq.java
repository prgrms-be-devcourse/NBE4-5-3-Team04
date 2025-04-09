package com.project2.global.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.project2.domain.member.entity.Member;
import com.project2.domain.member.service.MemberService;
import com.project2.global.exception.ServiceException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// Request, Response, Session, Cookie, Header
@Component
@RequiredArgsConstructor
public class Rq {

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final MemberService memberService;

	@Value("${custom.url.domain}")
	private String siteDomain;

	public void setLogin(Member actor) {

		// 유저 정보 생성
		UserDetails user = new SecurityUser(actor.getId(), actor.getEmail(),
			List.of(new SimpleGrantedAuthority("ROLE_USER")));

		// 인증 정보 저장소
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
		);
	}

	public Member getActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new ServiceException("401-2", "로그인이 필요합니다.");
		}

		Object principal = authentication.getPrincipal();

		if (!(principal instanceof SecurityUser)) {
			throw new ServiceException("401-3", "잘못된 인증 정보입니다");
		}

		SecurityUser user = (SecurityUser)principal;

		return Member.builder()
			.id(user.getId())
			.email(user.getUsername())
			.build();
	}

	public String getHeader(String name) {
		return request.getHeader(name);
	}

	public String getValueFromCookie(String name) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}

		return null;
	}

	public void setHeader(String name, String value) {
		response.setHeader(name, value);
	}

	public void addCookie(String name, String value, boolean isHttpOnly) {
		Cookie accsessTokenCookie = new Cookie(name, value);

		accsessTokenCookie.setDomain(siteDomain);
		accsessTokenCookie.setPath("/");
		accsessTokenCookie.setHttpOnly(isHttpOnly);
		accsessTokenCookie.setSecure(true);
		accsessTokenCookie.setAttribute("SameSite", "Strict");

		response.addCookie(accsessTokenCookie);
	}

	public Member getRealActor(Member actor) {
		return memberService.findById(actor.getId()).get();
	}

	public void removeCookie(String name) {
		// 원칙적으로 쿠키를 서버에서 삭제하는 것은 불가능.

		Cookie cookie = new Cookie(name, null);
		cookie.setDomain(siteDomain);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "Strict");
		cookie.setMaxAge(0);

		response.addCookie(cookie);
	}
}