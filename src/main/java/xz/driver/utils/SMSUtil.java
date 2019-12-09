package xz.driver.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

/**
 * yml 文件中自定义参数解析对象
 * 使用注解 @Value 来获取
 * 在setter方法上去掉了static
 */
@Component
public class SMSUtil {
    //	private static String utf8BaseUrl = "http://39.98.198.70:8868/sms.aspx";
//	private static String GBKBaseUrl = "http://39.98.198.70:8868/smsGBK.aspx";
//	private static String SMSID="882";
//	private static String SMSAccount="wu_di_ye1";
//	private static String SMSpassword="123456";
    private static String utf8BaseUrl;
    private static String GBKBaseUrl;
    private static String SMSID;
    private static String SMSAccount;
    private static String SMSpassword;
    private static String SMScodetemp;
    private static String SMSactiontemp;
    private static boolean enablesms;


    public static String getUtf8BaseUrl() {
        return utf8BaseUrl;
    }

    @Value("${custom.utf8BaseUrl}")
    public void setUtf8BaseUrl(String utf8BaseUrl) {
        SMSUtil.utf8BaseUrl = utf8BaseUrl;
    }

    public static String getGBKBaseUrl() {
        return GBKBaseUrl;
    }

    @Value("${custom.gbkbaseurl}")
    public void setGBKBaseUrl(String GBKBaseUrl) {
        SMSUtil.GBKBaseUrl = GBKBaseUrl;
    }

    public static String getSMSID() {
        return SMSID;
    }

    @Value("${custom.smsid}")
    public void setSMSID(String SMSID) {
        SMSUtil.SMSID = SMSID;
    }

    public static String getSMSAccount() {
        return SMSAccount;
    }

    @Value("${custom.smsaccount}")
    public void setSMSAccount(String SMSAccount) {
        SMSUtil.SMSAccount = SMSAccount;
    }

    public static String getSMSpassword() {
        return SMSpassword;
    }

    @Value("${custom.smspassword}")
    public void setSMSpassword(String SMSpassword) {
        SMSUtil.SMSpassword = SMSpassword;
    }


    public static String getSMScodetemp() {
        return SMScodetemp;
    }

    @Value("${custom.smscodetemp}")
    public  void setSMScodetemp(String SMScodetemp) {
        SMSUtil.SMScodetemp = SMScodetemp;
    }

    public static String getSMSactiontemp() {
        return SMSactiontemp;
    }

    @Value("${custom.smsactiontemp}")
    public  void setSMSactiontemp(String SMSactiontemp) {
        SMSUtil.SMSactiontemp = SMSactiontemp;
    }

    public static boolean isEnablesms() {
        return enablesms;
    }

    @Value("${custom.enablesms}")
    public void setEnablesms(boolean enablesms) {
        SMSUtil.enablesms = enablesms;
    }

    public static void sendSMS(String mobile, int type, String code, String user, String action) {
        if(enablesms){
            try {
                String content = "";
                if (type == 0) {//code
                    content = URLEncoder.encode(SMScodetemp.replace("$(code)", code), "UTF-8");
                } else {
                    content = URLEncoder.encode(SMSactiontemp.replace("$(user)", user).replace("$(action)", action), "UTF-8");
                }
                String url = utf8BaseUrl + "?action=send&userid=" + SMSID + "&account=" + SMSAccount + "&password=" + SMSpassword + "&mobile=" + mobile + "&content=" + content + "&sendTime=&extno=&qq=1";
                String string = HttpUtil.sendPost(url, null);
                System.out.println("__________" + string);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        String content = "【普查通】尊敬的用户：您的验证码为：111000（60分钟内有效），为了保证您的账户安全，请勿向任何人提供此验证码。";
//		content = "【芊乐零食屋】尊敬的用户：您的验证码为：111000（60分钟内有效），为了保证您的账户安全，请勿向任何人提供此验证码。";
        content = "【普查通】尊敬的用户${user}，您的操作${action}已经完成，请尽快登陆系统查看。";
        sendSMS("18936483081", 1, null, "admin", "数据导入");
    }
}
