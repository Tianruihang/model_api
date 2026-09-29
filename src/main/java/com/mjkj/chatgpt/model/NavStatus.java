package com.mjkj.chatgpt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavStatus {
    private int res;
    private int reason;
    private String goal;
    private double dist;
    private double mileage;
}