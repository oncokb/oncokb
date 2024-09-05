package org.mskcc.cbio.oncokb.controller.advice;

import org.springframework.http.HttpStatus;

/**
* Represents an HTTP error response with detailed information about the error.
* This class captures the title, status, detail, path, and message associated with the HTTP error.
*
* Example JSON representation:
* <pre>
* {
*   "title": "Method Not Allowed",
*   "status": 405,
*   "detail": "Request method 'POST' is not supported",
*   "instance": "/index.html",
*   "message": "error.http.405",
*   "path": "/index.html"
* }
* </pre>
*/
public class ApiHttpError {
    /** The title of the error, typically representing the HTTP status text. */
    private String title;

    /** The HTTP status code of the error (e.g., 404, 500). */
    private int status;

    /** A detailed message explaining the error. */
    private String detail;

    /** The request path that caused the error. */
    private String path;

    /** A message key that is standardize to help with client side error handling, such as "error.http.404". */
    private String message;

    /**
    * Constructs an ApiHttpError object with the specified HTTP path, status, and message.
    *
    * @param path    The request path that caused the error.
    * @param status  The HTTP status code of the error.
    * @param message A detailed message explaining the error.
    */
    public ApiHttpError(String path, HttpStatus status, String message) {
        this.setPath(path);
        this.setTitle(status.toString());
        this.setStatus(status.value());
        this.setDetail(message);
        this.setMessage("error.http." + status.value());
    }

    /**
    * Gets the title of the error, typically representing the status text.
    *
    * @return The title of the error.
    */
    public String getTitle() {
        return title;
    }

    /**
    * Sets the title of the error.
    *
    * @param title The title of the error.
    */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
    * Gets the HTTP status code of the error.
    *
    * @return The HTTP status code.
    */
    public int getStatus() {
        return status;
    }

    /**
    * Sets the HTTP status code of the error.
    *
    * @param status The HTTP status code.
    */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
    * Gets the detailed description of the error.
    *
    * @return The detail of the error.
    */
    public String getDetail() {
        return detail;
    }

    /**
    * Sets the detailed description of the error.
    *
    * @param detail The detail of the error.
    */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
    * Gets the request path that caused the error.
    *
    * @return The request path.
    */
    public String getPath() {
        return path;
    }

    /**
    * Sets the request path that caused the error.
    *
    * @param path The request path.
    */
    public void setPath(String path) {
        this.path = path;
    }

    /**
    * Gets the error message, typically used for localization.
    *
    * @return The error message.
    */
    public String getMessage() {
        return message;
    }

    /**
    * Sets the error message.
    *
    * @param message The error message.
    */
    public void setMessage(String message) {
        this.message = message;
    }
}

