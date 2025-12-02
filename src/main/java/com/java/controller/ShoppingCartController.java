package com.java.controller;

import com.java.entity.*;
import com.java.repository.*;
import com.java.service.SendMailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Controller
public class ShoppingCartController extends CommonController {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;

	@Autowired
	CustomersRepository customersRepository;

	@Autowired
	SendMailService sendMailService;

	@Autowired
	HttpSession session;

	@Autowired
	CartRepository cartRepository;




	@Autowired
	CartProductViewRepository cartProductViewRepository;

	String CUSTOMER_ID;

	public ShoppingCartController(ProductRepository productRepository, OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, CustomersRepository customersRepository, SendMailService sendMailService) {
		this.productRepository = productRepository;
		this.orderRepository = orderRepository;
		this.orderDetailRepository = orderDetailRepository;
		this.customersRepository = customersRepository;
		this.sendMailService = sendMailService;
	}

	@GetMapping(value = "/carts")
	public String shoppingCart(Model model, HttpServletRequest request, Principal principal) {

		// Kiểm tra xem người dùng đã đăng nhập hay chưa
		if (principal == null) {
			// Chưa đăng nhập, chuyển hướng đến trang đăng nhập
			return "redirect:/login";
		}

		Customer c = customersRepository.FindByEmail(principal.getName()).get();
		String customerId = c.getCustomerId();

		// Tải giỏ hàng từ cơ sở dữ liệu
		Collection<CartProductViewDTO> cartProductViewDTO = cartProductViewRepository.getCartProductViewByCustomerId(customerId);

		// Thêm giỏ hàng vào mô hình
		model.addAttribute("cartProductViewDTO", cartProductViewDTO);

		model.addAttribute("totalPrice", tongdonhang(cartProductViewDTO));

		// Các thông tin khác (nếu cần)/*/
		// model.addAttribute("totalCartItemWishs", wishListService.getCount());
		// model.addAttribute("totalCartItems", shoppingCartService.getCount());
		session = request.getSession();
		session.setAttribute("customerId", customerId);
		System.out.println(customerId);
		CUSTOMER_ID=customerId;

		return "site/shoppingCart";
	}
	private double tongdonhang(Collection<CartProductViewDTO> cartProductViewDTO)
	{
		double thanhtien = 0;
		for (CartProductViewDTO cart : cartProductViewDTO) {
			double price = cart.getTotalPrice();
			thanhtien += price;
		}
		return thanhtien;
	}

	@GetMapping(value = "/addToCart")
	public String add(@RequestParam("productId") Integer productId, HttpServletRequest request, Principal principal) {

		// Kiểm tra xem người dùng đã đăng nhập hay chưa
		if (principal == null) {
			// Chưa đăng nhập, chuyển hướng đến trang đăng nhập
			return "redirect:/login";
		}

		// Đã đăng nhập, tiếp tục thêm sản phẩm vào giỏ hàng
		Customer c = customersRepository.FindByEmail(principal.getName()).get();
		String customerId = c.getCustomerId();


		Product product = productRepository.findById(productId).orElse(null);
		session = request.getSession();

		Cart cart = new Cart(customerId, productId, 1, product.getPrice());
		cartRepository.updateOrInsertIntoCart(customerId, productId);

		Cart item = new Cart();
		BeanUtils.copyProperties(product, item);
		item.setQuantity(1);
		item.setProductId(product.getProductId());
		item.setProductId(productId);
		item.setCustomerId(customerId);

		session.setAttribute("cart", cart);
		session.setAttribute("customerId", customerId);
		return "redirect:" + request.getHeader("Referer");
	}




	@PutMapping(value = "/updateCart")
	public ResponseEntity<String> updateCart(@RequestBody Map<String, String> payload,HttpServletRequest request) {
		String name = payload.get("name");
		int productId = Integer.parseInt(payload.get("productId"));
		String customerId = (String) session.getAttribute("customerId");
		int quantity = Integer.parseInt(payload.get("quantity"));
		double price = Double.parseDouble(payload.get("price"));
		double totalPrice = Double.parseDouble(payload.get("totalPrice"));
		String image = payload.get("image");
		double discount = Double.parseDouble(payload.get("discount"));

		// You should validate the input here
		CartProductViewDTO cartProductViewDTO = new CartProductViewDTO();
		cartProductViewDTO.setCustomerId(customerId);
		cartProductViewDTO.setName(name);
		cartProductViewDTO.setProductId(productId);
		cartProductViewDTO.setQuantity(quantity);
		cartProductViewDTO.setPrice(price);
		cartProductViewDTO.setTotalPrice(totalPrice);
		cartProductViewDTO.setImage(image);
		cartProductViewDTO.setDiscount(discount);
		System.out.println(cartProductViewDTO);

		cartRepository.updateCart(cartProductViewDTO);
		session = request.getSession();
		session.setAttribute("customerId", customerId);
		return ResponseEntity.ok("Cart updated");
	}

