package com.roger.springcacheredis;

import com.roger.springcacheredis.entities.EmpVO;
import com.roger.springcacheredis.service.DeptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
class DeptServiceTests {

    @Autowired
    private DeptService deptService;

    @Test
    @DisplayName("[Test-001] 測試 deptService.getEmpsByDeptId")
    void test_001() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            List<EmpVO> emps = deptService.getEmpsByDeptId(222L);
            emps.forEach(emp -> System.out.println("emp = " + emp));
        });
    }

}
