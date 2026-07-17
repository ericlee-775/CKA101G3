package com.farmily.product.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.farmily.product.service.ProductClickService;
import com.farmily.product.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductViewController {

	@Autowired
	private ProductClickService productClickService;
	@Autowired
	private ProductService productService;

	// 手動結算熱門商品（管理員後台觸發）
	@PostMapping("/hot/settle")
	public String settleHotProducts(RedirectAttributes ra) {
		productClickService.settleHotProductIds();
		ra.addFlashAttribute("success", "（已結算）");
		return "redirect:/admin/products";

	}

	@GetMapping
	public String listAll(ModelMap model) {
		model.addAttribute("productTop", productService.getHotProducts());
		return "back-end/admin/product";
	}
}
