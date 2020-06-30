import * as React from 'react'
import {MapComponent} from "@/MapComponent";

var styles = require('./App.css') as any

export const App = () => (
    <div className={styles.appWrapper}>
        <MapComponent/>
    </div>
)