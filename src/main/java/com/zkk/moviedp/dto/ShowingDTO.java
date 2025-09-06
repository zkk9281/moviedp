package com.zkk.moviedp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowingDTO {
    private Long id;
    private String movie;
    private String theater;
    private String address;
    private LocalDateTime startTime;
    private Double price;
    private Long voucherId;
    private Integer stock;
}
