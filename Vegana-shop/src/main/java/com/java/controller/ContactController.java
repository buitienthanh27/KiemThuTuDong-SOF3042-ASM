package com.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController extends CommonController {

	@GetMapping(value = "/contact")
	public String contact(Model model) {

		return "site/contact";
	}
}
