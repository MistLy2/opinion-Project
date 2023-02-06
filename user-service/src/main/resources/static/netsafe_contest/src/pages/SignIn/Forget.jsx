import React, { Component } from 'react'

export default function Forget() {
    return (
      <div>
            <div className="login">
                <h2>忘记密码</h2>
                <div className="login_form">
                    <span>账号</span>
                    <input type="text" placeholder="请输入您的账号"/>
                    <br/>
                    <span>电话号码：</span>
                    <input type="password" placeholder="请输入电话号码"/>
                    <span>新密码：</span>
                    <input type="password" placeholder="请输入新密码"/>
                </div>
                <div className="btn">
                    <button className="login_btn" onclick="login()">报 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;存</button>
                </div>
            </div>
      </div>
    )
}
