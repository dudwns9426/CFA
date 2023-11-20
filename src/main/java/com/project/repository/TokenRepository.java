package com.project.repository;

import java.time.Instant;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.domain.entity.Token;

@Repository
@Transactional
public interface TokenRepository extends JpaRepository<Token, Long>{
	
	Token save(Token token);

	@Query("SELECT t.refreshToken " +
		   "FROM Token t " +
		   "LEFT JOIN t.user u " +
		   "WHERE u.id = :userId")
	String findRefreshTokensByUserId(Long userId);
	
	@Modifying
	@Query("UPDATE Token t " +
	       "SET t.refreshToken = :refreshToken, " +
	       "t.expirationTime = :expirationTime, " +
	       "t.modifiedDate = :modifiedDate, " +
	       "t.sessionId = :sessionId " +  // 세션 아이디 추가
	       "WHERE t.user.user_id = :userId")
	void updateRefreshToken(
	        @Param("userId") Long userId,
	        @Param("refreshToken") String refreshToken,
	        @Param("expirationTime") Instant expirationTime,
	        @Param("modifiedDate") Instant modifiedDate,
	        @Param("sessionId") String sessionId  // 세션 아이디 매개변수 추가
	);
	
	@Modifying
	@Query("UPDATE Token t " +
	       "SET t.modifiedDate = :modifiedDate, " +
	       "t.sessionId = :sessionId " +  // 세션 아이디 추가
	       "WHERE t.user.user_id = :userId")
	void updateSessionId(
	        @Param("userId") Long userId,
	        @Param("modifiedDate") Instant modifiedDate,
	        @Param("sessionId") String sessionId  // 세션 아이디 매개변수 추가
	);
	
	@Modifying
	@Query("UPDATE Token t " +
	       "SET t.sessionId = NULL, " +
	       "t.modifiedDate = :modifiedDate " +
	       "WHERE t.user.user_id = :userId")
	void deleteSessionId(
	        @Param("userId") Long userId,
	        @Param("modifiedDate") Instant modifiedDate
	);

	@Query("SELECT t " +
		   "FROM Token t " +
		   "WHERE t.sessionId = :sessionId")
    Token findBySessionId(@Param("sessionId") String sessionId);
}
