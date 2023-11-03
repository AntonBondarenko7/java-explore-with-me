package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotSavedException;
import ru.practicum.ewm.user.dto.NewUserRequestDto;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        if (ids == null) {
            return UserMapper.INSTANCE.convertUserListToUserDTOList(
                    userRepository.findAll(page).getContent());
        } else {
            return UserMapper.INSTANCE.convertUserListToUserDTOList(
                    userRepository.findAllByIdIn(ids, page));
        }
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    public UserDto saveUser(NewUserRequestDto newUserRequest) {
        try {
            User user = userRepository.save(
                    UserMapper.INSTANCE.toUserFromNewDto(newUserRequest));
            return UserMapper.INSTANCE.toUserDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Пользователь не был создан: " + newUserRequest);
        }
    }

    public void deleteUserById(Long userId) {
        findUserById(userId);
        userRepository.deleteById(userId);
    }

}
