package com.zerobase.homemate.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawRequestDto {

    private String reason;
    private String detail;
}
