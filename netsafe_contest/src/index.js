import React from 'react'
import createRoot from 'react-dom'
import { BrowserRouter } from 'react-router-dom'
import APP from './App'

createRoot.render(
    <BrowserRouter>
        <APP/>
    </BrowserRouter>,
    document.getElementById('root')
)