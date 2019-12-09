package xz.research.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xz.research.manager.entity.RcRegion;
import xz.research.manager.mapper.RcRegionMapper;
import xz.research.manager.service.IRcRegionService;

import java.util.Arrays;
import java.util.List;

/**
 * @author ZhongLe
 */
@Service
public class RcRegionServiceImpl extends ServiceImpl<RcRegionMapper, RcRegion> implements IRcRegionService {

    @Override
    public void createRegion(List<RcRegion> regions) {
        boolean b = this.saveBatch(regions);
    }

    @Override
    public void updateRegion(RcRegion r) {
        this.updateById(r);
    }

    @Override
    public void deleteRegions(String[] regionIds) {
        this.removeByIds(Arrays.asList(regionIds));
    }
}
