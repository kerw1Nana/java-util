package org.kerw1n.javautil.http;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.kerw1n.javautil.constant.BaseConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RestTemplate 远程调用工具类
 * <p>
 * 他处注入Bean {@link RestTemplate} 会报 NPE，此类提供了 RestTemplate 单例，可自行调用。
 *
 * @author Guan Yonchao
 */
public final class RestClientUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RestClientUtil.class);

    private HttpEntity httpEntity;
    private HttpHeaders httpHeaders;
    private String contentType;
    private Map<String, String> param;

    private static final int CONNECT_TIMEOUT = 60000;
    private static final int READ_TIMEOUT = 60000;
    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private RestClientUtil() {
    }

    /**
     * 初始化
     *
     * @return
     */
    public static RestClientUtil create() {
        return new RestClientUtil();
    }

    /**
     * post 请求
     *
     * @param url 请求地址
     * @return
     */
    public String post(String url) {
        return post(url, String.class);
    }

    /**
     * post 请求
     *
     * @param url          请求地址
     * @param responseType 响应类型
     * @return
     */
    public <T> T post(String url, Class<T> responseType) {
        this.build();
        return getInstance().postForObject(url, httpEntity, responseType);
    }

    /**
     * get 请求
     *
     * @param url 请求地址
     * @return
     */
    public String get(String url) {
        return get(url, String.class);
    }

    /**
     * get 请求
     *
     * @param url          请求地址
     * @param responseType 响应类型
     * @return
     */
    public <T> T get(String url, Class<T> responseType) {
        this.build();
        URI uri = null;
        try {
            URIBuilder builder = new URIBuilder(url);
            if (this.param != null) {
                this.param.forEach((k, v) -> builder.addParameter(k, v));
            }
            uri = builder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        return getInstance().exchange(uri, HttpMethod.GET, httpEntity, responseType).getBody();
    }

    /**
     * 添加请求头
     *
     * @param key
     * @param value
     * @return
     */
    public RestClientUtil addHeader(String key, String value) {
        if (this.httpHeaders == null) {
            this.httpHeaders = new HttpHeaders();
        }
        this.httpHeaders.add(key, value);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param headers
     * @return
     */
    public RestClientUtil addHeader(MultiValueMap<String, String> headers) {
        if (this.httpHeaders == null) {
            this.httpHeaders = new HttpHeaders();
        }
        this.httpHeaders.addAll(headers);
        return this;
    }

    /**
     * 设置媒体类型
     *
     * @param contentType
     * @return
     */
    public RestClientUtil setContentType(String contentType) {
        this.contentType = contentType;
        return addHeader(HttpHeaders.CONTENT_TYPE, this.contentType);
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public RestClientUtil addParam(String key, String value) {
        if (this.param == null) {
            this.param = new LinkedHashMap<>();
        }
        this.param.put(key, value);
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param param
     * @return
     */
    public RestClientUtil addParam(Map<String, String> param) {
        if (this.param == null) {
            this.param = new LinkedHashMap<>();
        }
        this.param.putAll(param);
        return this;
    }

    /**
     * 构建 HttpEntity
     */
    private void build() {
        if (StringUtils.isEmpty(contentType) || DEFAULT_CONTENT_TYPE.equals(contentType)) {
            this.setContentType(DEFAULT_CONTENT_TYPE);
            this.httpEntity = new HttpEntity<>((param != null ? JSON.toJSONString(param) : null), httpHeaders);
        } else if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(contentType)) {
            if (param != null) {
                MultiValueMap<Object, Object> formData = new LinkedMultiValueMap<>();
                this.param.keySet().forEach(key -> formData.add(key, param.get(key)));
                this.httpEntity = new HttpEntity<>(formData, httpHeaders);
            } else {
                this.httpEntity = new HttpEntity<>(null, httpHeaders);
            }
        } else {
            throw new RuntimeException(String.format("Unsupported content type %s ", contentType));
        }
    }

    /**
     * 请求拦截器，用于记录日志
     *
     * @author kerw1n
     */
    public static class HttpLogInterceptor implements ClientHttpRequestInterceptor {
        private static final Logger LOG = LoggerFactory.getLogger(HttpLogInterceptor.class);

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            requestLog(request, body);
            ClientHttpResponse response = null;
            try {
                response = execution.execute(request, body);
            } catch (SocketTimeoutException e) {
                LOG.error("Http SocketTimeOutException,{}", e.getMessage());
                return null;
            }
            responseLog(response);
            return response;
        }

        private void requestLog(HttpRequest request, byte[] body) throws IOException {
            URI uri = request.getURI();
            String scheme = uri.getScheme().toUpperCase();
            LOG.info(scheme + " " + request.getMethod() + " " + uri);
            LOG.info("Content-Type={}", request.getHeaders().getContentType());
//        LOG.info("Writing {}", new String(body, BaseConst.CHARSET_UTF8));
        }

        private void responseLog(ClientHttpResponse response) throws IOException {
            LOG.info("Response {}", response.getStatusCode());
//        LOG.info("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
        }

    }

    /**
     * 获取 RestTemplate 实例
     *
     * @return
     */
    public static RestTemplate getInstance() {
        RestTemplate instance = SingleRestTemplate.INSTANCE;
        instance.getMessageConverters().set(1, new StringHttpMessageConverter(BaseConst.CHARSET_UTF8));
        ClientHttpRequestInterceptor interceptor = new HttpLogInterceptor();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(1);
        interceptors.add(interceptor);
        instance.setInterceptors(interceptors);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        instance.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        return instance;
    }

    static class SingleRestTemplate {
        static final RestTemplate INSTANCE = new RestTemplate();
    }

}
