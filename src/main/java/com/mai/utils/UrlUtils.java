package com.mai.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UrlUtils {
    public static boolean matchUrl(String requestUrl, String urlToMatch) {
        return Pattern.compile(UrlUtils.getUrlRegex(StringUtils.rightTrim(requestUrl, '/')))
                .matcher(StringUtils.rightTrim(urlToMatch, '/'))
                .matches();
    }

    public static List<String> getParamsFromUrl(String requestUrl, String urlToMatch) {
        var matches = Pattern.compile(UrlUtils.getUrlRegex(StringUtils.rightTrim(requestUrl, '/')))
                .matcher(StringUtils.rightTrim(urlToMatch, '/'));
        List<String> results = new ArrayList<>();
        if (matches.matches()) {
            for (int i = 1; i <= matches.groupCount(); i++) {
                results.add(matches.group(i));
            }
        }
        return results;
    }

    public static List<String> getParameterNames(String url) {
        return Pattern.compile("\\{(.+?)}")
                .matcher(url)
                .results()
                .map(matchResult -> matchResult.group(1))
                .collect(Collectors.toList());
    }

    public static Map<String, String> getParameterMap(String requestUrl, String urlToMatch) {
        var parameterNames = getParameterNames(requestUrl);
        var paramsFromUrl = getParamsFromUrl(requestUrl, urlToMatch);
        if (parameterNames.size() != paramsFromUrl.size())
            throw new IllegalArgumentException("Count of params must be same");

        return IntStream.range(0, parameterNames.size())
                .boxed()
                .collect(Collectors.toMap(parameterNames::get, paramsFromUrl::get));
    }

    private static String getUrlRegex(String url) {
        return url.replaceAll("\\{.+?}", "(.+?)").replace("/", "\\/");
    }
}
