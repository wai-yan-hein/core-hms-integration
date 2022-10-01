package com.cv.integration.mongo;

import com.cv.integration.mongo.model.PatientInfo;
import com.cv.integration.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class MigrateMongo {
    @Autowired
    private ReportService service;
    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(c -> c
                            .defaultCodecs()
                            .maxInMemorySize(16 * 1024 * 1024))
                    .build())
            .baseUrl("http://localhost:101")
            .build();

    //@Scheduled(fixedRate = 10 * 60000 * 1000)
    public void migrate() throws SQLException {
        migratePatient();
    }

    private void migratePatient() throws SQLException {
        List<PatientInfo> infos = service.getPatient();
        infos.forEach(i -> {
            Mono<PatientInfo> result = webClient.post()
                    .uri("/api/patient/save-patient")
                    .body(Mono.just(i), PatientInfo.class)
                    .retrieve()
                    .bodyToMono(PatientInfo.class);
            PatientInfo info =result.block();
            log.info(info.getPatientNo());
        });
    }
}
