package com.zkk.moviedp.service.impl;

import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.VoucherOrder;
import com.zkk.moviedp.service.IPaymentService;
import com.zkk.moviedp.service.IVoucherOrderService;
import com.zkk.moviedp.constants.OrderStatus;
import com.zkk.moviedp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentServiceImpl implements IPaymentService {

    @Autowired
    IVoucherOrderService voucherOrderService;

    @Override
    @Transactional
    public Result pay(long orderId) {
        Long userId = UserHolder.getUser().getId();
        //5.1查询订单
        VoucherOrder voucherOrder = voucherOrderService.query().eq("id", orderId).eq("user_id", userId).one();
        //5.2判断是否存在
        if(voucherOrder == null){
            log.error("订单不存在");
            return Result.fail("订单不存在");
        }
        if (voucherOrder.getStatus() != OrderStatus.UNPAID) {
            return Result.fail("订单状态不是未支付");
        }
        // 2. 更新订单状态为支付中
        voucherOrder.setStatus(OrderStatus.PAYING);
        voucherOrderService.updateById(voucherOrder);

        // TODO 调用第三方接口进行支付

        // 3. 更新订单状态为已支付
        voucherOrder.setStatus(OrderStatus.PAID);
        voucherOrderService.updateById(voucherOrder);
        log.info("支付成功");
        return Result.ok(userId);
    }
}
