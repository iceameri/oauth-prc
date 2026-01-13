package com.jw.resourceserver.controller.opened;

import com.jw.resourceserver.controller.BaseController;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class BaseOpenedController extends BaseController {

    public static final String OPENED_API_PREFIX = BaseController.API_PREFIX + "/open";
}
