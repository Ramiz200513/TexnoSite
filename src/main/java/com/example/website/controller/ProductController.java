package com.example.website.controller;

import com.example.website.models.Order;
import com.example.website.models.OrderItem;
import com.example.website.models.Product;
import com.example.website.models.WebUser;
import com.example.website.repository.OrderRepository;
import com.example.website.repository.ProductRepository;
import com.example.website.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CurrencyApiService currencyApiService; // Добавили сервис курсов валют

    @Autowired
    private OrderRepository orderRepository;

    // Обновили конструктор
    public ProductController(ProductRepository productRepository,
                             UserRepository userRepository,
                             CurrencyApiService currencyApiService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.currencyApiService = currencyApiService;
    }

    // Смена валюты
    @GetMapping("/set-currency/{curr}")
    public String setCurrency(@PathVariable String curr, HttpSession session, HttpServletRequest request) {
        session.setAttribute("currency", curr);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/products");
    }

    // === ГЛАВНАЯ СТРАНИЦА С МОЩНОЙ ФИЛЬТРАЦИЕЙ ===
    @GetMapping("/products")
    public String mainPage(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) Double minPrice,
                           @RequestParam(required = false) Double maxPrice,
                           @RequestParam(defaultValue = "0") int page,
                           Model model, HttpSession session) {

        Page<Product> productPage = productRepository.findFiltered(
                keyword, category, minPrice, maxPrice, PageRequest.of(page, 6)
        );

        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentPage", page);

        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("categories", productRepository.findAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCat", category);

        // ПОЛУЧАЕМ АКТУАЛЬНЫЙ КУРС И ПЕРЕДАЕМ В HTML
        Double actualKztRate = currencyApiService.getActualKztRate();
        model.addAttribute("kztRate", actualKztRate);

        List<Product> cart = (List<Product>) session.getAttribute("cart");
        model.addAttribute("cartSize", cart == null ? 0 : cart.size());

        return "products";
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        model.addAttribute("cartItems", cart);
        model.addAttribute("totalPrice", cart.stream().mapToDouble(Product::getPrice).sum());
        return "cart";
    }

    @GetMapping("/products/{id}")
    public String showDetails(@PathVariable Integer id, Model model, HttpSession session) {
        productRepository.findById(id).ifPresent(p -> model.addAttribute("item", p));
        model.addAttribute("user", session.getAttribute("user"));

        // Сюда тоже можно добавить курс валют, если цена выводится на странице товара
        Double actualKztRate = currencyApiService.getActualKztRate();
        model.addAttribute("kztRate", actualKztRate);

        List<Product> cart = (List<Product>) session.getAttribute("cart");
        model.addAttribute("cartSize", cart == null ? 0 : cart.size());
        return "details";
    }

    @GetMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Integer id, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/products?auth_error=true";
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        productRepository.findById(id).ifPresent(cart::add);
        session.setAttribute("cart", cart);
        return "redirect:/products";
    }

    @GetMapping("/cart/remove/{index}")
    public String removeFromCart(@PathVariable int index, HttpSession session) {
        List<Product> cart = (List<Product>) session.getAttribute("cart");
        if (cart != null && index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        WebUser user = (WebUser) session.getAttribute("user");
        List<Product> cart = (List<Product>) session.getAttribute("cart");

        if (user == null) return "redirect:/products?auth_error=true";
        if (cart == null || cart.isEmpty()) return "redirect:/products";

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cart.stream().mapToDouble(Product::getPrice).sum());
        order.setStatus("Оплачен");

        for (Product p : cart) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(p);
            item.setPriceAtPurchase(p.getPrice());
            order.getItems().add(item);
        }

        orderRepository.save(order);

        model.addAttribute("orderNumber", order.getId());
        model.addAttribute("totalPrice", order.getTotalAmount());
        session.removeAttribute("cart");

        return "success";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        WebUser user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
        }
        return "redirect:/products";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, @RequestParam String fullname, HttpSession session) {
        if (userRepository.findByUsername(username) == null) {
            WebUser user = new WebUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setFullName(fullname);
            userRepository.save(user);
            session.setAttribute("user", user);
        }
        return "redirect:/products";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/products";
    }
}