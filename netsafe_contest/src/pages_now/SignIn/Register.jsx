import React, { Component } from 'react'

export default class index extends Component {
  render() {
    return (
      <div>
            <div className="login">
                <h2>注册账号</h2>
                <div className="login_form">
                    <span>账号设置</span>
                    <input type="text" placeholder="请输入昵称"/>
                    <br/>
                    <span>设置新密码：</span>
                    <input type="password" placeholder="请输入密码"/>
                    <span>确认新密码：</span>
                    <input type="password" placeholder="请输入密码"/>
                </div>
                <div className="btn">
                    <button className="login_btn" onclick="login()">确 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;定</button>
                </div>
            </div>
      </div>
    )
  }
}
