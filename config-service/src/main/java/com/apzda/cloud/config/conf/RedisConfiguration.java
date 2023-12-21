/*
 * Copyright (C) 2023-2023 Fengz Ning (windywany@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apzda.cloud.config.conf;

import com.apzda.cloud.config.event.SettingChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import static com.apzda.cloud.config.listener.SettingChangedListener.BEAN_NAME;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
@Slf4j
class RedisConfiguration {

    @Bean(BEAN_NAME)
    ApplicationListener<SettingChangedEvent> configSettingChangedListener(StringRedisTemplate redisTemplate,
            @Qualifier("configChangedMessageTopic") ChannelTopic topic) {
        log.debug("Redis Setting Changed Listener configured!");
        return event -> redisTemplate.convertAndSend(topic.getTopic(), event.getSource().toString());
    }

}
