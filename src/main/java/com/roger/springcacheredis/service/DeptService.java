package com.roger.springcacheredis.service;

import com.roger.springcacheredis.entities.EmpVO;
import com.roger.springcacheredis.entities.MockData;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author RogerLo
 * @date 2025/8/17
 */
@Service
public class DeptService {

    /**
     * 根據部門ID獲取員工列表
     *
     * 預設會使用主要的 CacheManager，使用 DB 0
     */
    @Cacheable(cacheNames = "empListCache", key = "#root.methodName + '_' + #deptId", cacheManager = "primaryCacheManager")
    public List<EmpVO> getEmpsByDeptId(long deptId) {
        return List.of(
                MockData.EMP_DB
                        .stream()
                        .filter(emp -> emp.getDeptVO().getId() == deptId)
                        .toArray(EmpVO[]::new)
        );
    }

}
