package com.java.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.java.entity.Product;
import com.java.repository.ProductRepository;

@Controller
public class IndexController extends CommonController {

	@Autowired
	ProductRepository productRepository;

	@GetMapping(value = "/")
	public String index(Model model) {
		listproduct10(model);
		topProduct10(model);
		
		return "site/index";
	}

	// list product ở trang chủ limit 10 sản phẩm mới nhất
	@ModelAttribute("listProduct10")
	public List<Product> listproduct10(Model model) {
		List<Product> productList = productRepository.listProduct10();
		model.addAttribute("productList", productList);
		return productList;
	}
	
	// Hiển thị Top 10 sản phẩm bán chạy nhất.
	public void topProduct10(Model model) {
		List<Object[]> productList = productRepository.topSellingProduct10();
		if (productList != null) {
			ArrayList<Integer> listIdProductArrayList = new ArrayList<>();
			for (int i = 0; i < productList.size(); i++) {
				String id = String.valueOf(productList.get(i)[0]);
				listIdProductArrayList.add(Integer.valueOf(id));
			}
			List<Product> listProducts = productRepository.findByInventoryIds(listIdProductArrayList);
			model.addAttribute("listTop10Product", listProducts);
		}
	}

}
