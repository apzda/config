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
package com.apzda.cloud.demo.controller;

import com.apzda.cloud.config.Revision;
import com.apzda.cloud.config.exception.SettingUnavailableException;
import com.apzda.cloud.config.service.SettingService;
import com.apzda.cloud.demo.setting.DemoSetting;
import com.apzda.cloud.gsvc.domain.Pager;
import com.apzda.cloud.gsvc.dto.Response;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@RestController
@RequestMapping("/demo-config")
@RequiredArgsConstructor
public class DemoController {

    private final SettingService settingService;

    @PostMapping("/save")
    public Response<String> save(@RequestBody DemoSetting setting) {
        try {
            val saved = settingService.save(setting);
        }
        catch (SettingUnavailableException e) {
            return Response.error(500, e.getMessage());
        }
        return Response.success("ok");
    }

    @GetMapping("/load")
    public Response<DemoSetting> load() throws SettingUnavailableException {
        val setting = settingService.load(DemoSetting.class);
        return Response.success(setting);
    }

    @GetMapping("/revisions")
    public Response<List<Revision<DemoSetting>>> revisions(
            @RequestParam(value = "page_number", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(value = "page_size", required = false, defaultValue = "1") int pageSize)
            throws SettingUnavailableException {
        val setting = settingService.revisions(DemoSetting.class,
                Pager.of(pageNumber, pageSize).withSort(Sort.Direction.DESC, "revision"));
        return Response.success(setting);
    }

}
