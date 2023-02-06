import React from 'react'
import { useRoutes, NavLink} from "react-router-dom"

export default function Sign() {
  return (
    <div>
        <h2>登录</h2>
        <div className="login_form">
            <span>账号：</span>
            <input type="text" placeholder="请输入账号"/>
            <br/>
            <span>密码：</span>
            <input type="password" placeholder="请输入密码"/>
        </div>
        <div className="account_else">
            {/* <span class="register"> <a href="{<Forget/>}">注册账号</a></span>
            <span><a href="">忘记密码？</a></span> */}

            <span className="register"> 
                <NavLink to='/register'>注册账号</NavLink>
            </span>
            <span>
                <NavLink to='/forget'>忘记密码？</NavLink>
            </span>
        </div>
        <div className="btn">
            <button className="login_btn" onclick="login()">登 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;录</button>
        </div>
    </div>
  )
}
