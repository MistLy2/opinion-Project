import { Navigate } from "react-router-dom";
import Forget from '../pages/SignIn/Forget';
import Register from '../pages/SignIn/Register';
import Sign from "../pages/SignIn/Sign";

export default [
    {
        path:'/forget',
        element:<Forget/>
    },
    {
        path:'/register',
        element:<Register/>
    },
    {
        path:'/',
        element:<Sign/>
    }
]