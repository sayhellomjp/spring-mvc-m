package com.mjp.mvc.service.impl;

import com.mjp.mvc.annotation.Service;
import com.mjp.mvc.service.ModifyService;

@Service
public class ModifyServiceImpl implements ModifyService {
    @Override
    public String modify(int id, String name) {
        System.out.println(String.format("modify successd, id:%n, name:%s", id, name));
        return "modify success";
    }
}
