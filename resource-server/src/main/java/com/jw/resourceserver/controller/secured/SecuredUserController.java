package com.jw.resourceserver.controller.secured;

import com.jw.resourceserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = SecuredUserController.BASE_PATH)
public class SecuredUserController extends BaseSecuredController {

    public static final String BASE_PATH = BaseSecuredController.SECURED_API_PREFIX + "/user";

    private final UserService userService;

    public SecuredUserController(final UserService userService) {
        this.userService = userService;
    }
}
