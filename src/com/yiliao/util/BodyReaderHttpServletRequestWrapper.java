package com.yiliao.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;


public class BodyReaderHttpServletRequestWrapper extends  HttpServletRequestWrapper {

	
	private final byte[] body;
	
	public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		 // body = StreamUtil.readBytes(request.getReader(), JoddDefault.encoding);  
	     // 因为http协议默认传输的编码就是iso-8859-1,如果使用utf-8转码乱码的话，可以尝试使用iso-8859-1
	     body =readBytes(request.getReader(), "UTF-8");
	}

	@Override  
    public BufferedReader getReader() throws IOException {  
        return new BufferedReader(new InputStreamReader(getInputStream()));  
    }  
  
    @Override  
    public ServletInputStream getInputStream() throws IOException {  
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);  
        return new ServletInputStream() {  
  
            @Override  
            public int read() throws IOException {  
                return bais.read();  
            }  
        };  
    } 
    
    /**
     * 通过BufferedReader和字符编码集转换成byte数组
     * @param br
     * @param encoding
     * @return
     * @throws IOException
     */
    private byte[] readBytes(BufferedReader br,String encoding) throws IOException{
        String str = null,retStr="";
        while ((str = br.readLine()) != null) {
            retStr += str;
        }
        if (StringUtils.isNotBlank(retStr)) {
            return retStr.getBytes(Charset.forName(encoding));
        }
        return null;
    }
}
