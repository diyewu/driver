package xz.research.manager.service.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import xz.research.common.exception.RedisConnectException;
import xz.research.common.service.RedisService;
import xz.research.manager.entity.RcRegion;
import xz.research.manager.service.IRcRegionService;
import xz.research.manager.service.InitializeService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitializeServiceImpl implements InitializeService, CommandLineRunner {
    @Autowired
    private RedisService redisService;

    @Autowired
    private IRcRegionService iRcRegionService;

    @Override
    public void run(String... args) throws Exception {
        this.loadRegionData();
    }

    /**
     * 加载地址数据信息到redies，防止重复插入
     */
    @Override
    public void loadRegionData() throws RedisConnectException, IOException {
        List<RcRegion> list = iRcRegionService.list();
        if(list != null){
            ObjectMapper mapper = new ObjectMapper();
            for (RcRegion rcRegion : list) {
                String level = String.valueOf(rcRegion.getLevel());
                String checkKey = rcRegion.getRegionName() + rcRegion.getParentId();
                Map<String, String> map = new HashMap<>();
                if (this.redisService.exists(level)) {
                    if (!this.redisService.mapExistsKey(level, checkKey)) {
                        this.redisService.hset(level, checkKey, rcRegion);
                    }
                } else {
                    map.put(checkKey, mapper.writeValueAsString(rcRegion));
                    this.redisService.setMap(level, map);
                }

            }
        }
    }



}
