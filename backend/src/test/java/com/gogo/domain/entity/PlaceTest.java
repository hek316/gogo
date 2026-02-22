package com.gogo.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PlaceTest {

    @Test
    void 장소_생성_성공() {
        Place place = Place.create("성수동 카페", "서울 성동구", "CAFE", "https://naver.me/xxx", "분위기 좋음", null, "홍길동");

        assertThat(place.getName()).isEqualTo("성수동 카페");
        assertThat(place.getStatus()).isEqualTo(PlaceStatus.WANT_TO_GO);
        assertThat(place.getCreatedAt()).isNotNull();
    }

    @Test
    void 이름이_빈_문자열이면_예외() {
        assertThatThrownBy(() -> Place.create("", "서울 성동구", "CAFE", null, null, null, "홍길동"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("장소 이름");
    }

    @Test
    void 이름이_null이면_예외() {
        assertThatThrownBy(() -> Place.create(null, "서울 성동구", "CAFE", null, null, null, "홍길동"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 방문완료_상태_변경() {
        Place place = Place.create("성수동 카페", "서울 성동구", "CAFE", null, null, null, "홍길동");

        place.markAsVisited();

        assertThat(place.getStatus()).isEqualTo(PlaceStatus.VISITED);
    }
}
