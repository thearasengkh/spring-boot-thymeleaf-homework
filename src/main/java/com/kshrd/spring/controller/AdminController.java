package com.kshrd.spring.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kshrd.spring.model.User;
import com.kshrd.spring.service.UserService;

@Controller
public class AdminController {
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/admin/dashboard")
	public String dashboard(){
		return "admin/dashboard";
	}
	
	@RequestMapping("/admin/user-list")
	public String userList(Model model){
		model.addAttribute("USERS", userService.getAllUsers());
		return "/admin/user-list";
	}
	
}
