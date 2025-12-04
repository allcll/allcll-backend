package kr.allcll.backend.support.web;

import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;

public class PrefixParser {

    public static List<String> extractAllWithOutDuplicate(List<String> ids) {
        return ids.stream()
            .map(PrefixParser::extract)
            .distinct()
            .toList();
    }

    public static String extract(String id) {
        int start = id.indexOf('[') + 1;
        int end = id.indexOf(']');
        if (prefixNotExist(start, end)) {
            throw new AllcllException(AllcllErrorCode.PREFIX_NOT_FOUND);
        }
        return id.substring(start, end);
    }

    private static boolean prefixNotExist(int start, int end) {
        return start == 0 || end == 1 || start >= end - 1;
    }
}
