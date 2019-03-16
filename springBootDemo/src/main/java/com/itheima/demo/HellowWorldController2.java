package com.itheima.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HellowWorldController2 {
	
	@Autowired
	private Environment env;

	@RequestMapping("/info2")
	public String info(){
		return "HelloWorld222!!"+env.getProperty("url");		
	}
}