	@DeleteMapping("/deleteCartItem/{customerId}/{productId}")
	public ResponseEntity<?> deleteCartItem(@PathVariable("productId") Integer productId) {
		try {
			cartRepository.deleteByCustomerIdAndProductId(CUSTOMER_ID, productId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting cart item");
		}
	}


	// show check out
	@GetMapping(value = "/checkout")
	public String checkOut(Model model) {

		Order order = new Order();
		model.addAttribute("order", order);
		model.addAttribute("message","");
		// Tải giỏ hàng từ cơ sở dữ liệu
		Collection<CartProductViewDTO> cartProductViewDTO = cartProductViewRepository.getCartProductViewByCustomerId(CUSTOMER_ID);
		if (cartProductViewDTO == null || cartProductViewDTO.isEmpty()) {
			// Giỏ hàng không có hàng, hiển thị thông báo và chuyển hướng về trang chủ
			return "redirect:/"; // Điều hướng về trang chủ
		}
		// Thêm giỏ hàng vào mô hình
		model.addAttribute("cartProductViewDTO", cartProductViewDTO);

		model.addAttribute("total", tongdonhang(cartProductViewDTO));
		model.addAttribute("NoOfItems", cartProductViewDTO.size());

		model.addAttribute("totalPrice", tongdonhang(cartProductViewDTO));
		model.addAttribute("totalCartItems", cartProductViewDTO.size());

		return "site/checkOut";
	}

	// submit checkout
	@PostMapping(value = "/checkout")
	@Transactional
	public String checkedOut(Model model, @ModelAttribute("order") Order order, HttpServletRequest request, Principal principal) {
		// Bước 1: Tạo đơn đặt hàng từ giỏ hàng
		session = request.getSession();
		Collection<CartProductViewDTO> cartProductViewDTO = cartProductViewRepository.getCartProductViewByCustomerId(CUSTOMER_ID);
		Customer c = customersRepository.FindByEmail(principal.getName()).get();
		//orderRepository.createOrderFromCart(c.getCustomerId(),order.getPhone());
		order.setTotalPrice(tongdonhang(cartProductViewDTO));
		order.setOrderDate(null);
		order.setCustomer(c);
		orderRepository.save(order);
		order.getOrderId();
		// Bước 2: Thêm chi tiết đơn hàng từ giỏ hàng
		for (CartProductViewDTO cartItem : cartProductViewDTO) {
			OrderDetail orderDetail = new OrderDetail();
			orderDetail.setQuantity(cartItem.getQuantity());
			orderDetail.setOrder(order);
			orderDetail.setProduct(productRepository.findByIdProduct(cartItem.getProductId()));
			orderDetail.setTotalPrice(cartItem.getTotalPrice());
			orderDetail.setPrice(cartItem.getPrice());
			orderDetail.setStatus("Đang Chờ Xử Lý");
			orderDetailRepository.save(orderDetail);
		}
		// Bước 3: Xóa giỏ hàng sau khi hoàn tất đặt hàng
		cartRepository.emptyCart(c.getCustomerId());
		session.removeAttribute("cartItems");
		model.addAttribute("orderId", order.getOrderId());
		model.addAttribute("totalCartItems", cartProductViewDTO.size());
		return "site/checkout_success";
	}


	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(true);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
	}

}

/*		sendMailService.sendMail(c.getEmail(), "Vegana Store",
				"<h3>Hi: " + order.getReceiver() + " !</h3> Bạn có một đơn đặt hàng từ Vegana Store!\r\n <br>"
						+ "<br>"
						+ "Ngày đặt hàng : " + "<h4 style=\"color: black;\">"+ order.getOrderDate() + "</h4>"
						+ "<br>"
						+ "Tổng số tiền là: "
						+ "<h4 style=\"color: red;\">$" + order.getTotalPrice() + "</h4>"
						+ "<br>"
						+ "Cảm ơn bạn đã mua sắm trong cửa hàng của chúng tôi!\r\n ");*/