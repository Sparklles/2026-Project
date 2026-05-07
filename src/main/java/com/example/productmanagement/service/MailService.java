package com.example.productmanagement.service;

/**
 * 邮件发送服务
 */
public interface MailService {

    /**
     * 发送纯文本邮件
     *
     * @param to      收件邮箱
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    void sendTextMail(String to, String subject, String content);
}
