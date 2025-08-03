package kr.hhplus.be.server.user.exception;

import kr.hhplus.be.server.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long userId) {
        super("해당 유저를 찾을 수 없습니다. 유저 ID: " + userId);
    }
}