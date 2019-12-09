package xz.research.common.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import xz.research.common.exception.RedisConnectException;

import java.io.IOException;

@FunctionalInterface
public interface JedisExecutor<T, R> {
    R excute(T t) throws RedisConnectException, IOException;
}
