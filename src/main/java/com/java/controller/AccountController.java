package com.java.controller;

import java.security.Principal;
import java.util.Collection;

import com.java.entity.BillViewDTO;
import com.java.repository.BillViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.java.entity.Customer;
import com.java.repository.CustomersRepository;
import com.java.repository.OrderRepository;

@Controller
public class AccountController extends CommonController {

	@Autowired
	CustomersRepository customersRepository;

	@Autowired
	OrderRepository orderRepository;
	@Autowired
	BillViewRepository billViewRepository;


	@GetMapping(value = "/account")
	public String account(Model model, Principal principal) {
		model.addAttribute("customer", new Customer());
		Customer customer = customersRepository.FindByEmail(principal.getName()).get();
		model.addAttribute("customer", customer);
		System.out.println(customer.getCustomerId()+" "+principal.getName());

		Collection<BillViewDTO> billViews = billViewRepository.getBillViewByCustomerId(customer.getCustomerId());
		for (BillViewDTO billView : billViews) {
			System.out.println(billView.toString());
		}
		model.addAttribute("billViews", billViews);

		return "site/account";
	}


}
