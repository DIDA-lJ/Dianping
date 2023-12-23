package com.linqi;

import com.linqi.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
class HmDianPingApplicationTests {
    @Resource
    private ShopServiceImpl shopService;

    @Test
    void testSaveShop(){
        shopService.saveShop2Redis(1L,10L);
    }

    @Test
    void testStartTime(){
        LocalDateTime time = LocalDateTime.of(2023,12,13,0,0,0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println(second);
    }
}
