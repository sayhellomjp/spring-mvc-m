package com.mjp.mvc.service.impl;

import com.mjp.mvc.annotation.Service;
import com.mjp.mvc.service.QueryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map get(int id) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("id", id);
        m.put("name", "mjp");
        return m;
    }
}
