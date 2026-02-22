package com.gogo.infrastructure.config;

import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PlaceRepository placeRepository;

    public DataInitializer(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @Override
    public void run(String... args) {
        if (!placeRepository.findAll().isEmpty()) {
            log.info("Seed data already exists, skipping.");
            return;
        }

        List<Place> seeds = List.of(
            Place.create(
                "광장시장 육회비빔밥",
                "서울 종로구 창경궁로 88",
                "RESTAURANT",
                null,
                "신선한 육회와 비빔밥의 조화, 광장시장 대표 맛집",
                "https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600&q=80",
                "system"
            ),
            Place.create(
                "을지로 노가리골목",
                "서울 중구 을지로 119",
                "BAR",
                null,
                "을지로 감성의 노가리 안주와 생맥주, 레트로 분위기",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&q=80",
                "system"
            ),
            Place.create(
                "성수동 커피 커퍼",
                "서울 성동구 성수이로 78",
                "CAFE",
                null,
                "성수동 스페셜티 커피 명소, 직접 로스팅한 원두 사용",
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600&q=80",
                "system"
            ),
            Place.create(
                "이태원 한남 버거",
                "서울 용산구 이태원로 180",
                "RESTAURANT",
                null,
                "두툼한 패티의 수제버거, 이태원 현지인 맛집",
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&q=80",
                "system"
            ),
            Place.create(
                "삼청동 수제비",
                "서울 종로구 삼청로 101",
                "RESTAURANT",
                null,
                "경복궁 인근 전통 손수제비, 구수한 멸치 육수",
                "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=600&q=80",
                "system"
            ),
            Place.create(
                "연남동 커피리브레",
                "서울 마포구 연남로 1길 59",
                "CAFE",
                null,
                "핸드드립 스페셜티 커피의 성지, 연남동 감성 카페",
                "https://images.unsplash.com/photo-1511920170033-f8396924c348?w=600&q=80",
                "system"
            ),
            Place.create(
                "홍대 오뎅식당",
                "서울 마포구 어울마당로 65",
                "BAR",
                null,
                "홍대 이자카야 분위기, 오뎅탕과 사케 페어링",
                "https://images.unsplash.com/photo-1544148103-0773bf10d330?w=600&q=80",
                "system"
            ),
            Place.create(
                "망원동 베이커리",
                "서울 마포구 망원로 64",
                "CAFE",
                null,
                "망원동 골목 베이커리 카페, 직접 구운 크루아상과 커피",
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&q=80",
                "system"
            )
        );

        seeds.forEach(placeRepository::save);
        log.info("Seeded {} places.", seeds.size());
    }
}
