package kr.hhplus.be.server.user.domain;


import java.util.Optional;

public interface IUserRepository {
    Optional<User> findById(Long aLong);

    User save(User user);
}
