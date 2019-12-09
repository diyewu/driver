package xz.research.app.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xz.research.app.domain.MapInfo;
import xz.research.app.manager.AppUserManager;
import xz.research.common.authentication.JWTToken;
import xz.research.common.authentication.JWTUtil;
import xz.research.common.config.CustomConfig;
import xz.research.common.domain.ActiveUser;
import xz.research.common.domain.FebsConstant;
import xz.research.common.domain.FebsResponse;
import xz.research.common.exception.FebsException;
import xz.research.common.properties.FebsProperties;
import xz.research.common.service.RedisService;
import xz.research.utils.*;
import xz.research.system.dao.LoginLogMapper;
import xz.research.system.domain.LoginLog;
import xz.research.system.domain.User;
import xz.research.system.manager.UserManager;
import xz.research.system.service.LoginLogService;
import xz.research.system.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;

@Validated
@RestController
@RequestMapping("app")
public class AppLoginController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private AppUserManager appUserManager;

    @Autowired
    private UserManager userManager;
    @Autowired
    private UserService userService;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    private LoginLogMapper loginLogMapper;
    @Autowired
    private FebsProperties properties;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomConfig customConfig;


    @PostMapping("/login")
    @ApiOperation("APP登陆入口")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "username", value = "用户名", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "password", value = "密码", required = true)
    }
    )
    public FebsResponse login(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password, HttpServletRequest request) throws Exception {
        username = StringUtils.lowerCase(username);
        password = MD5Util.encrypt(username, password);
        // 示例代码
        customConfig.getAppid();




        // 示例代码


        final String errorMessage = "用户名或密码错误";
        User user = this.userManager.getUser(username);

        if (user == null)
            throw new FebsException(errorMessage);
        if (!StringUtils.equals(user.getPassword(), password))
            throw new FebsException(errorMessage);
        if (User.STATUS_LOCK.equals(user.getStatus()))
            throw new FebsException("账号已被锁定,请联系管理员！");

        // 更新用户登录时间
        this.userService.updateLoginTime(username);
        // 保存登录记录
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        this.loginLogService.saveLoginLog(loginLog);

        String token = FebsUtil.encryptToken(JWTUtil.sign(username, password));
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
        String expireTimeStr = DateUtil.formatFullTime(expireTime);
        JWTToken jwtToken = new JWTToken(token, expireTimeStr);

//        String userId = this.saveTokenToRedis(user, jwtToken, request);
//        user.setId(userId);
        String userId = this.saveTokenToRedis(user, jwtToken, request);

        Map<String, Object> userInfo = this.generateUserInfo(jwtToken, user);
        return new FebsResponse().message("认证成功").data(userInfo);
    }


    /**
     * 生成前端需要的用户信息，包括：
     * 1. token
     * 2. Vue Router
     * 3. 用户角色
     * 4. 用户权限
     * 5. 前端系统个性化配置信息
     *
     * @param token token
     * @param user  用户信息
     * @return UserInfo
     */
    private Map<String, Object> generateUserInfo(JWTToken token, User user) {
        String username = user.getUsername();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token.getToken());
        userInfo.put("exipreTime", token.getExipreAt());

        Set<String> roles = this.appUserManager.getUserRoles(username);
        userInfo.put("roles", roles);

//        Set<String> permissions = this.appUserManager.getUserPermissions(username);
//        userInfo.put("permissions", permissions);

//        UserConfig userConfig = this.appUserManager.getUserConfig(String.valueOf(user.getUserId()));
//        userInfo.put("config", userConfig);

        //TODO mapinfo
        List<MapInfo> mapInfo = new ArrayList<MapInfo>();
        userInfo.put("mapInfo", mapInfo);

//        user.setPassword("it's a secret");
//        userInfo.put("user", user);
        return userInfo;
    }


    private String saveTokenToRedis(User user, JWTToken token, HttpServletRequest request) throws Exception {
        String ip = IPUtil.getIpAddr(request);

        // 构建在线用户
        ActiveUser activeUser = new ActiveUser();
        activeUser.setUsername(user.getUsername());
        activeUser.setIp(ip);
        activeUser.setToken(token.getToken());
        activeUser.setLoginAddress(AddressUtil.getCityInfo(ip));
        activeUser.setPhone(user.getMobile());

        // zset 存储登录用户，score 为过期时间戳
        this.redisService.zadd(FebsConstant.ACTIVE_USERS_ZSET_PREFIX, Double.valueOf(token.getExipreAt()), mapper.writeValueAsString(activeUser));
        // redis 中存储这个加密 token，key = 前缀 + 加密 token + .ip
        this.redisService.set(FebsConstant.TOKEN_CACHE_PREFIX + token.getToken() + StringPool.DOT + ip, token.getToken(), properties.getShiro().getJwtTimeOut() * 1000);

        return activeUser.getId();
    }
}
