package com.resume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.resume.entity.User;
import com.resume.mapper.UserMapper;
import com.resume.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
