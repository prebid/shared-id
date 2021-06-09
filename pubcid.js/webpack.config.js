const path = require("path");
const TerserPlugin = require("terser-webpack-plugin");

const outputDir = "dist";
const filename = "pubcid.min.js";

module.exports = {
    devtool: "source-map",
    entry: path.resolve(__dirname, "./src/index.js"),
    mode: "production",
    optimization: {
        minimizer: [
            new TerserPlugin(),
        ],
    },
    output: {
        filename,
        path: path.resolve(__dirname, outputDir),
    },
};
