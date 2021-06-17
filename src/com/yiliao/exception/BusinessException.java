package com.yiliao.exception;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.context.i18n.LocaleContextHolder;

public class BusinessException extends RuntimeException{
	  /**
	 * 
	 */
	private static final long serialVersionUID = 2148374270769534530L;
	
	/**
	 * 是否要从将错误信息国际化要考虑
	 */
	private static final String ERROR_BUNDLE = "i18n/errors";

	/**
     * 错误代码,默认为未知错误
     */
	protected int errorCode = 0;

    /**
     * 错误信息中的参数
     */
    protected String[] errorArgs = null;

    /**
     * 兼容纯错误信息，不含error code,errorArgs的情况
     */
    protected String errorMessage = null;

    /**
     * 错误信息的i18n ResourceBundle.
     */
    final static protected ResourceBundle rb = ResourceBundle.getBundle(ERROR_BUNDLE,LocaleContextHolder.getLocale());
    
    public BusinessException() {
        super();
    }

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(int errorCode) {
		this.errorCode = errorCode;
	}
	
    public BusinessException(String errorMessage) {
    	super(errorMessage);
    	this.errorMessage=errorMessage;
    }

    /**
     * 获得出错信息.
     * 读取i18N properties文件中Error Code对应的message,并组合参数获得i18n的出错信息.
     */
    @Override
    public String getMessage() {
        //如果errorMessage不为空,直接返回出错信息.
        if (errorMessage != null) {
            return errorMessage;
        }
        //否则用errorCode查询Properties文件获得出错信息
        String message;
        try {
            message = rb.getString(String.valueOf(errorCode));
        }
        catch (MissingResourceException mse) {
            message = "ErrorCode is: " + errorCode + ", but can't get the message of the Error Code";
        }

        //将出错信息中的参数代入到出错信息中
        if (errorArgs != null)
            message = MessageFormat.format(message, (Object[]) errorArgs);

        return message + ", Error Code is: " + errorCode;

    }
}
