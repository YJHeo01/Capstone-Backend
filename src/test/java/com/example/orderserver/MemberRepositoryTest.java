package com.example.orderserver;

import com.example.orderserver.domain.Member;
import com.example.orderserver.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void memberShouldBePersistedAndSearchableByEmail() {
        Member member = Member.createUser(
                "alice@example.com",
                "hashed-password",
                "Alice",
                "010-1111-2222"
        );

        memberRepository.save(member);

        assertTrue(memberRepository.existsByEmail("alice@example.com"));
        assertEquals(
                "Alice",
                memberRepository.findByEmail("alice@example.com")
                        .orElseThrow()
                        .getName()
        );
    }
}
