package com.gogo.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GroupTest {

    @Test
    void 그룹_생성_시_초대코드_자동생성() {
        Group group = Group.create("성수동 탐방대", "홍길동");

        assertThat(group.getInviteCode()).isNotNull();
        assertThat(group.getInviteCode()).hasSize(8);
        assertThat(group.getCreatedAt()).isNotNull();
    }

    @Test
    void 서로_다른_그룹은_다른_초대코드() {
        Group group1 = Group.create("그룹1", "홍길동");
        Group group2 = Group.create("그룹2", "김철수");

        assertThat(group1.getInviteCode()).isNotEqualTo(group2.getInviteCode());
    }

    @Test
    void 멤버_추가_성공() {
        Group group = Group.create("성수동 탐방대", "홍길동");

        group.addMember("김철수");

        assertThat(group.getMembers()).hasSize(1);
        assertThat(group.getMembers().get(0).getNickname()).isEqualTo("김철수");
    }

    @Test
    void 그룹_이름이_빈문자열이면_예외() {
        assertThatThrownBy(() -> Group.create("", "홍길동"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("그룹 이름");
    }
}
