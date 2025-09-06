package com.zkk.moviedp.controller;


import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.service.IPaymentService;
import com.zkk.moviedp.service.IVoucherOrderService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Autowired
    private IVoucherOrderService iVoucherOrderService;
    @Autowired
    private IPaymentService iPaymentService;

    @Tool(name = "Place_an_order_based_on_movie_voucher_ID", value = "After the user confirms the order, use this tool to create the order. " +
            "The result returns the order number if the order is successful, or the reason for failure.")
    @PostMapping("/seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return iVoucherOrderService.secKillVoucher(voucherId);
    }

    @PostMapping("/pay")
    public Result pay(@RequestParam Long orderId) {
        return iPaymentService.pay(orderId);
    }
}
