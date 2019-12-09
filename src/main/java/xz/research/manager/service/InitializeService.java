package xz.research.manager.service;

import xz.research.common.exception.RedisConnectException;

import java.io.IOException;

public interface InitializeService {

    /**
     * 加载地址数据信息到redies，防止重复插入
     */
    void loadRegionData() throws RedisConnectException, IOException;

}
