package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee>implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = query().eq("username",username).one();

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        String pass = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!pass.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setStatus(StatusConstant.ENABLE);
        save(employee);

    }

    @Override
    public Result<PageResult> pageSelect(EmployeePageQueryDTO employeePageQueryDTO) {
        Page<Employee> page = Page.of(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        page.addOrder(OrderItem.asc("id"));

        QueryWrapper<Employee> employeeQueryWrapper = new QueryWrapper<Employee>()
                .like("username",employeePageQueryDTO.getName());
        if(employeePageQueryDTO.getName() == null){
            employeeQueryWrapper = new QueryWrapper<Employee>();
        }

        Page<Employee> p = page(page,employeeQueryWrapper);
        List<Employee> res = p.getRecords();
        long total = p.getTotal();
        return Result.success(new PageResult(total,res));
    }

    @Override
    public Result startOrStop(Integer status, Long id) {
        Employee employee = query().eq("id",id).one();
        if(employee == null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if(status==employee.getStatus()){

        }
        employee.setStatus(status);
        updateById(employee);
        return Result.success();
    }

    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = query().eq("id",employeeDTO.getId()).one();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        updateById(employee);
    }

    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Employee employee = query().eq("id",passwordEditDTO.getEmpId()).one();
        if(employee == null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!employee.getPassword().equals(DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes()))) {

            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        employee.setPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()));
        updateById(employee);
    }

}
