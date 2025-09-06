package com.zkk.moviedp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zkk.moviedp.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfMovie(@Param("movieId") Long movieId);
}
