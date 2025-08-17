package com.roger.springcacheredis.entities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author RogerLo
 * @date 2025/8/17
 */
public class MockData {

    // 模擬資料庫
    public static final List<EmpVO> EMP_DB = new ArrayList<>();
    public static final Set<DeptVO> DEPT_DB = new LinkedHashSet<>();

    static {
        EMP_DB.add(EmpVO.builder()
                .empNo(7001)
                .empName("Roger")
                .empAge(21)
                .deptVO(DeptVO.builder().id(222L).build())
                .build());

        EMP_DB.add(EmpVO.builder()
                .empNo(7002)
                .empName("Kelly")
                .empAge(22)
                .deptVO(DeptVO.builder().id(222L).build())
                .build());

        EMP_DB.add(EmpVO.builder()
                .empNo(7003)
                .empName("Cathy")
                .empAge(23)
                .deptVO(DeptVO.builder().id(333L).build())
                .build());
    }

    static {
        DEPT_DB.add(DeptVO.builder().id(111L).deptName("HR").deptLoc("New York").build());
        DEPT_DB.add(DeptVO.builder().id(222L).deptName("IT").deptLoc("San Francisco").build());
        DEPT_DB.add(DeptVO.builder().id(333L).deptName("Finance").deptLoc("Chicago").build());
    }

}
