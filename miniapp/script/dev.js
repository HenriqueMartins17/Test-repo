const fs = require('fs')
const path = require('path')
const env = process.argv.slice(2)[0] || 'dev'

const devConfig = {
  indexUrl: 'https://vikadata.com',
  apiUrl: 'https://vbs-integration.vika.ltd/api/v1/',
  appId: 'wxa53e1166321e4958'
}

const proConfig = {
  indexUrl: 'https://vika.cn',
  apiUrl: 'https://vika.cn/api/v1/',
  appId: 'wxb1a459d3a34bdb1f'
}

const CONFIG_JSON_PATH = path.resolve(__dirname, '../', 'project.config.json')

function modifyJSON () {
  fs.readFile(CONFIG_JSON_PATH, (err, data) => {
    const projectConfig = JSON.parse(data.toString())
    if (env === 'dev') {
      projectConfig.appid = devConfig.appId
    }
    if (env === 'pro') {
      projectConfig.appid = proConfig.appId
    }
    const _configJson = JSON.stringify(projectConfig)
    fs.writeFile(CONFIG_JSON_PATH, _configJson, 'utf-8', err => {
      if (err) {
        console.error(err);
      }
      console.log('----------config.json 配置成功-------------');
    })
  })
}

function isDirExit (path) {
  return new Promise((resolve, reject) => {
    fs.stat(path, function (err, data) {
      if (err) {
        resolve(false)
        console.log('路径不存在，创建中……');
      } else {
        resolve(data)
      }
    });
  })
}

function createDir (path) {
  fs.mkdirSync(path)
}

async function createBaseConfig () {
  const srcConfig = path.resolve(__dirname, '../src', 'config')
  if (!await isDirExit(srcConfig)) {
    createDir(srcConfig)
  }
  const data =
    `
export const indexUrl = '${env === 'pro' ? proConfig.indexUrl : devConfig.indexUrl}';
export const apiUrl = '${env === 'pro' ? proConfig.apiUrl : devConfig.apiUrl}';
`
  fs.writeFile(path.resolve(srcConfig, './index.ts'), data, err => {
    if (err) {
      console.error(err);
    }
    console.log('----------src/config/index.ts 配置成功-------------');
  })
}

modifyJSON()
createBaseConfig()
