package xz.driver.common.service;

import xz.driver.common.domain.RedisInfo;
import xz.driver.common.exception.RedisConnectException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {


    /**  redis 命令行操作map
     *
     hset key field value   单个设置

     hget key field   获取map中指定key的值

     HMSET key field value [field value ...]   多个设置

     HMGET key field [field ...]  获取map中多个key的值

     HGETALL key   获取map中所有的数据

     hdel key field [field ...]  删除map中指定key的数据

     HINCRBY key field increment   对map中 指定key值进行加法计算

     hlen key     获取map的大小

     hkeys key    获取map中所有的key值

     HVALS key   获取map中所有的value值

     HEXISTS key field  判断map中指定key是否存在

     */


    /**
     * 获取 redis 的详细信息
     *
     * @return List
     */
    List<RedisInfo> getRedisInfo() throws RedisConnectException;

    /**
     * 获取 redis key 数量
     *
     * @return Map
     */
    Map<String, Object> getKeysSize() throws RedisConnectException;

    /**
     * 获取 redis 内存信息
     *
     * @return Map
     */
    Map<String, Object> getMemoryInfo() throws RedisConnectException;

    /**
     * 获取 key
     *
     * @param pattern 正则
     * @return Set
     */
    Set<String> getKeys(String pattern) throws RedisConnectException;

    /**
     * get命令
     *
     * @param key key
     * @return String
     */
    String get(String key) throws RedisConnectException;

    /**
     * set命令
     *
     * @param key   key
     * @param value value
     * @return String
     */
    String set(String key, String value) throws RedisConnectException;

    /**
     * set  对象为map
     * @param key
     * @param value
     * @return
     */
    String setMap(String key, Map<String, String> value) throws RedisConnectException;

    /**
     * get 对象为map
     * @param key
     * @return
     * @throws RedisConnectException
     */
    Set<String> getMapKeys(String key) throws RedisConnectException;

    /**
     * 判断map中指定key是否存在
     * @param key
     * @param mapKey
     * @return
     */
    boolean mapExistsKey(String key, String mapKey) throws RedisConnectException;

    /**
     * 单个map值设置
     * @param key
     * @param mapKey
     * @param mapValue
     * @return
     */
    Long hset(String key, String mapKey, RcRegion r) throws RedisConnectException;

    RcRegion hget(String key, String mapKey) throws RedisConnectException, IOException;

    /**
     * set 命令
     *
     * @param key         key
     * @param value       value
     * @param milliscends 毫秒
     * @return String
     */
    String set(String key, String value, Long milliscends) throws RedisConnectException;

    /**
     * del命令
     *
     * @param key key
     * @return Long
     */
    Long del(String... key) throws RedisConnectException;

    /**
     * exists命令
     *
     * @param key key
     * @return Boolean
     */
    Boolean exists(String key) throws RedisConnectException;

    /**
     * pttl命令
     *
     * @param key key
     * @return Long
     */
    Long pttl(String key) throws RedisConnectException;

    /**
     * pexpire命令
     *
     * @param key         key
     * @param milliscends 毫秒
     * @return Long
     */
    Long pexpire(String key, Long milliscends) throws RedisConnectException;


    /**
     * zadd 命令
     *
     * @param key    key
     * @param score  score
     * @param member value
     */
    Long zadd(String key, Double score, String member) throws RedisConnectException;

    /**
     * zrangeByScore 命令
     *
     * @param key key
     * @param min min
     * @param max max
     * @return Set<String>
     */
    Set<String> zrangeByScore(String key, String min, String max) throws RedisConnectException;

    /**
     * zremrangeByScore 命令
     *
     * @param key   key
     * @param start start
     * @param end   end
     * @return Long
     */
    Long zremrangeByScore(String key, String start, String end) throws RedisConnectException;

    /**
     * zrem 命令
     *
     * @param key     key
     * @param members members
     * @return Long
     */
    Long zrem(String key, String... members) throws RedisConnectException;
}
