package com.zkk.moviedp.service;

import com.zkk.moviedp.dto.Result;

public interface IPaymentService {
    Result pay(long voucherId);
}
