package org.kerw1n.javautil.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.kerw1n.javautil.constant.BaseConst;
import org.kerw1n.javautil.file.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * HttpClient 工具类
 *
 * <li>
 * {@link #getInstance()} - 获取 HttpClientUtil 实例
 * {@link #setCharset(Charset)} - 设置字符集
 * {@link #setContentType(String)} - 设置媒体类型
 * {@link #addParam(String, String)} - 添加参数
 * {@link #addParam(Map)} - 添加参数
 * {@link #addHeaders(String, String)} - 添加请求头
 * {@link #addHeaders(Map)} - 添加请求头
 * {@link #get(String)} - GET 请求
 * {@link #post(String)} - POST 请求
 * {@link #post(String, String)} - POST 请求，JSON 参数
 * {@link #post(String, HttpEntity)} - POST 请求，自定义 HttpEntity
 * </li>
 *
 * @author kerw1n
 */
public class HttpClientUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    private RequestBuilder requestBuilder;
    private String contentType = MediaType.APPLICATION_JSON_VALUE;
    private Map<String, String> header;
    private Map<String, String> param;
    private Charset charset = BaseConst.CHARSET_UTF8;
    private static final int CONNECT_TIMEOUT = 60000;
    private static final int REQUEST_TIMEOUT = 60000;
    private static final int SOCKET_TIMEOUT = 60000;

    /**
     * 请求参数配置
     */
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setSocketTimeout(SOCKET_TIMEOUT)
            .build();

    /**
     * 获取 HttpClientUtil 实例
     *
     * @return
     */
    public static HttpClientUtil getInstance() {
        return SingleHttpClient.INSTANCE;
    }

    /**
     * 设置媒体类型
     *
     * @param contentType
     * @return
     */
    public HttpClientUtil setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置字符集
     *
     * @param charset
     * @return
     */
    public HttpClientUtil setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 添加参数
     *
     * @param key
     * @param value
     * @return
     */
    public HttpClientUtil addParam(String key, String value) {
        if (this.param == null) {
            this.param = new LinkedHashMap<>();
        }
        this.param.put(key, value);
        return this;
    }

    /**
     * 添加参数
     *
     * @param param
     * @return
     */
    public HttpClientUtil addParam(Map<String, String> param) {
        if (this.param == null) {
            this.param = new LinkedHashMap<>();
        }
        this.param.putAll(param);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param headers
     * @return
     */
    public HttpClientUtil addHeaders(Map<String, String> headers) {
        if (this.header == null) {
            this.header = new HashMap<>(6);
        }
        this.header.putAll(headers);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param key
     * @param value
     * @return
     */
    public HttpClientUtil addHeaders(String key, String value) {
        if (this.header == null) {
            this.header = new HashMap<>(6);
        }
        this.header.put(key, value);
        return this;
    }

    public HttpClientRequest get(String url) {
        requestBuilder = RequestBuilder.get(url).setConfig(REQUEST_CONFIG);
        if (this.param != null) {
            this.param.forEach((k, v) -> requestBuilder.addParameter(k, v));
        }
        return new HttpClientRequest();
    }

    public HttpClientRequest delete(String url) {
        requestBuilder = RequestBuilder.delete(url).setConfig(REQUEST_CONFIG);
        return new HttpClientRequest();
    }

    public HttpClientRequest put(String url) {
        requestBuilder = RequestBuilder.put(url).setConfig(REQUEST_CONFIG);
        return new HttpClientRequest();
    }

    public HttpClientRequest post(String url, String json) {
        return post(url, new StringEntity(json, charset));
    }

    public HttpClientRequest post(String url) {
        UrlEncodedFormEntity formEntity = null;
        if (this.param != null) {
            List<NameValuePair> params = new ArrayList<>(this.param.size());
            this.param.forEach((k, v) -> params.add(new BasicNameValuePair(k, v)));
            formEntity = new UrlEncodedFormEntity(params, charset);
        }
        return post(url, formEntity);
    }

    private HttpClientRequest post(String url, HttpEntity httpEntity) {
        this.requestBuilder = RequestBuilder.post(url).setConfig(REQUEST_CONFIG);
        if (httpEntity != null) {
            this.requestBuilder.setEntity(httpEntity);
            this.contentType = this.requestBuilder.getEntity().getContentType().getValue();
        }
        return new HttpClientRequest();
    }

    public class HttpClientRequest {
        private String responseContent;

        public HttpClientRequest() {
            if (charset != null) {
                requestBuilder.setCharset(charset);
            }

            if (header != null) {
                header.forEach((k, v) -> requestBuilder.addHeader(new BasicHeader(k, v)));
            }

            if (StringUtils.isNotEmpty(contentType)) {
                requestBuilder.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
            }
            request();
        }

        private void request() {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpUriRequest request = requestBuilder.build();

            URI uri = request.getURI();
            String scheme = uri.getScheme().toUpperCase();
            LOG.info(scheme + " " + request.getMethod() + " " + uri);
            LOG.info("Content-Type={}", contentType);

            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(request);
                responseContent = parseResult(response);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IoUtil.close(response, httpClient);
            }
        }

        public String getResponse() {
            return responseContent;
        }
    }

    private String parseResult(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        LOG.info("Response {}", response.getStatusLine());
        if (statusCode == HttpStatus.SC_OK) {
            return EntityUtils.toString(response.getEntity(), BaseConst.CHARSET_UTF8);
        }
        return "";
    }

    private HttpClientUtil() {
    }

    static class SingleHttpClient {
        static final HttpClientUtil INSTANCE = new HttpClientUtil();
    }
}
