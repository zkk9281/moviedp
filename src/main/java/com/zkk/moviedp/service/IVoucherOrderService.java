package com.zkk.moviedp.service;

import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result secKillVoucher(Long voucherId);

//    void createVoucherOrder(VoucherOrder voucherId);
}
