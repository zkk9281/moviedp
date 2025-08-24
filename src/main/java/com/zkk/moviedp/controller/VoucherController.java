package com.zkk.moviedp.controller;


import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.Voucher;
import com.zkk.moviedp.service.IVoucherService;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    private IVoucherService voucherService;

    /**
     * 新增普通券
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 新增秒杀券
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 查询的优惠券列表
     * @param movieId id
     * @return 优惠券列表
     */
    @GetMapping("/list/{movieId}")
    public Result queryVoucherOfMovie(@PathVariable("movieId") Long movieId) {
       return voucherService.queryVoucherOfMovie(movieId);
    }
}
