package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌

        String token = JwtUtil.createJWT(employee.getId());

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }



    @PostMapping
    public Result addEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工，员工信息：{}",employeeDTO);
        employeeService.addEmployee(employeeDTO);
        return Result.success();
    }


    @GetMapping("/page")
    public Result<PageResult> pageSelect( EmployeePageQueryDTO employeePageQueryDTO){
        return employeeService.pageSelect(employeePageQueryDTO);
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,Long id){
        return employeeService.startOrStop(status,id);
    }

    @PutMapping
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("员工信息：{}",employeeDTO);
        employeeService.updateEmployee(employeeDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Employee> selectById(@PathVariable Long id){
        log.info("员工id：{}",id);
        Employee employee = employeeService.query().eq("id",id).one();
        return Result.success(employee);
    }


    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改密码：{}",passwordEditDTO);
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }

}
