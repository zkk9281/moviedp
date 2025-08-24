package com.zkk.moviedp.controller;


import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.service.IPaymentService;
import com.zkk.moviedp.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Autowired
    private IVoucherOrderService iVoucherOrderService;
    @Autowired
    private IPaymentService iPaymentService;

    @PostMapping("/seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return iVoucherOrderService.secKillVoucher(voucherId);
    }

    @PostMapping("/pay")
    public Result pay(@RequestParam Long orderId) {
        return iPaymentService.pay(orderId);
    }
}
