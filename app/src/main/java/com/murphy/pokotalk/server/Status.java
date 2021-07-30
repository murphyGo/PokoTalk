package com.murphy.pokotalk.server;

/* Server response status(success or fail) and details */
public class Status {
    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    protected int status;
    protected Object errorCode;
    protected Object errorDetail;

    public Status(int status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status == this.SUCCESS;
    }

    public void setErrorCode(Object errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorDetail(Object errorDetail) {
        this.errorDetail = errorDetail;
    }

    public Object getErrorCode() {
        return errorCode;
    }

    public Object getErrorDetail() {
        return errorDetail;
    }
}
