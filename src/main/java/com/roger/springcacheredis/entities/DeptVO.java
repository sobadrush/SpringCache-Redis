package com.roger.springcacheredis.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author RogerLo
 * @date 2025/8/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeptVO {
    private Long id;
    private String deptName;
    private String deptLoc;
    @JsonManagedReference
    private Set<EmpVO> emps;
}