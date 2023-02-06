import React from 'react'
import { useRoutes, NavLink} from "react-router-dom";
import routes from './routes'
import './App.css'

export default function App(){
    //根据路由表生成对应的路由规则
    const element = useRoutes(routes)
        return (
        <div>
            <div className="login">
                {/* 注册路由 */}
                {element}
            </div>
        </div>
    )
}



