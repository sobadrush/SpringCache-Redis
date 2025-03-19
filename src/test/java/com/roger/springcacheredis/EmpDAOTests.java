package com.roger.springcacheredis;

import com.roger.springcacheredis.out.EmpDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmpDAOTests {

    @Autowired
    private EmpDAO empDAO;

    @Test
    @DisplayName("[Test-001] 測試 EmpDAO + @Cacheable")
    void test_001() {
        System.out.println(empDAO.getEmp(7001));
        System.out.println(empDAO.getEmp(7002));
        System.out.println(empDAO.getEmp(7003));
    }

}
