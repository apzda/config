package com.apzda.cloud.config.service;

import com.apzda.cloud.config.TestApp;
import com.apzda.cloud.config.TestSetting;
import com.apzda.cloud.config.autoconfig.ConfigAutoConfiguration;
import com.apzda.cloud.config.exception.SettingUnavailableException;
import com.apzda.cloud.config.proto.ConfigService;
import com.apzda.cloud.config.proto.KeyReq;
import com.apzda.cloud.config.proto.RevisionReq;
import com.apzda.cloud.config.proto.SaveReq;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = TestApp.class)
@ImportAutoConfiguration({ JacksonAutoConfiguration.class, RedisAutoConfiguration.class,
        ConfigAutoConfiguration.class })
@ActiveProfiles({ "test" })
@Testcontainers
class ConfigServiceImplTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfigService configService;

    @Autowired
    private SettingService settingService;

    @Test
    void load() {
        // given
        val builder = KeyReq.newBuilder();
        builder.setKey(TestSetting.class.getCanonicalName());
        val req = builder.build();

        // when
        val res = configService.load(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getErrCode()).isEqualTo(404);
    }

    @Test
    void save() throws JsonProcessingException, SettingUnavailableException, InterruptedException {
        // given
        val settingKey = TestSetting.class.getCanonicalName() + "@0";
        val ts = new TestSetting();
        ts.setAge(18);
        ts.setName("gsvc");
        ts.setAddress(List.of("a1", "d1", "c2"));

        val builder = SaveReq.newBuilder();
        builder.setKey(settingKey);
        builder.setSetting(ByteString.copyFrom(objectMapper.writeValueAsBytes(ts)));
        val saveReq = builder.buildPartial();

        // when
        val res = configService.save(saveReq);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getErrCode()).isEqualTo(0);

        // given
        val builder1 = KeyReq.newBuilder();
        builder1.setKey(settingKey);
        val req = builder1.build();

        // when
        val res1 = configService.load(req);

        // then
        assertThat(res1).isNotNull();
        assertThat(res1.getErrCode()).isEqualTo(0);
        assertThat(res1.getSetting()).isNotNull();

        // when
        var setting = settingService.load(TestSetting.class);
        assertThat(setting).isNotNull();
        assertThat(setting.getAge()).isEqualTo(18);
        assertThat(setting.getName()).isEqualTo("gsvc");
        assertThat(setting.getAddress()).contains("a1", "d1", "c2");

        // given
        ts.setAge(20);
        ts.setAddress(List.of("a1", "d1", "c2", "e3"));
        val builder2 = SaveReq.newBuilder();
        builder2.setKey(settingKey);
        builder2.setSetting(ByteString.copyFrom(objectMapper.writeValueAsBytes(ts)));
        val saveReq2 = builder2.buildPartial();

        // when
        val res2 = configService.save(saveReq2);

        // then
        assertThat(res2).isNotNull();
        assertThat(res2.getErrCode()).isEqualTo(0);

        TimeUnit.SECONDS.sleep(2);
        // when
        setting = settingService.load(TestSetting.class);
        // then
        assertThat(setting).isNotNull();
        assertThat(setting.getAge()).isEqualTo(20);
        assertThat(setting.getName()).isEqualTo("gsvc");
        assertThat(setting.getAddress()).contains("a1", "d1", "c2", "e3");

        // given
        val b3 = RevisionReq.newBuilder();
        b3.setKey(settingKey);
        // when
        val revisions = configService.revisions(b3.build());
        // then
        assertThat(revisions.getPageInfo().getFirst()).isTrue();
        assertThat(revisions.getPageInfo().getNumberOfElements()).isEqualTo(1);
        assertThat(revisions.getRevisionCount()).isEqualTo(1);
        assertThat(revisions.getRevision(0).getRevision()).isEqualTo(1);
    }

}
