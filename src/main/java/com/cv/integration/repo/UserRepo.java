package com.cv.integration.repo;

import com.cv.integration.model.SystemProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class UserRepo {
    private final HashMap<String, String> hmKey = new HashMap<>();
    @Autowired
    private WebClient userApi;


    public String getProperty(String key, String compCode) {
        if (hmKey.isEmpty()) {
            Mono<List<SystemProperty>> result = userApi.get().uri(builder -> builder.path("/user/getSystemProperty")
                            .queryParam("compCode", compCode)
                            .build())
                    .retrieve().bodyToFlux(SystemProperty.class)
                    .collectList();
            List<SystemProperty> list = result.block();
            assert list != null;
            list.forEach(s -> hmKey.put(s.getKey().getPropKey(), s.getPropValue()));
        }
        return hmKey.get(key);
    }

}
