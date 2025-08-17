package com.roger.springcacheredis.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author RogerLo
 * @date 2025/3/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpVO {
    private int empNo;
    private String empName;
    private Integer empAge;
    @JsonBackReference
    private DeptVO deptVO;
}