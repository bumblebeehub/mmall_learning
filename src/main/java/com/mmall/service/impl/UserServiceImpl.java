package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Created by mjl on 18-7-15.
 */
@Service("iUserService")   //注入到controller
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;


    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0)
        {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //此时对比的应该是加密后的密码
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null)
        {
            //说明and条件没有匹配到，肯定是密码错误
            return ServerResponse.createByErrorMessage("密码错误");
        }
        
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user)
    {
        //复用了isValid的方法
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!validResponse.isSuccess())
        {
            return validResponse;
        }

        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!validResponse.isSuccess())
        {
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0)
        {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");

    }


    //实时校验
    public ServerResponse<String> checkValid(String str, String type)
    {
        if(StringUtils.isNotBlank(type))
        {
            //非空才开始校验
            if(Const.USERNAME.equals(type))
            {
                //str是用户名
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0)
                {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }

            if(Const.EMAIL.equals(type))
            {
                //str是email
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0)
                {
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }
        else
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    //返回忘记密码提示问题
    public ServerResponse<String> selectQuestion(String username)
    {
        int resultCount = userMapper.checkUsername(username);
        //直接复用checkvalid
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess())
        {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question))
        {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码问题为空");
    }

    //提示问题与答案
    public ServerResponse<String> checkAnswer(String username, String question, String answer)
    {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount > 0)
        {
            //说明问题及问题答案均属于这个用户且正确
            //生成token是不想要非用户使用忘记密码这个功能来修改密码
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);//本地缓存放入token
            return ServerResponse.createBySuccess(forgetToken);
        }

        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    //重置密码
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken)
    {
        //首先校验参数
        if(StringUtils.isBlank(forgetToken))
        {
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        int resultCount = userMapper.checkUsername(username);
        //直接复用checkvalid
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess())
        {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token))
        {
            return ServerResponse.createByErrorMessage("token无效或者token过期");
        }

        if(StringUtils.equals(forgetToken, token))
        {
            //token一致，才可以修改密码
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);  //生效行数
            if(rowCount>0)
            {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
            else
            {
                return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
            }
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user)
    {
        //防止横向越权，要校验这个旧密码是不是属于这个用户的密码，不然的话查询count(1)，若不指定id，返回结果为true，count>0
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0)
        {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>0)
        {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user)
    {
        //username是不能被更新的，email是需要被校验的，校验新的email是否存在，并且该存在的email如果相同的话，不能是当前用户的
        //意思就是，如果email存在，且被其他人占用，是不能更新的，如果email存在，但是属于当前用户，那就没必要更新
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if(resultCount > 0)
        {
            return ServerResponse.createByErrorMessage("email已存在，请更换email再尝试更新");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0)
        {
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }

        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> getInformation(Integer userId)
    {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null)
        {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }

        user.setPassword(StringUtils.EMPTY);   //注意把密码置为空
        return ServerResponse.createBySuccess(user);
    }
}
