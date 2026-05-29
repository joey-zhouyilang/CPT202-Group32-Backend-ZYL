package com.group32.cpt202.zyl_project.zyl_login;

import com.group32.cpt202.LY_contributor.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;/**


/**
 * UserRepository 类，处理当前模块的业务逻辑或数据结构。
 */
@Repository("zylLoginUserRepository")
public class UserRepository {
    private final com.group32.cpt202.LY_contributor.repository.UserRepository delegate;

    public UserRepository(com.group32.cpt202.LY_contributor.repository.UserRepository delegate) {
        this.delegate = delegate;
    }

    public Optional<User> findById(Long id) {
        return delegate.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return delegate.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return delegate.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return delegate.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return delegate.existsByEmail(email);
    }

    public List<User> findAllByOrderByIdAsc() {
        return delegate.findAllByOrderByIdAsc();
    }

    public List<User> findByRoleOrderByIdAsc(User.Role role) {
        return delegate.findByRoleOrderByIdAsc(role);
    }

    public User save(User user) {
        return delegate.save(user);
    }
}