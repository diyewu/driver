package xz.research.manager.service;

import xz.research.manager.entity.RcRegion;
import com.baomidou.mybatisplus.extension.service.IService;
import xz.research.system.domain.Dept;

import java.util.List;

/**
 * @author ZhongLe
 */
public interface IRcRegionService extends IService<RcRegion> {

    void createRegion(List<RcRegion> regions);

    void updateRegion(RcRegion r);

    void deleteRegions(String[] regionIds);

}
