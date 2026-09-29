package com.mjkj.chatgpt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryStatus {
    private int battery;
    private int chargeFlag;
    private int emergencyButton;
    
    public boolean isCharging() {
        return chargeFlag == 2 || chargeFlag == 3 || chargeFlag == 8;
    }
    
    public boolean isEmergencyPressed() {
        return emergencyButton == 0;
    }
    
    public boolean isLowBattery() {
        return battery < 20;
    }
}