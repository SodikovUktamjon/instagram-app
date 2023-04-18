package com.instagram.instagram.service;

import com.instagram.instagram.criteria.FollowersCriteria;
import com.instagram.instagram.domains.Follow;
import com.instagram.instagram.domains.Notification;
import com.instagram.instagram.domains.basic.User;
import com.instagram.instagram.repository.NotificationRepository;
import com.instagram.instagram.domains.auth.AuthUser;
import com.instagram.instagram.repository.FollowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FollowService {
    private final FollowRepository followRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public FollowService(FollowRepository followRepository,
                         NotificationRepository notificationRepository, UserService userService) {
        this.followRepository = followRepository;
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public void save(AuthUser from, AuthUser to) {
        followRepository.save(new Follow(from,to, LocalDateTime.now()));
        log.info("User with id {} is following user with id {}",from.getId(),to.getId());
        notificationRepository.save(new Notification(from.getUsername()+" has started following you!",false,from, Notification.NotificationType.FOLLOW));
        log.info("Notification sent to user with id {} about following",to.getId());
    }

    public void unfollow(AuthUser from, AuthUser to) {
        followRepository.delete(from.getId(),to.getId());
        log.info("User with id {} is following user with id {}",from.getId(),to.getId());
        notificationRepository.save(new Notification(from.getUsername()+" has stopped following you!",false,from, Notification.NotificationType.FOLLOW));
        log.info("Notification sent to user with id {} about unfollowing",to.getId());
    }

    public List<User> findAll(FollowersCriteria criteria) {
        Sort sort;
        if(criteria.getOrder()== FollowersCriteria.Order.ASC){
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        }else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        Page<Follow> follows = followRepository.findAll(pageRequest);
        List<Long> ids = follows.stream().map(e -> e.getFollower().getId()).toList();
        return userService.getUsers(ids, criteria);
    }
}