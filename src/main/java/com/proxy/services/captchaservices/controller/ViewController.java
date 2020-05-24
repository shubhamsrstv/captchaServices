package com.proxy.services.captchaservices.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class ViewController {
	
	
	@GetMapping("/v3")
	public String v3() {
		
		return "v3";
	}

}