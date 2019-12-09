package xz.research.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "custom") //接收application.yml中的custom下面的属性
public class CustomConfig {
    private String clientFileUpladPath;
    private String smtp;
    private String port;
    private String user;
    private String pwd;
    private String appid;
    private String secret;
    private String baidumapapikey;
    private String weixintoken;
    private String sendtimes;
    private String aeskeycode;
    private String mchid;
    private String notifyurl;
    private String weixinapikey;
    private boolean sandbox;
    private String certpath;

    private String cmsutf8baseurl;
    private String gbkbaseurl;
    private String smsid;
    private String smsaccount;
    private String smspassword;
    private String smscodetemp;
    private String smsactiontemp;

    public String getCmsutf8baseurl() {
        return cmsutf8baseurl;
    }

    public void setCmsutf8baseurl(String cmsutf8baseurl) {
        this.cmsutf8baseurl = cmsutf8baseurl;
    }

    public String getGbkbaseurl() {
        return gbkbaseurl;
    }

    public void setGbkbaseurl(String gbkbaseurl) {
        this.gbkbaseurl = gbkbaseurl;
    }

    public String getSmsid() {
        return smsid;
    }

    public void setSmsid(String smsid) {
        this.smsid = smsid;
    }

    public String getSmsaccount() {
        return smsaccount;
    }

    public void setSmsaccount(String smsaccount) {
        this.smsaccount = smsaccount;
    }

    public String getSmspassword() {
        return smspassword;
    }

    public void setSmspassword(String smspassword) {
        this.smspassword = smspassword;
    }

    public String getClientFileUpladPath() {
        return clientFileUpladPath;
    }

    //String类型的一定需要setter来接收属性值；maps, collections, 和 arrays 不需要
    public void setClientFileUpladPath(String clientFileUpladPath) {
        this.clientFileUpladPath = clientFileUpladPath;
    }

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }


    public String getBaidumapapikey() {
        return baidumapapikey;
    }

    public void setBaidumapapikey(String baidumapapikey) {
        this.baidumapapikey = baidumapapikey;
    }

    public String getWeixintoken() {
        return weixintoken;
    }

    public void setWeixintoken(String weixintoken) {
        this.weixintoken = weixintoken;
    }

    public String getSendtimes() {
        return sendtimes;
    }

    public void setSendtimes(String sendtimes) {
        this.sendtimes = sendtimes;
    }

    public String getAeskeycode() {
        return aeskeycode;
    }

    public void setAeskeycode(String aeskeycode) {
        this.aeskeycode = aeskeycode;
    }

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String mchid) {
        this.mchid = mchid;
    }

    public String getNotifyurl() {
        return notifyurl;
    }

    public void setNotifyurl(String notifyurl) {
        this.notifyurl = notifyurl;
    }

    public String getWeixinapikey() {
        return weixinapikey;
    }

    public void setWeixinapikey(String weixinapikey) {
        this.weixinapikey = weixinapikey;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public String getCertpath() {
        return certpath;
    }

    public void setCertpath(String certpath) {
        this.certpath = certpath;
    }

    public String getSmscodetemp() {
        return smscodetemp;
    }

    public void setSmscodetemp(String smscodetemp) {
        this.smscodetemp = smscodetemp;
    }

    public String getSmsactiontemp() {
        return smsactiontemp;
    }

    public void setSmsactiontemp(String smsactiontemp) {
        this.smsactiontemp = smsactiontemp;
    }
}
