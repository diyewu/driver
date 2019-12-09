package xz.research.manager.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xz.research.common.bh.NamingConfig;
import xz.research.common.bh.TreePathService;
import xz.research.common.config.CustomConfig;
import xz.research.common.domain.FebsResponse;
import xz.research.common.exception.FebsException;
import xz.research.common.exception.RedisConnectException;
import xz.research.common.service.RedisService;
import xz.research.manager.entity.RcRegion;
import xz.research.manager.service.IRcRegionService;
import xz.research.system.domain.User;
import xz.research.utils.ExcelReadUtils;
import xz.research.utils.SMSUtil;
import xz.research.utils.SortableUUID;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static xz.research.utils.FebsUtil.getCurrentUser;

@Slf4j
@Validated
@RestController
@RequestMapping("region")
public class RegionController {

    static final int nThreads = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(nThreads * 5, nThreads * 8, 60, TimeUnit.SECONDS,
                                                                        new ArrayBlockingQueue<Runnable>(15000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            t.setName("recommend_task");
            return t;
        }
    }, new ThreadPoolExecutor.DiscardPolicy());


    @Autowired
    private CustomConfig customConfig;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IRcRegionService iRcRegionService;

    @Autowired
    private TreePathService treePathService;

    @PostMapping("/importRegionData")
    @ApiOperation(value = "导入地区数据", notes = "导入地区数据", httpMethod = "POST", consumes = "multipart/form-data")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authentication", paramType = "header", value = "token信息", dataType = "String", required = true)
    }
    )
    public FebsResponse importRegionData(
            MultipartFile file,
            HttpServletRequest request) throws Exception {

        String message;
        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")) {
            throw new FebsException("只支持.xlsx类型文件导入");
        }
        System.out.println("filename=" + filename);

        String tempFileDir = customConfig.getClientFileUpladPath();
        File tempDir = new File(tempFileDir);
        if (!tempDir.isDirectory()) {
            tempDir.mkdir();
        }
        String fileFileName = new Date().getTime() + file.getOriginalFilename();
        File nfile = new File(tempDir, fileFileName);
        if (!nfile.exists()) {
            nfile.createNewFile();
        }
        file.transferTo(nfile);
        //线程执行数据导入操作，执行完成后短信通知操作人
        executor.execute(new Thread() {
            public void run() {
                insertValue(nfile);
            }
        });

        return new FebsResponse().message("传入文件成功,请等待数据完成通知").data("");
    }

    /**
     * 必须加锁，防止多线程情况下，重复插入非子级节点
     * @param nfile
     */
    private synchronized void insertValue(File nfile){
        try {
            ArrayList<ArrayList<Object>> arrayLists = ExcelReadUtils.readAllRows(nfile);
            List<RcRegion> rcRegions = processRegionData(arrayLists);
            iRcRegionService.createRegion(rcRegions);
            generateBH("RC_REGION", null);//最暴力的直接生成全表BH，优化点，应该只需要生成最高一级没有插入过的数据的BH
            //发送短信
            User currentUser = getCurrentUser();
            SMSUtil.sendSMS(currentUser.getMobile(), 1, null, currentUser.getUsername(), "基础数据导入");
        } catch (Throwable e) {
            log.error("", e);
        }
    }

    /**
     * 生成BH，parentId 为空时，重新生成整表BH
     *
     * @param table
     * @param parentId
     */
    private void generateBH(String table, String parentId) {
        if (StringUtils.isNotBlank(table)) {
            NamingConfig cfg = new NamingConfig();
            cfg.setId("id");
            cfg.setTable(table);
            cfg.setParentId("parent_id");
            cfg.setSortField("priority");
            treePathService.buildPath(parentId, cfg);
        }
    }

    private List<RcRegion> processRegionData(ArrayList<ArrayList<Object>> arrayLists) throws RedisConnectException, IOException {
        List<RcRegion> insertValues = new ArrayList<RcRegion>();
        if (arrayLists != null && arrayLists.size() > 0) {
            ObjectMapper mapper = new ObjectMapper();
            for (int k = 1; k < arrayLists.size(); k++) {
                ArrayList<Object> arrayList = arrayLists.get(k);
                if (arrayList != null) {
                    String parentId = null;
                    RcRegion building = null;
                    for (int i = 0; i < arrayList.size(); i++) {
                        // TODO 校验EXCEL数据  有些列不能为空
                        String value = (String) arrayList.get(i);
                        String levelConstant = "region_level_" + (i + 1);
                        if (StringUtils.isBlank(value) || "null".equals(value.toLowerCase()))
                            continue;
                        if ((i == 9 || i == 10) && building != null) {
                            if (i == 9) {
                                building.setLongitude(value);
                            } else if (i == 10) {
                                building.setLatitude(value);
                                RcRegion b = new RcRegion();
                                try {
                                    BeanUtils.copyProperties(b, building);//cp bean
                                } catch (Exception e) {
                                    log.error("", e);
                                }
                                insertValues.add(b);
                                building = null;
                            }
                            continue;
                        }

                        String tempId = SortableUUID.randomUUID();
                        String checkKey = value + parentId;
                        if (this.redisService.exists(levelConstant)) {
                            if (this.redisService.mapExistsKey(levelConstant, checkKey)) {
                                RcRegion redisRc = this.redisService.hget(levelConstant, checkKey);
                                parentId = redisRc.getId();
                                continue;
                            } else {
                                if (i != 7) {
                                    RcRegion r = new RcRegion();
                                    r.setId(tempId);
                                    r.setRegionName(value);
                                    r.setLevel(i + 1);
                                    r.setParentId(parentId);
                                    insertValues.add(r);
                                    this.redisService.hset(levelConstant, checkKey, r);
                                } else {
                                    building = new RcRegion();
                                    building.setId(tempId);
                                    building.setRegionName(value);
                                    building.setLevel(i + 1);
                                    building.setParentId(parentId);
                                }
                            }
                        } else {
                            Map<String, String> map = new HashMap<>();
                            if (i != 7) {
                                RcRegion r = new RcRegion();
                                r.setId(tempId);
                                r.setRegionName(value);
                                r.setLevel(i + 1);
                                r.setParentId(parentId);
                                map.put(checkKey, mapper.writeValueAsString(r));
//                                this.redisService.setMap(levelConstant,map);
                                insertValues.add(r);
                            } else {
                                building = new RcRegion();
                                building.setId(tempId);
                                building.setRegionName(value);
                                building.setLevel(i + 1);
                                building.setParentId(parentId);
                                map.put(checkKey, mapper.writeValueAsString(building));
                            }
                            this.redisService.setMap(levelConstant, map);
                        }
                        parentId = tempId;

                    }
                }
            }
        }
        return insertValues;
    }
}
