const path = require('path')
const HTMLWebpackPlugin = require('html-webpack-plugin')

module.exports = {
    mode: 'development',
    entry: path.resolve(__dirname, 'src', 'index.tsx'),
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'bundle.js',
    },
    devServer: {
        contentBase: path.resolve(__dirname, 'dist'),
        https: false,
        port: 3000,
    },
    devtool: 'source-map',
    resolve: {
        alias: {'@': path.resolve(__dirname, 'src')},
        extensions: ['.ts', '.tsx', '.js', '.json'],
    },
    module: {
        rules: [
            {test: /\.tsx?$/, loader: 'awesome-typescript-loader'},
            {enforce: 'pre', test: /\.js$/, loader: 'source-map-loader'},
            // enable css-modules
            {
                test: /\.css$/,
                use: [
                    {loader: 'style-loader'},
                    {
                        loader: 'css-loader',
                        options: {
                            sourceMap: true,
                            modules: true,
                            localIdentName: '[name]__[local]__[hash:base64:5]',
                        },
                    },
                ],
            },
            {test: /\.glsl$/, loader: 'raw-loader'}
        ],
    },
    plugins: [new HTMLWebpackPlugin({template: path.resolve(__dirname, 'src/index.html')})],

    // When importing a module whose path matches one of the following, just
    // assume a corresponding global variable exists and use that instead.
    // This is important because it allows us to avoid bundling all of our
    // dependencies, which allows browsers to cache those libraries between builds.

    // maybe use this later, but with HTMLWebpackPlugin it is easier without
    /*  externals: {
          "react": "React",
          "react-dom": "ReactDOM"
      }
      */
}