package com.roger.springcacheredis.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author RogerLo
 * @date 2025/3/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpVO implements Serializable {
    private int empNo;
    private String empName;
    private Integer empAge;
}
