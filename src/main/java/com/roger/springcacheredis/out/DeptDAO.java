package com.roger.springcacheredis.out;

import com.roger.springcacheredis.entities.DeptVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import static com.roger.springcacheredis.entities.MockData.DEPT_DB;

/**
 * @author RogerLo
 * @date 2025/8/17
 */
@Repository
public class DeptDAO {

    @Cacheable(cacheNames = "deptCache", key = "#deptId", cacheManager = "deptCacheManager")
    public DeptVO getDept(long deptId) {
        System.out.println("[ 呼叫 - getDept ] Fetching user from Redis: " + deptId);
        return DEPT_DB.stream().filter(dd -> deptId == dd.getId()).findFirst().get();
    }

}
