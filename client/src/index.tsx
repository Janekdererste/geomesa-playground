import {App} from '@/App'
import ReactDOM from 'react-dom'
import * as React from 'react'

const root = document.createElement('div') as HTMLDivElement
root.id = 'root'
root.style.height = '100%'
document.body.appendChild(root)

ReactDOM.render(<App/>, root)