package com.roger.springcacheredis;

import com.roger.springcacheredis.out.DeptDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

@SpringBootTest
class DeptDAOTests {

    @Autowired
    private DeptDAO deptDAO;

    @Test
    @DisplayName("[Test-001] 測試 DeptDAO + @Cacheable")
    void test_001() {
        IntStream.range(0, 10).forEach(i -> {
            System.out.println(deptDAO.getDept(111L));
            System.out.println(deptDAO.getDept(222L));
            System.out.println(deptDAO.getDept(333L));
            System.out.println("===================================");
        });
    }

}
