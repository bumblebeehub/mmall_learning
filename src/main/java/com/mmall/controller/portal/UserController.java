package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import net.sf.jsqlparser.schema.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by mjl on 18-7-13.
 */
@Controller   //当用controller标签说明这是一个controller之后，它会被dispatcherservlet进行上下文的管理,能够使用依赖注入等
@RequestMapping("/user/")    //说明controller负责处理的根url
public class UserController {

    @Autowired
    private IUserService iUserService;


    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method =RequestMethod.POST)    //和类级别的requestmapping一起构成方法要处理的urlmapping
    //处理/user/login.do下的请求，method表明只处理post请求
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session)
    {
        ServerResponse<User> response = iUserService.login(username, password);
        if(response.isSuccess())
        {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }


    //登出功能
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session)
    {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    //注册
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user)
    {
        return iUserService.register(user);
    }

    //校验
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type)
    {
        return iUserService.checkValid(str, type);
    }

    //获取登陆信息
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null)
        {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登陆，无法获得用户信息");
    }

    //忘记密码,返回忘记密码提示问题
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username)
    {
        return iUserService.selectQuestion(username);
    }


    //校验问题答案是否正确
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer)
    {
        return iUserService.checkAnswer(username, question, answer);
    }


    //重置密码
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken)
    {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }


    //登录状态重置密码
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }


    //更新个人信息
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user)
    {
        User currentuser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentuser == null)
        {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        //新的用户信息里面是一些比如说新的忘记密码问题啦，邮箱，手机号等，是没有userId的，所以需要重新设置一下它的userId
        user.setId(currentuser.getId());
        user.setUsername(user.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess())
        {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }

        return response;
    }

    //获取用户信息
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session)
    {
        User currentuser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentuser == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，需要强制登录");
        }

        return iUserService.getInformation(currentuser.getId());

    }

}
