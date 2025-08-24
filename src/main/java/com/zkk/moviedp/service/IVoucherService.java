package com.zkk.moviedp.service;

import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfMovie(Long movieId);

    void addSeckillVoucher(Voucher voucher);
}
