package com.crumoria.service;

import org.springframework.stereotype.Service;

import com.crumoria.exception.BusinessException;
import com.crumoria.exception.ErrorCode;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

@Service
public class PasswordPolicyService {

    private final Zxcvbn zxcvbn = new Zxcvbn();

    public void assertStrongPassword(String rawPassword) {
        Strength strength = zxcvbn.measure(rawPassword);

        if (strength.getScore() < 3) {
            throw new BusinessException(ErrorCode.WEAK_PASSWORD, strength.getFeedback().getWarning());
        }
    }
}
