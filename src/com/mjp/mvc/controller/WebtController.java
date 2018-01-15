package com.mjp.mvc.controller;

import com.mjp.mvc.annotation.Autowired;
import com.mjp.mvc.annotation.Controller;
import com.mjp.mvc.annotation.RequestMapping;
import com.mjp.mvc.annotation.RequestParam;
import com.mjp.mvc.service.ModifyService;
import com.mjp.mvc.service.QueryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/web")
public class WebtController {

    @Autowired("QueryServiceImpl")
    private QueryService queryService;

    @Autowired
    private ModifyService modifyService;

    @RequestMapping("/list")
    public void list() {
        List<String> results = queryService.list();
    }

    @RequestMapping("/get")
    public Map get(@RequestParam("id") int id, HttpServletRequest request, HttpServletResponse response) {
        Map map = queryService.get(id);
        return map;
    }

    @RequestMapping("/update")
    public void update(@RequestParam("id") int id, @RequestParam("name") String name, HttpServletRequest request, HttpServletResponse response) {
        String str = modifyService.modify(id, name);
    }

    @RequestMapping("/remove")
    public void remove(int removeId, HttpServletRequest request, HttpServletResponse response) {
        String str = "remove";
    }

}
