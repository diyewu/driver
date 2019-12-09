package xz.driver.common.function;

import xz.driver.common.exception.RedisConnectException;

import java.io.IOException;

@FunctionalInterface
public interface JedisExecutor<T, R> {
    R excute(T t) throws RedisConnectException, IOException;
}
