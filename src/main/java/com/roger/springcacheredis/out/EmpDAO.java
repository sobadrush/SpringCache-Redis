package com.roger.springcacheredis.out;

import com.roger.springcacheredis.entities.EmpVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RogerLo
 * @date 2025/3/19
 */
@Repository
public class EmpDAO {

    // 模擬資料庫
    private static final List<EmpVO> EMP_DB = new ArrayList<>();

    static {
        EMP_DB.add(EmpVO.builder().empNo(7001).empName("Roger").empAge(21).build());
        EMP_DB.add(EmpVO.builder().empNo(7002).empName("Kelly").empAge(22).build());
        EMP_DB.add(EmpVO.builder().empNo(7003).empName("Cathy").empAge(23).build());
    }

    /**
     * 取得員工資料 && 緩存
     */
    @Cacheable(cacheNames = "empCache", key = "#empId", cacheManager = "empCacheManager")
    public EmpVO getEmp(int empId) {
        System.out.println("[ 呼叫 - getEmp ] Fetching user from Redis: " + empId);
        return EMP_DB.stream().filter(empVO -> empVO.getEmpNo() == empId).findFirst().get();
    }

    /**
     * 更新用户信息，并更新缓存
     */
    @CachePut(cacheNames = "empCache", key = "#empId")
    public String updateEmp(int empId, String name) {
        System.out.println("Updating user in DB: " + empId);
        EMP_DB.stream()
                .filter(empVO -> empVO.getEmpNo() == empId && empVO.getEmpName().equals(name))
                .findFirst().get()
                .setEmpName(name);
        return name;
    }

    /**
     * 删除用户缓存
     */
    @CacheEvict(cacheNames = "empCache", key = "#empId")
    public void deleteEmp(int empId) {
        System.out.println("Deleting Emp cache: " + empId);
    }

    /**
     * 清除整个缓存
     */
    @CacheEvict(cacheNames = "empCache", allEntries = true)
    public void clearCache() {
        System.out.println("Clearing all Emp cache");
    }

}
