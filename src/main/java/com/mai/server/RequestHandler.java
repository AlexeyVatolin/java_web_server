package com.mai.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mai.annotataions.RequestMapping;
import com.mai.annotataions.RequestBody;
import com.mai.utils.StringUtils;
import com.mai.utils.UrlUtils;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.sql.rowset.serial.SerialException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RequestHandler implements Runnable {
    private Socket socket;
    private String packagePrefix;
    private static Logger log = Logger.getLogger(RequestHandler.class);

    public RequestHandler(Socket socket, String packagePrefix) {
        this.socket = socket;
        this.packagePrefix = packagePrefix;
    }

    public void run() {
        try {
            Request request = Request.parse(socket.getInputStream());
            Method currentMethod = getMethodByUrl(request.getPath(), request.getMethod());

            if (currentMethod != null) {
                var currentAnnotation = currentMethod.getAnnotation(RequestMapping.class);
                var methodObject = currentMethod.getDeclaringClass().getConstructor().newInstance();
                var methodParameters = List.of(currentMethod.getParameters());
                Object result = null;
                if (currentMethod.getParameterCount() > 0) {
                    Object[] parameters = new Object[currentMethod.getParameterCount()];
                    Map<String, String> requestParameters = UrlUtils.getParameterMap(currentAnnotation.url(), request.getPath());

                    // Pass request body
                    var requestBodyParameter = methodParameters.stream()
                            .filter(parameter -> parameter.getAnnotation(RequestBody.class) != null)
                            .collect(Collectors.toList());
                    if (requestBodyParameter.size() > 1) {
                        throw new InvalidParameterException("Method must have only one parameter with RequestBody annotation");
                    }

                    if (requestBodyParameter.size() > 0) {
                        var indexBodyParam = methodParameters.indexOf(requestBodyParameter.get(0));
                        parameters[indexBodyParam] = request.getBody();
                    }

                    // Pass params from url
                    if (parameters.length - requestBodyParameter.size() != requestParameters.size())
                        throw new InvalidParameterException("Count of request url parameters and method parameters " +
                                "must be the same");

                    for (var i = 0; i < methodParameters.size(); i++) {
                        if (methodParameters.get(i).getAnnotation(RequestBody.class) == null) {
                            var requestParam = requestParameters.get(methodParameters.get(i).getName());
                            var castedParam = StringUtils.stringToNumber(requestParam, methodParameters.get(i).getType());
                            parameters[i] = castedParam;
                        }
                    }
                    result = currentMethod.invoke(methodObject, parameters);
                } else {
                    result = currentMethod.invoke(methodObject);
                }
                try {
                    if (result == null) result = "";
                    String response = this.objectToString(result, currentMethod.getReturnType());
                    ResponseBuilder.writeContent(response, socket.getOutputStream());
                } catch (SerialException e) {
                    ResponseBuilder.writeError(socket.getOutputStream(), e);
                    log.error(e);
                }

            } else {
                ResponseBuilder.write404(socket.getOutputStream());
            }
            log.info("Response sent");
            socket.close();
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    private String objectToString(Object object, Class<?> type) throws SerialException, JsonProcessingException {
        if (type.equals(String.class)) return (String) object;
        else if (object instanceof Serializable) {
            SerializationHelper<Serializable> serializationHelper = new SerializationHelper<>();
            return serializationHelper.convertToJsonString(((Serializable) object));
        } else {
            throw new SerialException("Object is not serializable");
        }
    }

    private Method getMethodByUrl(String url, HttpMethod httpMethod) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packagePrefix))
                .setScanners(new MethodAnnotationsScanner()));
        Set<Method> getMethods = reflections.getMethodsAnnotatedWith(RequestMapping.class);

        Optional<Method> currentMethod = getMethods.stream()
                .filter(method -> UrlUtils.matchUrl(method.getAnnotation(RequestMapping.class).url(), url) &&
                        method.getAnnotation(RequestMapping.class).method() == httpMethod)
                .findFirst();
        return currentMethod.orElse(null);
    }

    private Object matchUrl(String urlToMatch, String currentUrl) {
        Pattern pattern = Pattern.compile(StringUtils.rightTrim(urlToMatch, '/'), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(StringUtils.rightTrim(currentUrl, '/'));
        if (matcher.matches()) {
            if (matcher.groupCount() > 0) return matcher.group(1);
            else return true;
        } else return null;
    }

//    private Map<String, String> getParamsFromUrl(String urlToMatch, String currentUrl) {
//        urlToMatch = StringUtils.rightTrim(urlToMatch, '/');
//        currentUrl = StringUtils.rightTrim(currentUrl, '/');
//        Pattern pattern = Pattern.compile(urlToMatch, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(StringUtils.rightTrim(currentUrl, '/'));
//        if (matcher.matches()) {
//            if (matcher.groupCount() > 0) return matcher.group(1);
//            else return true;
//        } else return null;
//    }


}
