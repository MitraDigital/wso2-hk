package com.hkjc.wso2.identity.service.rest.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.ssl.Base64;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.CharacterSequence;
import org.passay.IllegalSequenceRule;
import org.passay.PasswordData;
import org.passay.PasswordGenerator;
import org.passay.SequenceData;

/**
 * Collection of utils with common functionallity
 */
public class CommonUtils {
    /**
     * Search of parameter at string by prefix and separator.
     * As example if you need "abc" from "type=4;name=abc;value=8" then
     * call this method with "name=" prefix and ";" separator
     *
     * @param src       is source string
     * @param prefix    is identifier of param with a sign of equal
     * @param separator is string for parameters separation
     * @return parameter value
     */
    public static String getParamFromString(String src, String prefix, String separator) {
        String result = null;
        if (src != null && src.length() > 0) {
            int startIndex;
            int endIndex;
            if (src.startsWith(prefix)) {
                startIndex = prefix.length();
            } else {
                startIndex = src.indexOf(separator + prefix) + separator.length() + prefix.length();
            }
            if (startIndex > -1 && startIndex < src.length()) {
                endIndex = src.indexOf(separator, startIndex);
                if (endIndex != -1) {
                    if (endIndex > startIndex) {
                        result = src.substring(startIndex, endIndex);
                    }
                } else {
                    result = src.substring(startIndex);
                }
            }
        }
        return result;
    }

    public static String encrypt(String plainText, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        return new String(Base64.encodeBase64(encrypted));
    }

    public static String decrypt(String encrypted, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decrypted = cipher.doFinal(Base64.decodeBase64(encrypted.getBytes("UTF-8")));
        return new String(decrypted);
    }


    public enum PassGenCharacterData implements CharacterData {
        LowerCase("INSUFFICIENT_LOWERCASE", "abcdefghijkmnopqrstuxyz"),
        UpperCase("INSUFFICIENT_UPPERCASE", "ABCDEFGHJKLMNPQRSTUXYZ"),
        Digit("INSUFFICIENT_DIGIT", "123456789"),
        Special("INSUFFICIENT_SPECIAL", "!#%+-@_");

        private final String errorCode;
        private final String characters;

        private PassGenCharacterData(String code, String charString) {
            this.errorCode = code;
            this.characters = charString;
        }

        public String getErrorCode() {
            return this.errorCode;
        }

        public String getCharacters() {
            return this.characters;
        }
    }

    public static String generatePassword() {
        List<CharacterRule> rules = Arrays.asList(
                // at least one upper-case character
                new CharacterRule(PassGenCharacterData.UpperCase, 2),
                // at least one lower-case character
                new CharacterRule(PassGenCharacterData.LowerCase, 4),
                // at least one digit character
                new CharacterRule(PassGenCharacterData.Digit, 1),
                // at least one special symbol
                new CharacterRule(PassGenCharacterData.Special, 1)
        );
        PasswordGenerator generator = new PasswordGenerator();

        //Password validation rule that prevents illegal sequences of characters (Alphabetical, Qwerty)
        IllegalSequenceRule illegalSequenceRule = new IllegalSequenceRule(new SequenceData() {
            public String getErrorCode() {
                return "ILLEGAL_SEQUENCE";
            }

            public CharacterSequence[] getSequences() {
                CharacterSequence[] arr = {
                        //Alphabetical Sequence ENG
                        new CharacterSequence("abcdefghijkmnopqrstuxyz"),
                        //Keyboard Sequence ENG
                        new CharacterSequence("qwertyuiop"), new CharacterSequence("asdfghjkl"), new CharacterSequence("zxcvbnm")
                };
                return arr;
            }
        }, IllegalSequenceRule.MINIMUM_SEQUENCE_LENGTH, false, false);

        String password = null;
        for (int i = 0; i < 5; i++) {
            password = generator.generatePassword(8, rules);

            if (illegalSequenceRule.validate(new PasswordData(password)).isValid()) {
                return password;
            }
        }
        throw new RuntimeException("Password generation error");
    }

    /**
     * Adding of suffix to each string at list
     *
     * @param strings contains strings without suffix
     * @param suffix    a suffix
     * @return strings with suffix
     */
    public static List<String> addSuffix(List<String> strings, String suffix) {
        List<String> result = new ArrayList<>();
        if (strings != null) {
            for (String exactString : strings) {
                result.add(exactString + suffix);
            }
        }
        return result;
    }

    /**
     * Clean app names
     *
     * @param appList contains names with suffix
     * @return names of applications without suffix
     */
    public static List<String> cleanAppNames(List<String> appList) {
        //TODO - need to standartify function
        List<String> result = new ArrayList<>();
        if (appList != null) {
            for (Object name : appList) {
                if (((String) name).endsWith("_SANDBOX")) {
                    result.add(((String) name).substring(0,
                            ((String) name).lastIndexOf("_SANDBOX")));
                } else if (((String) name).endsWith("_PRODUCTION")) {
                    result.add(((String) name).substring(0,
                            ((String) name).lastIndexOf("_PRODUCTION")));
                } else {
                    result.add((String) name);
                }
            }
        }
        return result;
    }

}
