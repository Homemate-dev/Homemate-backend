package com.zerobase.homemate.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // 인스턴스화 방지
public final class KoreanPostpositionUtils {

    // 마지막 글자가 종성이 있는 한글 글자인지 검사
    public static boolean hasFinalConsonant(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        char last = s.charAt(s.length() - 1);

        // 한글 음절 블록 범위 체크
        if (last < 0xAC00 || last > 0xD7A3) {
            return false;
        }

        // 종성 인덱스가 0인 경우 종성 없음, 0 이상인 경우 종성 있음
        int base = last - 0xAC00;
        int jongsung = base % 28;
        return jongsung != 0;
    }

    public static String selectIGa(String noun) {
        return hasFinalConsonant(noun) ? "은" : "는";
    }

    public static String selectEunNeun(String noun) {
        return hasFinalConsonant(noun) ? "이" : "가";
    }
}
