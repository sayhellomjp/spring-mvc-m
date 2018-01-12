package com.mjp.mvc.service.impl;

import com.mjp.mvc.annotation.Service;
import com.mjp.mvc.service.QueryService;

import java.util.ArrayList;
import java.util.List;

@Service("QueryServiceImpl")
public class QueryServiceImpl implements QueryService {
    @Override
    public List<String> list() {
        List<String> strs = new ArrayList<String>();
        strs.add("mjp");
        strs.add("rtt");
        return strs;
    }

    @Override
    public String get(int id) {
        String str = String.valueOf(id);
        return str;
    }
}
