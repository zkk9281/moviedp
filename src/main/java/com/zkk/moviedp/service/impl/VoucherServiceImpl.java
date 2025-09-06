package com.zkk.moviedp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.Voucher;
import com.zkk.moviedp.mapper.VoucherMapper;
import com.zkk.moviedp.entity.SeckillVoucher;
import com.zkk.moviedp.service.ISeckillVoucherService;
import com.zkk.moviedp.service.IVoucherService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import static com.zkk.moviedp.constants.RedisConstants.SECKILL_STOCK_KEY;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfMovie(Long movieId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfMovie(movieId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
//        SeckillVoucher seckillVoucher = new SeckillVoucher();
//        seckillVoucher.setVoucherId(voucher.getId());
//        seckillVoucher.setStock(voucher.getStock());
//        seckillVoucher.setBeginTime(voucher.getBeginTime());
//        seckillVoucher.setEndTime(voucher.getEndTime());
//        seckillVoucherService.save(seckillVoucher);

        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
