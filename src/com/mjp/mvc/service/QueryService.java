package com.mjp.mvc.service;

import java.util.List;
import java.util.Map;

public interface QueryService {

    List<String> list();

    Map get(int id);

}
