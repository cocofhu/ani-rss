package ani.rss.msg;

import ani.rss.entity.Ani;
import ani.rss.entity.Config;
import ani.rss.entity.MyMailAccount;
import ani.rss.enums.MessageEnum;
import ani.rss.util.ExceptionUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 邮箱
 */
@Slf4j
public class Mail implements Message {
    @Override
    public Boolean send(Config config, Ani ani, String text, MessageEnum messageEnum) {
        String messageBody = replaceMessageTemplate(ani, config.getMessageTemplate(), text, messageEnum);
        Boolean mail = config.getMail();
        MyMailAccount myMailAccount = config.getMailAccount();
        String mailAddressee = config.getMailAddressee();

        if (!mail) {
            return false;
        }
        String from = myMailAccount.getFrom();
        String host = myMailAccount.getHost();
        String pass = myMailAccount.getPass();
        Assert.notBlank(from, "发件人邮箱 为空");
        Assert.notBlank(host, "SMTP地址 为空");
        Assert.notBlank(pass, "密码 为空");
        Assert.notBlank(mailAddressee, "收件人 为空");

        MailAccount mailAccount = new MailAccount();
        BeanUtil.copyProperties(myMailAccount, mailAccount, CopyOptions
                .create()
                .setIgnoreNullValue(true));
        mailAccount.setUser(from)
                .setFrom(StrFormatter.format("ani-rss <{}>", from))
                .setAuth(true);

        messageBody = messageBody.replace("\n", "<br/>");

        Boolean mailImage = config.getMailImage();
        if (mailImage) {
            String image = "https://docs.wushuo.top/null.png";

            if (Objects.nonNull(ani)) {
                image = ani.getImage();
            }

            messageBody += StrFormatter.format("<br/><img src=\"{}\"/>", image);
        }

        try {
            MailUtil.send(mailAccount, List.of(mailAddressee), text.length() > 200 ? ani.getTitle() : text, messageBody, true);
            return true;
        } catch (Exception e) {
            String message = ExceptionUtil.getMessage(e);
            log.error(message, e);
            return false;
        }
    }
}
