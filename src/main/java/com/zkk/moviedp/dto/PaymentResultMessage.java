package com.zkk.moviedp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

// 支付结果消息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultMessage implements Serializable {
    private String orderId;       // 订单ID
    private boolean success;      // 支付是否成功
    private String transactionId; // 交易ID
    private LocalDateTime payTime; // 支付时间
}
