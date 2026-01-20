package kr.allcll.backend.support.web;

import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;

public class PrefixParser {

    public static List<String> extractDistinct(List<String> ids) {
        return ids.stream()
            .map(PrefixParser::extract)
            .distinct()
            .toList();
    }

    public static String extract(String id) {
        int open = id.indexOf('[');
        int close = id.indexOf(']');
        if (prefixNotExist(open, close)) {
            throw new AllcllException(AllcllErrorCode.PREFIX_NOT_FOUND);
        }
        return id.substring(open + 1, close);
    }

    private static boolean prefixNotExist(int open, int close) {
        return isOpenMissing(open) || isCloseMissing(close) || isBracketedOrderInvalid(open, close);
    }

    private static boolean isOpenMissing(int open) {
        return open < 0;
    }

    private static boolean isCloseMissing(int close) {
        return close < 0;
    }

    private static boolean isBracketedOrderInvalid(int open, int close) {
        return close <= open + 1;
    }
}
