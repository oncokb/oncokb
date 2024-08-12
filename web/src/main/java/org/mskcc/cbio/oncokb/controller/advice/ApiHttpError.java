package org.mskcc.cbio.oncokb.controller.advice;

import org.springframework.http.HttpStatus;

public class ApiHttpError {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String path;
    private String message;

    public ApiHttpError(String path, HttpStatus status, String message) {
        this.setType("https://www.oncokb.org/problem/problem-with-message");
        this.setPath(path);
        this.setTitle(status.toString());
        this.setStatus(status.value());
        this.setDetail(status.value() + " " + status.name());
        this.setMessage(message);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

